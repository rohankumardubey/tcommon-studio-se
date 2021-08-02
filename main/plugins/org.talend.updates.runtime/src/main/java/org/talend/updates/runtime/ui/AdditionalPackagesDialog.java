// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.updates.runtime.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.PojoObservables;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.ISetChangeListener;
import org.eclipse.core.databinding.observable.set.SetChangeEvent;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.Properties;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.databinding.viewers.ObservableSetTreeContentProvider;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.update.PreferenceKeys;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.commons.utils.system.EclipseCommandLine;
import org.talend.commons.utils.time.PropertiesCollectorUtil;
import org.talend.core.model.general.ModuleToInstall;
import org.talend.updates.runtime.InstallFeatureObserver;
import org.talend.updates.runtime.engine.InstallNewFeatureJob;
import org.talend.updates.runtime.engine.component.ComponentNexusP2ExtraFeature;
import org.talend.updates.runtime.i18n.Messages;
import org.talend.updates.runtime.model.ExtraFeature;
import org.talend.updates.runtime.model.FeatureCategory;
import org.talend.updates.runtime.model.TalendWebServiceUpdateExtraFeature;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 * DOC wchen class global comment. Detailled comment
 */
public class AdditionalPackagesDialog extends TitleAreaDialog {

    private static Logger log = Logger.getLogger(AdditionalPackagesDialog.class);

    public static final String DO_NOT_SHOW_EXTERNALMODULESINSTALLDIALOG = "do_not_show_ExternalModulesInstallDialog"; //$NON-NLS-1$

    private Color color = new Color(null, 255, 255, 255);

    protected TableViewerCreator<ModuleToInstall> tableViewerCreator;

    protected String text;

    protected String title;

    protected Button installAllBtn;
    
    protected Button closeButton;
    
    protected SelectionAdapter closeListener;

    protected List<String> jarsInstalledSuccuss = new ArrayList<String>();

    protected List<ModuleToInstall> inputList = new ArrayList<ModuleToInstall>();

    private Tree tree;

    protected UpdateWizardModel updateWizardModel;

    protected CheckboxTreeViewer checkboxTreeViewer;

    private StyledText featureDescriptionText;

    public DataBindingContext dbc;

    public AdditionalPackagesDialog(Shell shell, String text, String title, UpdateWizardModel updateWizardModel) {
        super(shell);
        setShellStyle(SWT.DIALOG_TRIM | SWT.MIN | SWT.MAX | SWT.RESIZE | getDefaultOrientation());
        this.text = text;
        this.title = title;
        this.updateWizardModel = updateWizardModel;
        closeListener = new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                closePressed();
            }
        };
    }

    public AdditionalPackagesDialog(Shell shell, String text, String title) {
        super(shell);
        setShellStyle(SWT.DIALOG_TRIM | SWT.MIN | SWT.MAX | SWT.RESIZE | getDefaultOrientation());
        this.text = text;
        this.title = title;
    }
    
    public void showDialog(boolean block) {
        open();
    }
    
    protected void closePressed() {
        setReturnCode(CANCEL);
        close();
   
    }
    @Override
    protected void buttonPressed(int buttonId) {
        if (IDialogConstants.CANCEL_ID == buttonId) {
            closePressed();
        } // else cancel button has a listener already
    }
    @Override
    protected Button getButton(int id) {
        if (id == IDialogConstants.CANCEL_ID) {
            return closeButton;
        }
        return super.getButton(id);
    }
    
    private Button createCloseButton(Composite parent) {
        // increment the number of columns in the button bar
        ((GridLayout) parent.getLayout()).numColumns++;
        Button button = new Button(parent, SWT.PUSH);
        button.setText(IDialogConstants.CANCEL_LABEL);
        setButtonLayoutData(button);
        button.setFont(parent.getFont());
        button.setData(new Integer(IDialogConstants.CANCEL_ID));
        button.addSelectionListener(closeListener);
        return button;
    }


    @Override
    protected void initializeBounds() {
        super.initializeBounds();
        getShell().setSize(700, 400);
        Point location = getInitialLocation(getShell().getSize());
        getShell().setLocation(location.x, location.y);
    }

    @Override
    protected void configureShell(final Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(text);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        container.setLayout(new GridLayout(3, false));

        Label lblNewLabel = new Label(container, SWT.NONE);
        lblNewLabel.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1));
        lblNewLabel.setText(Messages.getString("AdditionalPackagesDialog.feature.list.label")); //$NON-NLS-1$
        Composite featureComposite = new Composite(container, SWT.NONE);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
        featureComposite.setLayoutData(gd);
        TreeColumnLayout friendsColumnLayout = new TreeColumnLayout();
        featureComposite.setLayout(friendsColumnLayout);

        checkboxTreeViewer = new CheckboxTreeViewer(featureComposite, SWT.BORDER | SWT.FULL_SELECTION);
        checkboxTreeViewer.setSorter(new ViewerSorter() {// regroupt by class type and then by name

            /*
             * (non-Javadoc)
             *
             * @see org.eclipse.jface.viewers.ViewerComparator#category(java.lang.Object)
             */
            @Override
            public int category(Object element) {
                return element.getClass().hashCode();
            }

            /*
             * (non-Javadoc)
             *
             * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer,
             * java.lang.Object, java.lang.Object)
             */
            @Override
            public int compare(Viewer viewer, Object e1, Object e2) {
                if ((e2 instanceof ExtraFeature) && ((ExtraFeature) e2).mustBeInstalled()) {
                    return 1;
                }
                if ((e1 instanceof ExtraFeature) && ((ExtraFeature) e1).mustBeInstalled()) {
                    return -1;
                }
                return ((ExtraFeature) e1).getName().compareTo(((ExtraFeature) e2).getName());
            }
        });
        tree = checkboxTreeViewer.getTree();
        tree.setSize(400, 155);
        tree.setLinesVisible(true);

        Label lblDescription = new Label(container, SWT.NONE);
        lblDescription.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
        lblDescription.setText(Messages.getString("AdditionalPackagesDialog.description.label")); //$NON-NLS-1$
        featureDescriptionText = new StyledText(container, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL);
        GridData gd_featureDescriptionText = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
        gd_featureDescriptionText.heightHint = 61;
        featureDescriptionText.setLayoutData(gd_featureDescriptionText);

        final IObservableFactory setFactory = new IObservableFactory() {

            @Override
            public IObservable createObservable(final Object target) {
                if (target instanceof WritableSet) {
                    return (IObservableSet) target;
                }
                if (target instanceof FeatureCategory) {
                    WritableSet set = new WritableSet();
                    set.addAll(((FeatureCategory) target).getChildren());
                    return set;
                }
                return null;
            }
        };

        ObservableSetTreeContentProvider contentProvider = new ObservableSetTreeContentProvider(setFactory, null);

        checkboxTreeViewer.setContentProvider(contentProvider);
        checkboxTreeViewer.addCheckStateListener(new ICheckStateListener() {

            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
            	if(event.getElement() instanceof TalendWebServiceUpdateExtraFeature){
                	if (event.getChecked()) {
                        updateWizardModel.selectedExtraFeatures.add(((TalendWebServiceUpdateExtraFeature) event.getElement()));
                    } else {
                        updateWizardModel.selectedExtraFeatures.remove(((TalendWebServiceUpdateExtraFeature) event.getElement()));
                    }
                }
                updateInstallModulesButtonState();
            }


        });

        checkboxTreeViewer.addTreeListener(new ITreeViewerListener() {

            @Override
            public void treeExpanded(TreeExpansionEvent event) {
                dbc.updateTargets();
            }

            @Override
            public void treeCollapsed(TreeExpansionEvent event) {

            }
        });

        checkboxTreeViewer
                .setLabelProvider(new ObservableMapLabelProvider(Properties.observeEach(contentProvider.getKnownElements(),
                        new IValueProperty[] { PojoProperties.value(ExtraFeature.class, "name"), //$NON-NLS-1$
                                PojoProperties.value(ExtraFeature.class, "version") }))); //$NON-NLS-1$

        checkboxTreeViewer.setInput(updateWizardModel.availableExtraFeatures);
        initDataBindings();
        updateSelectedState(updateWizardModel.availableExtraFeatures);
        return container;
    }

    protected DataBindingContext initDataBindings() {
        dbc = new DataBindingContext();

        // bind selecting of the check boxes to the selected extra features set in the model
        dbc.bindSet(ViewersObservables.observeCheckedElements(checkboxTreeViewer, ExtraFeature.class),
                updateWizardModel.selectedExtraFeatures);

        // bind the table selection desctiption to the text field
        IObservableValue selectedFeature = ViewersObservables.observeSingleSelection(checkboxTreeViewer);
        dbc.bindValue(SWTObservables.observeText(featureDescriptionText),
                PojoObservables.observeDetailValue(selectedFeature, "description", String.class)); //$NON-NLS-1$
        // add a validator for feature selection because SetObservable does not provide any validator.
        dbc.addValidationStatusProvider(updateWizardModel.new FeatureSelectionValidator());
//        WizardPageSupport.create(this.getDialogArea(), dbc);

        // add a listener to update the description and enabled state when avaialble features are added and also
        // add them to the selected list if the must be installed
        updateWizardModel.availableExtraFeatures.addSetChangeListener(new ISetChangeListener() {

            @Override
            public void handleSetChange(SetChangeEvent arg0) {
            	updateInstallModulesButtonState();
                updateSelectedState(arg0.diff.getAdditions());
            }
        });
        return dbc;

    }

    protected void updateSelectedState(Set<ExtraFeature> features) {

        for (ExtraFeature ef : features) {
            if (ef.mustBeInstalled()) {
                updateWizardModel.selectedExtraFeatures.add(ef);
            }
        }
    }

    /**
     * DOC sgandon Comment method "updateInstallModulesButtonState".
     */
    protected void updateInstallModulesButtonState() {
    
    	if (updateWizardModel.selectedExtraFeatures.isEmpty()) {
    		installAllBtn.setEnabled(false);
    	}else {
    		 featureDescriptionText.setText(Messages.getString("AdditionalPackagesDialog.description.content")); //$NON-NLS-1$ 
     		
    		installAllBtn.setEnabled(true);
    	}
    }


    protected void addListeners() {

        installAllBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
            	 InstallNewFeatureJob installNewFeatureJob = new InstallNewFeatureJob(
                         new HashSet<ExtraFeature>(updateWizardModel.selectedExtraFeatures), updateWizardModel.getFeatureRepositories());
                 Set<ExtraFeature> selectedExtraFeatures = updateWizardModel.getSelectedExtraFeatures();
                 for (ExtraFeature feature : selectedExtraFeatures) {
                     InstallFeatureObserver.getInstance().updateInstallFeatureStatus(feature.getName(),
                             InstallFeatureObserver.FEATURE_STATUS_TO_INSTALL);
                 }
                 installNewFeatureJob.schedule();
                 close();
                 // listen to the job end so that we can ask the user to restart the Studio
                 installNewFeatureJob.addJobChangeListener(new JobChangeAdapter() {

                     @Override
                     public void done(IJobChangeEvent jobEvent) {
                         MultiStatus results = (MultiStatus) jobEvent.getResult();
                         IStatus[] installStatus = results.getChildren();
                         boolean hasAnyFailure = false;
                         boolean hasAnySuccess = false;
                         boolean hasCancel = false;
                         for (IStatus status : installStatus) {
                             if (!status.isOK()) {// ask the user to restart the Studio
                                 if (status.getSeverity() == IStatus.CANCEL) {
                                     hasCancel = true;
                                 } else {
                                     hasAnyFailure = true;
                                 }
                             } else {
                                 hasAnySuccess = true;
                             }
                         }
                         // if cancel,should do nothing,not display any pop message
                         if (hasCancel) {
                             return;
                         }
                         // display message in case of any success
                         String firstPartOfMessage = Messages.getString("UpdateStudioWizard.all.feautures.installed.successfully"); //$NON-NLS-1$
                         if (hasAnySuccess) {
                             recordSuccessInstallation();

                             if (hasAnyFailure) {
                                 firstPartOfMessage = Messages.getString("UpdateStudioWizard.some.feautures.installed.sucessfully"); //$NON-NLS-1$
                             } // else only success to keep initial message
                             final String finalMessage = firstPartOfMessage
                                     + Messages.getString("UpdateStudioWizard.do.you.want.to.restart"); //$NON-NLS-1$
                             Display.getDefault().syncExec(new Runnable() {

                                 @Override
                                 public void run() {
                                     IPreferenceStore store = PlatformUI.getPreferenceStore();
                                     // reset the last type of project set.
                                     // this will force from the Application class to reset all the perspectives
                                     store.putValue("last_started_project_type", "NO_TYPE");
                                     if (needRestart()) {
                                         boolean isOkToRestart = MessageDialog.openQuestion(getShell(),
                                                 Messages.getString("UpdateStudioWizard.install.sucessfull"), finalMessage); //$NON-NLS-1$
                                         if (isOkToRestart) {
                                             EclipseCommandLine.updateOrCreateExitDataPropertyWithCommand(EclipseCommandLine.CLEAN, null,
                                                     false);
                                             PlatformUI.getWorkbench().restart();
                                         } else {
                                             store.setValue(PreferenceKeys.NEED_OSGI_CLEAN, true); // will do clean for next
                                                                                                   // time.
                                         }
                                     }
                                 }
                             });
                         } // else only failure or canceled so do nothing cause error are reported by Eclipse

                     }

                     private void recordSuccessInstallation() {
                         final String additionalPackages = PropertiesCollectorUtil.getAdditionalPackagePreferenceNode();

                         String records = PropertiesCollectorUtil.getAdditionalPackageRecording();

                         JSONObject allRecords;
                         try {
                             allRecords = new JSONObject(records);
                         } catch (Exception e) {
                             // the value is not set, or is empty
                             allRecords = new JSONObject();
                         }

                         Map<String, ExtraFeature> featureMap = new HashMap<String, ExtraFeature>();
                         selectedExtraFeatures.stream().filter(feat -> !(feat instanceof FeatureCategory))
                                 .forEach(feat -> featureMap.put(feat.getName(), feat));
                         List<String> installedFeatures = InstallFeatureObserver.getInstance().getInstalledFeatures();

                         try {
                             JSONObject jso = allRecords.has(additionalPackages) ? (JSONObject) allRecords.get(additionalPackages)
                                     : new JSONObject();

                             for (int i = 0; i < installedFeatures.size(); i++) {
                                 ExtraFeature extraFeature = featureMap.get(installedFeatures.get(i));
                                 if (extraFeature != null) {
                                     if (extraFeature.getParentCategory() != null) {
                                         String category = extraFeature.getParentCategory().getName();
                                         JSONArray jsonArray = jso.has(category) ? (JSONArray) jso.get(category) : new JSONArray();
                                         jsonArray.put(extraFeature.getName());
                                         jso.put(category, jsonArray);
                                     } else {
                                         String name = extraFeature.getName();
                                         if (name.contains("(") && name.contains(")")) {
                                             String suffix = name.substring(name.indexOf("(") + 1, name.lastIndexOf(")"));
                                             if (suffix.matches("\\d*")) {
                                                 name = name.substring(0, name.indexOf("(")).trim();
                                             }
                                         }
                                         jso.put(name, "");
                                     }
                                 }
                             }

                             allRecords.put(additionalPackages, jso);
                         } catch (JSONException e) {
                             ExceptionHandler.log(e.getMessage());
                         }

                         PropertiesCollectorUtil.storeAdditionalPackageRecording(allRecords.toString());
                     }
                 });
                 return ;
            }

        });

    }
    private boolean needRestart() {
        boolean _needRestart = false;
        if (updateWizardModel.selectedExtraFeatures != null) {
            for (Object feature : updateWizardModel.selectedExtraFeatures) {
                if (feature instanceof ExtraFeature) {
                    if (((ExtraFeature) feature).needRestart()) {
                        _needRestart = true;
                        break;
                    }
                    if (feature instanceof ComponentNexusP2ExtraFeature) {
                        _needRestart = true;
                        break;
                    }
                }
            }
        }
        return _needRestart;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.librariesmanager.ui.dialogs.ExternalModulesInstallDialog#createButtonsForButtonBar(org.eclipse.swt
     * .widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
    	 closeButton = createCloseButton(parent);// make the cancel button the most left
        installAllBtn = createInstallButton(parent);
        updateInstallModulesButtonState();
        addListeners();// bad name but I accanont change it since it may have been overriden.

    }

    /**
     * Creates the Install button for this wizard dialog. Creates a standard (<code>SWT.PUSH</code>) button and
     * registers for its selection events. Note that the number of columns in the button bar composite is incremented.
     *
     * @param parent the parent button bar
     * @return the new Install button
     */
    private Button createInstallButton(Composite parent) {
        // increment the number of columns in the button bar
        ((GridLayout) parent.getLayout()).numColumns++;
        Button button = new Button(parent, SWT.PUSH);
        button.setText(Messages.getString("AdditionalPackagesDialog.ok"));
        setButtonLayoutData(button);
        button.setFont(parent.getFont());

        return button;
    }

    @Override
    protected void okPressed() {
        super.okPressed();
        color.dispose();
    }

}
