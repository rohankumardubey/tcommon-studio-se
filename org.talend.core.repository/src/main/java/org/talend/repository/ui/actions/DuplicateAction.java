// ============================================================================
//
// Copyright (C) 2006-2011 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.ui.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.talend.commons.exception.BusinessException;
import org.talend.commons.exception.LoginException;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.exception.SystemException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.RepositoryManager;
import org.talend.core.repository.i18n.Messages;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.utils.KeywordsValidator;
import org.talend.designer.codegen.ICodeGeneratorService;
import org.talend.designer.core.ICamelDesignerCoreService;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryWorkUnit;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.IRepositoryService;
import org.talend.repository.model.RepositoryConstants;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;
import org.talend.repository.model.actions.CopyObjectAction;
import org.talend.repository.ui.dialog.PastSelectorDialog;

/**
 * DOC zwang class global comment. Detailled comment
 */
public class DuplicateAction extends AContextualAction {

    private RepositoryNode sourceNode = null;

    private IStructuredSelection selection = null;

    IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

    public DuplicateAction() {
        super();
        this.setText(Messages.getString("DuplicateAction.thisText.duplicate")); //$NON-NLS-1$
        this.setImageDescriptor(ImageProvider.getImageDesc(EImage.DUPLICATE_ICON));
    }

    public void init(TreeViewer viewer, IStructuredSelection selection) {

        boolean canWork = true;

        RepositoryNode node = (RepositoryNode) selection.getFirstElement();

        if (selection.isEmpty()) {
            setEnabled(false);
            return;
        }

        this.sourceNode = node;
        this.selection = selection;
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        if (factory.isUserReadOnlyOnCurrentProject() || !ProjectManager.getInstance().isInCurrentMainProject(node)) {
            canWork = false;
        }

        if (selection != null) {
            if (((StructuredSelection) selection).toArray().length > 1) {
                canWork = false;
            } else if (((StructuredSelection) selection).toArray().length == 1) {
                Object obj = ((StructuredSelection) selection).toList().get(0);
                if (canWork) {
                    RepositoryNode sourceNode = (RepositoryNode) obj;
                    if (!CopyObjectAction.getInstance().validateAction(sourceNode, null)) {
                        canWork = false;
                    } else if (node.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.JOB_DOC
                            || node.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.JOBLET_DOC) {
                        canWork = false;
                    } else if (node.getContentType() == ERepositoryObjectType.JOBS
                            || node.getContentType() == ERepositoryObjectType.JOBLETS
                            || node.getContentType() == ERepositoryObjectType.GENERATED
                            || node.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.JOB_DOC
                            || node.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.JOBLET_DOC) {
                        canWork = false;
                    } else if (node.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.METADATA_CON_CDC
                            || node.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.METADATA_CON_QUERY
                            || node.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.METADATA_CON_TABLE
                            || node.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.METADATA_CON_VIEW
                            || node.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.METADATA_CON_SYNONYM
                            || node.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.METADATA_SAP_FUNCTION
                            || node.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.METADATA_SALESFORCE_MODULE
                            || node.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.METADATA_SAP_IDOC
                            || node.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.MDM_CONCEPT) {
                        canWork = false;
                    }
                }
            }
        } else {
            canWork = false;
        }

        setEnabled(canWork);
    }

    @Override
    protected void doRun() {

        if (sourceNode == null) {
            return;
        }

        Property property = (Property) sourceNode.getObject().getProperty();

        Property updatedProperty = null;
        try {
            updatedProperty = ProxyRepositoryFactory.getInstance()
                    .getLastVersion(new Project(ProjectManager.getInstance().getProject(property.getItem())), property.getId())
                    .getProperty();
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        // update the property of the node repository object
        // sourceNode.getObject().setProperty(updatedProperty);

        String initNameValue = "Copy_of_" + sourceNode.getObject().getProperty().getItem().getProperty().getLabel(); //$NON-NLS-1$

        CopyObjectAction copyObjectAction = CopyObjectAction.getInstance();

        final TreeSelection selectionInClipboard = (TreeSelection) selection;

        // see feature 0001563: Display "Save job" prompt when "copy" action for a job is requested.
        promptForSavingIfNecessary((RepositoryNode) selection.getFirstElement());

        String jobNameValue = null;

        try {
            jobNameValue = getDuplicateName(initNameValue, selectionInClipboard);
        } catch (BusinessException e) {
            jobNameValue = ""; //$NON-NLS-1$
        }

        InputDialog jobNewNameDialog = new InputDialog(null, Messages.getString("DuplicateAction.input.title"), //$NON-NLS-1$
                Messages.getString("DuplicateAction.input.message"), jobNameValue, new IInputValidator() { //$NON-NLS-1$

                    public String isValid(String newText) {
                        return validJobName(newText, selectionInClipboard);
                    }

                });

        if (jobNewNameDialog.open() != Dialog.OK) {
            return;
        }

        String jobNewName = jobNewNameDialog.getValue();

        createOperation(jobNewName, sourceNode, copyObjectAction, selectionInClipboard);

        RepositoryManager.refreshCreatedNode(sourceNode.getObjectType());

    }

    public String getDuplicateName(String value, final TreeSelection selectionInClipboard) throws BusinessException {

        if (validJobName(value, selectionInClipboard) == null) {
            return value;
        } else {
            char j = 'a';
            String temp = value;
            while (validJobName(temp, selectionInClipboard) != null) {
                if (j > 'z') {
                    throw new BusinessException(Messages.getString("DuplicateAction.cannotGenerateItem")); //$NON-NLS-1$
                }
                temp = value + "_" + (j++) + ""; //$NON-NLS-1$ //$NON-NLS-2$
            }
            return temp;
        }
    }

    /**
     * 
     * DOC YeXiaowei Comment method "isValid".
     * 
     * @param itemName
     * @param selectionInClipboard
     * @return null means valid, other means some error exist
     */
    private String validJobName(String itemName, TreeSelection selectionInClipboard) {

        IRepositoryService service = (IRepositoryService) GlobalServiceRegister.getDefault().getService(IRepositoryService.class);
        IProxyRepositoryFactory repositoryFactory = service.getProxyRepositoryFactory();
        if (itemName.length() == 0) {
            return Messages.getString("DuplicateAction.NameEmptyError"); //$NON-NLS-1$
        } else if (!Pattern.matches(RepositoryConstants.getPattern(((RepositoryNode) selectionInClipboard.toArray()[0])
                .getObject().getRepositoryObjectType()), itemName)) {
            /*
             * maybe Messages.getString("PropertiesWizardPage.KeywordsError")
             */
            return Messages.getString("DuplicateAction.NameFormatError"); //$NON-NLS-1$
        } else {
            try {
                Item testNewItem = createNewItem();
                if (testNewItem != null) {
                    if (!repositoryFactory.isNameAvailable(testNewItem, itemName)) {
                        return Messages.getString("DuplicateAction.ItemExistsError"); //$NON-NLS-1$
                    }
                }
            } catch (PersistenceException e) {
                return Messages.getString("DuplicateAction.ItemExistsError"); //$NON-NLS-1$
            }
            // see bug 0004157: Using specific name for (main) tream
            if (isKeyword(itemName)) {
                return Messages.getString("DuplicateAction.KeywordsError"); //$NON-NLS-1$
            }
        }

        return null;
    }

    /**
     * DOC hcw Comment method "isKeyword".
     * 
     * @param itemName
     * @return
     */
    private boolean isKeyword(String itemName) {
        ERepositoryObjectType itemType = sourceNode.getObjectType();
        ERepositoryObjectType[] types = { ERepositoryObjectType.PROCESS, ERepositoryObjectType.ROUTINES,
                ERepositoryObjectType.JOBS, ERepositoryObjectType.JOBLET, ERepositoryObjectType.JOBLETS,
                ERepositoryObjectType.JOB_SCRIPT };
        List<ERepositoryObjectType> arraysList = Arrays.asList(types);
        List<ERepositoryObjectType> typeList = new ArrayList<ERepositoryObjectType>();
        addExtensionRepositoryNodes(typeList);
        typeList.addAll(arraysList);
        if (typeList.contains(itemType)) {
            return KeywordsValidator.isKeyword(itemName);
        }
        return false;
    }

    private void addExtensionRepositoryNodes(List<ERepositoryObjectType> arraysList) {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IConfigurationElement[] configurationElements = registry
                .getConfigurationElementsFor("org.talend.core.repository.repository_node_provider");
        for (int i = 0; i < configurationElements.length; i++) {
            IConfigurationElement element = configurationElements[i];
            String type = element.getAttribute("type");
            ERepositoryObjectType repositoryNodeType = (ERepositoryObjectType) ERepositoryObjectType.valueOf(
                    ERepositoryObjectType.class, type);
            if (repositoryNodeType != null) {
                arraysList.add(repositoryNodeType);
            }
        }
    }

    private Item createNewItem() {

        ERepositoryObjectType repositoryType = sourceNode.getObjectType();

        Item item = null;
        if (repositoryType != null) {
            if (repositoryType != null) {
                if (repositoryType == ERepositoryObjectType.BUSINESS_PROCESS) {
                    item = PropertiesFactory.eINSTANCE.createBusinessProcessItem();
                } else if (repositoryType == ERepositoryObjectType.CONTEXT) {
                    item = PropertiesFactory.eINSTANCE.createContextItem();
                } else if (repositoryType == ERepositoryObjectType.DOCUMENTATION) {
                    item = PropertiesFactory.eINSTANCE.createDocumentationItem();
                } else if (repositoryType == ERepositoryObjectType.JOBLET) {
                    item = PropertiesFactory.eINSTANCE.createJobletProcessItem();
                } else if (repositoryType == ERepositoryObjectType.METADATA_CONNECTIONS) {
                    item = PropertiesFactory.eINSTANCE.createDatabaseConnectionItem();
                } else if (repositoryType == ERepositoryObjectType.METADATA_FILE_DELIMITED) {
                    item = PropertiesFactory.eINSTANCE.createDelimitedFileConnectionItem();
                } else if (repositoryType == ERepositoryObjectType.METADATA_FILE_EBCDIC) {
                    item = PropertiesFactory.eINSTANCE.createEbcdicConnectionItem();
                } else if (repositoryType == ERepositoryObjectType.METADATA_FILE_EXCEL) {
                    item = PropertiesFactory.eINSTANCE.createExcelFileConnectionItem();
                } else if (repositoryType == ERepositoryObjectType.METADATA_FILE_HL7) {
                    item = PropertiesFactory.eINSTANCE.createHL7ConnectionItem();
                } else if (repositoryType == ERepositoryObjectType.METADATA_FILE_LDIF) {
                    item = PropertiesFactory.eINSTANCE.createLdifFileConnectionItem();
                } else if (repositoryType == ERepositoryObjectType.METADATA_FILE_POSITIONAL) {
                    item = PropertiesFactory.eINSTANCE.createPositionalFileConnectionItem();
                } else if (repositoryType == ERepositoryObjectType.METADATA_FILE_LINKRULES) {
                    item = PropertiesFactory.eINSTANCE.createLinkRulesItem();
                } else if (repositoryType == ERepositoryObjectType.METADATA_FILE_REGEXP) {
                    item = PropertiesFactory.eINSTANCE.createRegExFileConnectionItem();
                } else if (repositoryType == ERepositoryObjectType.METADATA_FILE_RULES) {
                    item = PropertiesFactory.eINSTANCE.createRulesItem();
                } else if (repositoryType == ERepositoryObjectType.METADATA_FILE_XML) {
                    item = PropertiesFactory.eINSTANCE.createXmlFileConnectionItem();
                } else if (repositoryType == ERepositoryObjectType.METADATA_GENERIC_SCHEMA) {
                    item = PropertiesFactory.eINSTANCE.createGenericSchemaConnectionItem();
                } else if (repositoryType == ERepositoryObjectType.METADATA_LDAP_SCHEMA) {
                    item = PropertiesFactory.eINSTANCE.createLDAPSchemaConnectionItem();
                } else if (repositoryType == ERepositoryObjectType.METADATA_MDMCONNECTION) {
                    item = PropertiesFactory.eINSTANCE.createMDMConnectionItem();
                } else if (repositoryType == ERepositoryObjectType.METADATA_SALESFORCE_SCHEMA) {
                    item = PropertiesFactory.eINSTANCE.createSalesforceSchemaConnectionItem();
                } else if (repositoryType == ERepositoryObjectType.METADATA_SAPCONNECTIONS) {
                    item = PropertiesFactory.eINSTANCE.createSAPConnectionItem();
                } else if (repositoryType == ERepositoryObjectType.METADATA_WSDL_SCHEMA) {
                    item = PropertiesFactory.eINSTANCE.createWSDLSchemaConnectionItem();
                } else if (repositoryType == ERepositoryObjectType.PROCESS) {
                    item = PropertiesFactory.eINSTANCE.createProcessItem();
                } else if (repositoryType == ERepositoryObjectType.ROUTINES) {
                    item = PropertiesFactory.eINSTANCE.createRoutineItem();
                } else if (repositoryType == ERepositoryObjectType.JOB_SCRIPT) {
                    item = PropertiesFactory.eINSTANCE.createJobScriptItem();
                } else if (repositoryType == ERepositoryObjectType.SNIPPETS) {
                    item = PropertiesFactory.eINSTANCE.createSnippetItem();
                } else if (repositoryType == ERepositoryObjectType.SQLPATTERNS) {
                    item = PropertiesFactory.eINSTANCE.createSQLPatternItem();
                } else if (repositoryType == ERepositoryObjectType.SVG_BUSINESS_PROCESS) {
                    item = PropertiesFactory.eINSTANCE.createSVGBusinessProcessItem();
                } else if (repositoryType == ERepositoryObjectType.METADATA_EDIFACT) {
                    item = PropertiesFactory.eINSTANCE.createEDIFACTConnectionItem();
                }
                if (GlobalServiceRegister.getDefault().isServiceRegistered(ICamelDesignerCoreService.class)) {
                    ICamelDesignerCoreService service = (ICamelDesignerCoreService) GlobalServiceRegister.getDefault()
                            .getService(ICamelDesignerCoreService.class);
                    item = service.createNewCamelItem(repositoryType);
                }

            }
        }
        if (item != null) {
            Property property = PropertiesFactory.eINSTANCE.createProperty();
            item.setProperty(property);
        }
        return item;
    }

    private void createOperation(String newJobName, RepositoryNode target, CopyObjectAction copyObjectAction,
            TreeSelection selectionInClipboard) {

        Object currentSource = selectionInClipboard.toArray()[0];
        try {
            IPath path = RepositoryNodeUtilities.getPath(target);

            if (((RepositoryNode) currentSource).getType().equals(ENodeType.REPOSITORY_ELEMENT)) {
                Item originalItem = ((RepositoryNode) currentSource).getObject().getProperty().getItem();
                List<IRepositoryViewObject> allVersion = factory.getAllVersion(originalItem.getProperty().getId());
                for (IRepositoryViewObject obj : allVersion) {
                    if (obj.getVersion().equals(originalItem.getProperty().getVersion())) {
                        originalItem = obj.getProperty().getItem();
                        break;
                    }
                }

                if (allVersion.size() == 1) {
                    duplicateSingleVersionItem(originalItem, path, newJobName);

                } else if (allVersion.size() > 1) {
                    PastSelectorDialog dialog = new PastSelectorDialog(Display.getCurrent().getActiveShell(), allVersion,
                            sourceNode);
                    if (dialog.open() == Window.OK) {
                        Set<IRepositoryViewObject> selectedVersionItems = dialog.getSelectedVersionItems();
                        String id = null;
                        String label = null;
                        boolean isfirst = true;
                        boolean needSys = true;
                        for (IRepositoryViewObject object : selectedVersionItems) {
                            Item selectedItem = object.getProperty().getItem();
                            final Item copy = factory.copy(selectedItem, path);
                            if (isfirst) {
                                id = copy.getProperty().getId();
                                label = copy.getProperty().getLabel();
                                isfirst = false;
                            }
                            copy.getProperty().setId(id);
                            copy.getProperty().setLabel(newJobName);
                            if (needSys && originalItem instanceof RoutineItem) {
                                String lastestVersion = getLastestVersion(selectedVersionItems);
                                if (lastestVersion.equals(copy.getProperty().getVersion())) {
                                    synDuplicatedRoutine((RoutineItem) copy);
                                    needSys = false;
                                }
                            }

                            final RepositoryWorkUnit<Object> workUnit = new RepositoryWorkUnit<Object>(this.getText(), this) {

                                @Override
                                protected void run() throws LoginException, PersistenceException {
                                    if (copy instanceof ProcessItem) {
                                        RelationshipItemBuilder.getInstance().addOrUpdateItem((ProcessItem) copy);
                                    }
                                    factory.save(copy);
                                }
                            };
                            workUnit.setAvoidUnloadResources(true);
                            factory.executeRepositoryWorkUnit(workUnit);
                        }
                    }
                }
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    private void duplicateSingleVersionItem(final Item item, final IPath path, final String newName) {

        RepositoryWorkUnit<Object> rwu = new RepositoryWorkUnit<Object>(this.getText(), this) {

            @Override
            protected void run() throws LoginException, PersistenceException {
                try {
                    final Item newItem = factory.copy(item, path, true);

                    newItem.getProperty().setLabel(newName);
                    // qli modified to fix the bug 5400 and 6185.
                    if (newItem instanceof RoutineItem) {
                        synDuplicatedRoutine((RoutineItem) newItem);
                    }// end
                    ICamelDesignerCoreService service = null;
                    if (GlobalServiceRegister.getDefault().isServiceRegistered(ICamelDesignerCoreService.class)) {
                        service = (ICamelDesignerCoreService) GlobalServiceRegister.getDefault().getService(
                                ICamelDesignerCoreService.class);
                    }
                    if (service != null && service.isInstanceofCamelBeans(item)) {
                        // for camel
                        synDuplicatedBean(newItem);
                    }
                    if (newItem instanceof ProcessItem) {
                        RelationshipItemBuilder.getInstance().addOrUpdateItem((ProcessItem) newItem);
                    }

                    if (newItem instanceof ConnectionItem) {
                        ConnectionItem connectionItem = (ConnectionItem) newItem;
                        connectionItem.getConnection().getSupplierDependency().clear();
                    }
                    factory.save(newItem);
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
            }
        };
        rwu.setAvoidUnloadResources(true);
        factory.executeRepositoryWorkUnit(rwu);

    }

    private String getLastestVersion(Set<IRepositoryViewObject> set) {
        if (set.isEmpty()) {
            return null;
        }
        String version = null;
        for (IRepositoryViewObject obj : set) {
            String curVersion = obj.getProperty().getVersion();
            if (version == null) {
                version = curVersion;
            } else {
                Double dVersion = Double.valueOf(version);
                Double dCurVersion = Double.valueOf(curVersion);
                if (dCurVersion > dVersion) {
                    version = curVersion;
                }
            }
        }
        return version;
    }

    private void synDuplicatedRoutine(RoutineItem item) {
        ICodeGeneratorService codeGenService = (ICodeGeneratorService) GlobalServiceRegister.getDefault().getService(
                ICodeGeneratorService.class);
        if (codeGenService != null) {
            codeGenService.createRoutineSynchronizer().renameRoutineClass((RoutineItem) item);
            try {
                codeGenService.createRoutineSynchronizer().syncRoutine((RoutineItem) item, true);
            } catch (SystemException e) {
                ExceptionHandler.process(e);
            }
        }
    }

    private void synDuplicatedBean(Item item) {
        ICodeGeneratorService codeGenService = (ICodeGeneratorService) GlobalServiceRegister.getDefault().getService(
                ICodeGeneratorService.class);
        if (codeGenService != null) {
            codeGenService.createCamelBeanSynchronizer().renameBeanClass(item);
            try {
                codeGenService.createCamelBeanSynchronizer().syncBean(item, true);
            } catch (SystemException e) {
                ExceptionHandler.process(e);
            }
        }
    }

    /**
     * 
     * DOC YeXiaowei Comment method "resetProcessVersion".
     * 
     * @return
     */
    protected boolean resetProcessVersion() {
        return false;
    }
}
