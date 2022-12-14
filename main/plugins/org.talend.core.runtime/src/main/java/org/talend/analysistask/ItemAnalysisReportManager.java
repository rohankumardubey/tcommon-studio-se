// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.analysistask;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.widgets.WidgetFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.report.ItemsReportUtil;
import org.talend.commons.utils.io.FilesUtils;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.runtime.i18n.Messages;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryService;

/**
 * DOC jding  class global comment. Detailled comment
 */
public class ItemAnalysisReportManager {

    private static final String COMMA = ",";

    private static final String ANALYSIS_REPORT_HEAD = "Task name,Task description,Link to details,Severity,Item type,Path to item,Details";

    public static final ItemAnalysisReportManager INSTANCE = new ItemAnalysisReportManager();

    public static ItemAnalysisReportManager getInstance() {
        return INSTANCE;
    }

    private AtomicBoolean inGenerating = new AtomicBoolean(false);

    public List<AnalysisReportRecorder> executeAnalysisTask(Project project) {
        IRepositoryService service = (IRepositoryService) GlobalServiceRegister.getDefault().getService(IRepositoryService.class);
        IProxyRepositoryFactory repFactory = service.getProxyRepositoryFactory();
        List<AnalysisReportRecorder> analysisResultList = new ArrayList<AnalysisReportRecorder>();
        List<IItemAnalysisTask> analysisTasks = ItemAnalysisTaskRegistryReader.getInstance().getItemAnalysisTasks();
        Set<ERepositoryObjectType> types = new HashSet<ERepositoryObjectType>();
        analysisTasks.forEach(task -> {
            Set<ERepositoryObjectType> typeScope = task.getRepositoryObjectTypeScope();
            if (typeScope != null && !typeScope.isEmpty()) {
                types.addAll(typeScope);
            }
        });

        try {
            for (ERepositoryObjectType type : types) {
                if (!type.isResourceItem()) {
                    continue;
                }
                List<IRepositoryViewObject> objects = repFactory.getAll(project, type, true, true);
                for (IRepositoryViewObject object : objects) {
                    Item item = object.getProperty().getItem();
                    for (IItemAnalysisTask analysisTask : analysisTasks) {
                        try {
                            List<AnalysisReportRecorder> recorder = analysisTask.execute(item);
                            if (recorder != null && !recorder.isEmpty()) {
                                analysisResultList.addAll(recorder);
                            }
                        } catch (Exception e) {
                            ExceptionHandler.process(e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        return analysisResultList;
    }

    public void generateAnalysisReport(String projectTecName) {
        if (isAnalysisReportInGenerating()) {
            Display.getDefault().asyncExec(() -> {
                MessageDialog.openWarning(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                        Messages.getString("ItemAnalysisReportManager.Warning.title"),
                        Messages.getString("ItemAnalysisReportManager.Warning.message"));
            });
            return;
        }

        setAnalysisReportGenerating(true);
        Job job = new Job("Generating analysis report") {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    Project project = ProjectManager.getInstance().getProjectFromProjectTechLabel(projectTecName);
                    List<AnalysisReportRecorder> analysisReportRecorders = executeAnalysisTask(project);
                    if (analysisReportRecorders == null || analysisReportRecorders.isEmpty()) {
                        Display.getDefault().syncExec(() -> {
                            MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                                    Messages.getString("ItemAnalysisReportManager.Information.title"),
                                    Messages.getString("ItemAnalysisReportManager.Information.message"));
                        });
                        return Status.OK_STATUS;
                    }
                    Collections.sort(analysisReportRecorders, (recorder1, recorder2) -> {
                        return recorder1.getSeverity().getPriority() - recorder2.getSeverity().getPriority();
                    });
                    List<String> recordLines = new ArrayList<String>();
                    for (AnalysisReportRecorder record : analysisReportRecorders) {
                        StringBuffer buffer = new StringBuffer();
                        buffer.append(ItemsReportUtil.handleColumnQuotes(record.getTaskName())).append(COMMA);
                        buffer.append(ItemsReportUtil.handleColumnQuotes(record.getTaskDescription())).append(COMMA);
                        buffer.append(ItemsReportUtil.handleColumnQuotes(record.getLinkToDetail())).append(COMMA);
                        buffer.append(ItemsReportUtil.handleColumnQuotes(record.getSeverity().getLabel())).append(COMMA);
                        buffer.append(ItemsReportUtil.handleColumnQuotes(record.getItemType())).append(COMMA);
                        buffer.append(ItemsReportUtil.handleColumnQuotes(record.getItemPath())).append(COMMA);
                        buffer.append(ItemsReportUtil.handleColumnQuotes(record.getDetailMessage()));
                        recordLines.add(buffer.toString());
                    }

                    String currentTimeString = ItemsReportUtil.getCurrentTimeString();
                    String folderName = "analysisReport" + "_" + currentTimeString;
                    String fileName = currentTimeString + "_" + projectTecName + "_Analysis_Report.csv";
                    String filePath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString() + "/report/" + folderName
                            + "/" + fileName;
                    File reportFile = new File(filePath);
                    boolean generateSuccess = ItemsReportUtil.generateReportFile(reportFile, ANALYSIS_REPORT_HEAD, recordLines);
                    if (generateSuccess) {
                        Display.getDefault().asyncExec(() -> {
                            AnalysisReportAccessDialog accessDialog = new AnalysisReportAccessDialog(
                                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                                    reportFile.getAbsolutePath());
                            accessDialog.open();
                        });
                    }
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                } finally {
                    setAnalysisReportGenerating(false);
                }
                return Status.OK_STATUS;
            }

        };
        job.setUser(false);
        job.setPriority(Job.INTERACTIVE);
        job.schedule();
    }

    public boolean isAnalysisReportInGenerating() {
        return inGenerating.get();
    }

    private void setAnalysisReportGenerating(boolean newValue) {
        inGenerating.set(newValue);
    }

}

class AnalysisReportAccessDialog extends Dialog {

    private String reportGeneratedFile;

    protected AnalysisReportAccessDialog(Shell parentShell, String reportGeneratedFile) {
        super(parentShell);
        this.reportGeneratedFile = reportGeneratedFile;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.getString("AnalysisReportAccessDialog.shellTitle"));
    }

    @Override
    protected void initializeBounds() {
        getShell().setSize(700, 190);
        Point location = getInitialLocation(getShell().getSize());
        getShell().setLocation(location.x, location.y);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        Composite container = WidgetFactory.composite(SWT.NONE).layout(layout).layoutData(new GridData(GridData.FILL_BOTH))
                .create(parent);
        applyDialogFont(container);

        Composite composite = new Composite(container, SWT.NONE);
        GridLayout compositeLayout = new GridLayout();
        compositeLayout.numColumns = 1;
        compositeLayout.marginWidth = 0;
        compositeLayout.marginTop = 8;
        compositeLayout.marginLeft = 10;
        composite.setLayout(compositeLayout);
        Label successMsgLabel = new Label(composite, SWT.NONE);
        successMsgLabel.setText(Messages.getString("AnalysisReportAccessDialog.generateSuccess"));
        GridData gridData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_VERTICAL);
        successMsgLabel.setLayoutData(gridData);

        Link accessLink = new Link(composite, SWT.NONE);
        accessLink.setText(Messages.getString("AnalysisReportAccessDialog.completeReportAvailable") + " <a>"
                + Messages.getString("AnalysisReportAccessDialog.accessReport") + "</a> ");
        accessLink.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_VERTICAL));
        accessLink.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                File reportFile = new File(reportGeneratedFile);
                if (reportFile != null && reportFile.exists()) {
                    try {
                        FilesUtils.selectFileInSystemExplorer(reportFile);
                    } catch (Exception excep) {
                        ExceptionHandler.process(excep);
                    }
                }
            }

        });

        return container;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    }

}
