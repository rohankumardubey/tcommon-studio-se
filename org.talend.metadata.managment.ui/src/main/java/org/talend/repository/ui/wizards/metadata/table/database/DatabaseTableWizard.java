// ============================================================================
//
// Copyright (C) 2006-2010 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.ui.wizards.metadata.table.database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.swt.dialogs.ErrorDialogWidthDetailArea;
import org.talend.commons.utils.platform.PluginChecker;
import org.talend.core.model.metadata.IMetadataConnection;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.database.TableInfoParameters;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.update.RepositoryUpdateManager;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.cwm.helper.CatalogHelper;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.cwm.helper.ResourceHelper;
import org.talend.metadata.managment.ui.i18n.Messages;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.ui.utils.ManagerConnection;
import org.talend.repository.ui.wizards.CheckLastVersionRepositoryWizard;
import orgomg.cwm.objectmodel.core.ModelElement;
import orgomg.cwm.objectmodel.core.Package;
import orgomg.cwm.resource.relational.Catalog;
import orgomg.cwm.resource.relational.Schema;

/**
 * TableWizard present the TableForm width the MetaDataTable. Use to create a new table (need a connection to a DB).
 */

public class DatabaseTableWizard extends CheckLastVersionRepositoryWizard implements INewWizard {

    private static Logger log = Logger.getLogger(DatabaseTableWizard.class);

    private SelectorTableWizardPage selectorWizardPage;

    private DatabaseTableWizardPage tableWizardpage;

    private DatabaseTableFilterWizardPage tableFilterWizardPage;

    private DatabaseConnection temConnection;

    private boolean skipStep;

    private final ManagerConnection managerConnection;

    private Map<String, String> oldTableMap;

    private IMetadataConnection metadataConnection;

    private List<IMetadataTable> oldMetadataTable;

    private MetadataTable selectedMetadataTable;

    /* hywang add for 0017426,catches used to store the uuids and labels of old tables and columns */
    private static Map<String, String> originalColumnsMap = new HashMap<String, String>();

    private static Map<String, String> originalTablesMap = new HashMap<String, String>();

    /**
     * DOC ocarbone DatabaseTableWizard constructor comment.
     * 
     * @param workbench
     * @param idNodeDbConnection
     * @param metadataTable
     * @param existingNames
     * @param managerConnection
     */
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public DatabaseTableWizard(IWorkbench workbench, boolean creation, IRepositoryViewObject object, MetadataTable metadataTable,
            String[] existingNames, boolean forceReadOnly, ManagerConnection managerConnection,
            IMetadataConnection metadataConnection) {
        super(workbench, creation, forceReadOnly);
        this.existingNames = existingNames;
        this.managerConnection = managerConnection;
        this.metadataConnection = metadataConnection;
        setNeedsProgressMonitor(true);

        // set the repositoryObject, lock and set isRepositoryObjectEditable
        setRepositoryObject(object);
        isRepositoryObjectEditable();
        initLockStrategy();
        this.selectedMetadataTable = metadataTable;
        this.connectionItem = (ConnectionItem) object.getProperty().getItem();
        if (connectionItem != null) {
            oldTableMap = RepositoryUpdateManager.getOldTableIdAndNameMap(connectionItem, metadataTable, creation);
            oldMetadataTable = RepositoryUpdateManager.getConversionMetadataTables(connectionItem.getConnection());
            cloneBaseDataBaseConnection((DatabaseConnection) connectionItem.getConnection());
        }
        originalColumnsMap.clear();
        originalTablesMap.clear();
    }

    /**
     * DOC acer Comment method "setSkipStep".
     * 
     * @param skipStep
     */
    public void setSkipStep(boolean skipStep) {
        this.skipStep = skipStep;
    }

    /**
     * Adding the page to the wizard.
     */
    @Override
    public void addPages() {
        setWindowTitle(Messages.getString("TableWizard.windowTitle")); //$NON-NLS-1$
        setDefaultPageImageDescriptor(ImageProvider.getImageDesc(ECoreImage.METADATA_TABLE_WIZ));
        TableInfoParameters tableInfoParameters = new TableInfoParameters();
        selectorWizardPage = new SelectorTableWizardPage(connectionItem, isRepositoryObjectEditable(), tableInfoParameters,
                metadataConnection, temConnection);

        tableWizardpage = new DatabaseTableWizardPage(selectedMetadataTable, managerConnection, connectionItem,
                isRepositoryObjectEditable(), metadataConnection, temConnection);
        tableFilterWizardPage = new DatabaseTableFilterWizardPage(tableInfoParameters, this.connectionItem);
        if (creation && !skipStep) {

            tableFilterWizardPage.setDescription(Messages.getString("DatabaseTableWizard.description")); //$NON-NLS-1$
            tableFilterWizardPage.setPageComplete(true);
            selectorWizardPage
                    .setTitle(Messages.getString("TableWizardPage.titleCreate") + " \"" + connectionItem.getProperty().getLabel() //$NON-NLS-1$ //$NON-NLS-2$
                            + "\""); //$NON-NLS-1$
            selectorWizardPage.setDescription(Messages.getString("TableWizardPage.descriptionCreate")); //$NON-NLS-1$
            selectorWizardPage.setPageComplete(true);

            tableWizardpage
                    .setTitle(Messages.getString("TableWizardPage.titleCreate") + " \"" + connectionItem.getProperty().getLabel() //$NON-NLS-1$ //$NON-NLS-2$
                            + "\""); //$NON-NLS-1$
            tableWizardpage.setDescription(Messages.getString("TableWizardPage.descriptionCreate")); //$NON-NLS-1$
            tableWizardpage.setPageComplete(false);

            addPage(tableFilterWizardPage);
            addPage(selectorWizardPage);
            addPage(tableWizardpage);

        } else {
            tableWizardpage
                    .setTitle(Messages.getString("TableWizardPage.titleUpdate") + " \"" + connectionItem.getProperty().getLabel() //$NON-NLS-1$ //$NON-NLS-2$
                            + "\""); //$NON-NLS-1$
            tableWizardpage.setDescription(Messages.getString("TableWizardPage.descriptionUpdate")); //$NON-NLS-1$
            tableWizardpage.setPageComplete(false);
            addPage(tableWizardpage);
        }

    }

    /**
     * This method determine if the 'Finish' button is enable This method is called when 'Finish' button is pressed in
     * the wizard. We will create an operation and run it using wizard as execution context.
     */
    @Override
    public boolean performFinish() {
        if (tableWizardpage.isPageComplete()) {
            // temConnection will be set to model when finish
            DatabaseConnection connection = (DatabaseConnection) connectionItem.getConnection();

            /* compare the original datapackages and new datapackages */
            EList<Package> dataPackageTemConnection = temConnection.getDataPackage();
            EList<Package> dataPackageFromOrignalConnection = connection.getDataPackage();

            /*
             * The first save,to make sure all the columns and tables in temConnection can has a eResource,or it can't
             * set uuids
             */
            saveMetaData();

            if (PluginChecker.isTDQLoaded()) {
                /* generate the map contains old uuid and label for old tables and columns */
                generateOriginalColumnsMap(dataPackageFromOrignalConnection);
                Collection<Package> copyDataPackage = EcoreUtil.copyAll(dataPackageTemConnection);
                ConnectionHelper.addPackages(copyDataPackage, connection);
                /* using the old uuid to replace the old ones which on tables and columns */
                replaceUUidsForColumnsAndTables(copyDataPackage);
                /* The second save is used to save the changes of uuid */
                saveMetaData();
            }
            RepositoryUpdateManager.updateMultiSchema(connectionItem, oldMetadataTable, oldTableMap);
            closeLockStrategy();

            List<IRepositoryViewObject> list = new ArrayList<IRepositoryViewObject>();
            list.add(repositoryObject);
            CoreRuntimePlugin.getInstance().getRepositoryService().notifySQLBuilder(list);
            temConnection = null;
            return true;
        } else {
            return false;
        }
    }

    private void generateOriginalColumnsMap(Collection<? extends Package> dataPackageFromOrignalConnection) {
        for (orgomg.cwm.objectmodel.core.Package pkg : dataPackageFromOrignalConnection) {
            for (ModelElement mol : pkg.getOwnedElement()) {
                if (mol instanceof MetadataTable) {
                    MetadataTable table = (MetadataTable) mol;
                    String oldTableUuid = ResourceHelper.getUUID(table);
                    originalTablesMap.put(table.getLabel(), oldTableUuid);
                    for (ModelElement col : table.getFeature()) {
                        if (col instanceof MetadataColumn) {
                            MetadataColumn column = (MetadataColumn) col;
                            String oldColumnUuid = ResourceHelper.getUUID(column);
                            originalColumnsMap.put(column.getLabel(), oldColumnUuid);
                        }
                    }
                }
                if (mol instanceof Catalog) {
                    Catalog catlog = (Catalog) mol;
                    List<Schema> subschemas = CatalogHelper.getSchemas(catlog);
                    if (!subschemas.isEmpty()) {
                        generateOriginalColumnsMap(subschemas);
                    }
                }
            }

        }
    }

    private void replaceUUidsForColumnsAndTables(Collection<? extends Package> copyDataPackage) {
        for (orgomg.cwm.objectmodel.core.Package pkg : copyDataPackage) {
            for (ModelElement mol : pkg.getOwnedElement()) {
                if (mol instanceof MetadataTable) {
                    MetadataTable table = (MetadataTable) mol;
                    if (originalTablesMap.keySet().contains(table.getLabel())) {
                        Resource resource = table.eResource();
                        if (resource != null && resource instanceof XMLResource) {
                            XMLResource xmlResource = (XMLResource) resource;
                            xmlResource.setID(table, originalTablesMap.get(table.getLabel()));
                        }
                    }
                    for (ModelElement col : table.getFeature()) {
                        if (col instanceof MetadataColumn) {
                            MetadataColumn column = (MetadataColumn) col;
                            if (originalColumnsMap.keySet().contains(column.getLabel())) {
                                Resource resource = column.eResource();
                                if (resource != null && resource instanceof XMLResource) {
                                    XMLResource xmlResource = (XMLResource) resource;
                                    xmlResource.setID(column, originalColumnsMap.get(column.getLabel()));
                                }
                            }
                        }
                    }
                }
                if (mol instanceof Catalog) {
                    Catalog catlog = (Catalog) mol;
                    List<Schema> subschemas = CatalogHelper.getSchemas(catlog);
                    if (!subschemas.isEmpty()) {
                        replaceUUidsForColumnsAndTables(subschemas);
                    }
                }
            }

        }
    }

    /**
     * We will accept the selection in the workbench to see if we can initialize from it.
     * 
     * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
     */
    public void init(final IWorkbench workbench, final IStructuredSelection selection2) {
        this.selection = selection2;
    }

    /**
     * execute saveMetaData() on TableForm.
     */
    private void saveMetaData() {
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        try {
            factory.save(connectionItem);
        } catch (PersistenceException e) {
            String detailError = e.toString();
            new ErrorDialogWidthDetailArea(getShell(), PID, Messages.getString("CommonWizard.persistenceException"), detailError); //$NON-NLS-1$
            log.error(Messages.getString("CommonWizard.persistenceException") + "\n" + detailError); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.wizards.RepositoryWizard#performCancel()
     */
    @Override
    public boolean performCancel() {
        selectorWizardPage.performCancel();
        temConnection = null;
        return super.performCancel();
    }

    /**
     * clone a new DB connection
     */
    private void cloneBaseDataBaseConnection(DatabaseConnection connection) {
        temConnection = (DatabaseConnection) EcoreUtil.copy(connection);
        EList<Package> dataPackage = connection.getDataPackage();
        Collection<Package> newDataPackage = EcoreUtil.copyAll(dataPackage);
        ConnectionHelper.addPackages(newDataPackage, temConnection);
    }
}
