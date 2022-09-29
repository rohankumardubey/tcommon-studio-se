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
package org.talend.core.ui;

import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IEditorPart;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.IService;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.general.Project;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.Information;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryEditorInput;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.repository.model.RepositoryNode;

/**
 * created by hwang on Jan 7, 2015 Detailled comment
 *
 */
public interface ITestContainerProviderService extends IService {

    public boolean isMatchedPath(IPath topLevelNodeWorkspaceRelativePath, IPath path);

    public boolean isTestContainerType(ERepositoryObjectType type);

    public boolean isTestContainerProcess(IProcess process);

    public boolean isTestContainerItem(Item item);

    public boolean hasTestCase(Property property);

    public boolean hasTestCase(Project project, Property property);

    public ProcessType getTestContainerProcess(Item item);

    public void setTestContainerProcess(ProcessType process,Item item);

    public Item getParentJobItem(Item item) throws PersistenceException;

    public IProcess getParentJobProcess(IProcess process) throws PersistenceException;

    public void switchToCurTestContainerView();

    public void updateDetect(final IProcess curJobProcess, final boolean updateAllJobs);

    public void reloadJunitsNodes(INode nc);

    public List<IFile> getTestReportFiles(ProcessItem testItem);

    public IFolder getProcessFolder(String originalJobID);

    public IFolder getProcessFolder(IProject project, String originalJobID);

    public List<ProcessItem> getAllTestContainers(ProcessItem item);

    public List<ProcessItem> getAllTestContainers(ProcessItem item, boolean latest, boolean excludeDeleted);

    public void copyDataSetFiles(IProcess process, IPath srcPath);

    public String getTestDataValue(IProcess process, String instance, String testData);

    public String getTestDataParamemter(IProcess process, String instance, String testData);

    public List<Information> getTestContainerInformations(Item item);

    public boolean isOriginalNode(INode node);

    public List<String> getTestInstances(IProcess process);

    public List<String> getTestData(IProcess process, String instanceName);

    public String getInstanceContext(IProcess process, String instance);

    public String getOriginalID(Object curNode);

    public List<IRepositoryViewObject> listExistingTestCases(String originalParentID);

    public boolean isDuplicateTestCaseOptionSelected();

    public IPath getWorkspaceTopNodePath(RepositoryNode topLevelNode);

    public void copyTestCase(Item jobItem, Item testItem, IPath path, String newName, boolean isDuplicate);

    public void copyDataFiles(Item newItem, String sourceNodeid);

    public String getDataFileSourcePath(Item item, String dataValue);

    public void renameConnection(INode node, boolean isOpen);

    public boolean isTestCaseComponent(IComponent component);

    public List<IResource> getDataFiles(Item item);

    public IRepositoryEditorInput createTestCaseEditorInput(ProcessItem junitItem, boolean load, String originalJobID,
            List<INode> testNodes, Boolean lastVersion, Boolean readonly) throws PersistenceException;

    public ProcessType loadRemoteTestContainer(Item item, IProcess2 loadedProcess, Property loadProperty);

    public ERepositoryObjectType getTestCaseObjectType();

    public String getParentVersion(Item item);

    public List<ProcessItem> getTestContainersByVersion(ProcessItem item);

    boolean isTestContainerEditor(IEditorPart editor);

    Set<ModuleNeeded> getAllJobTestcaseModules(ProcessItem item) throws PersistenceException;

    public void deleteDataFiles(Object deleteObj);

    public List<NodeType> getOriginalNodes(ProcessType process);

    public static ITestContainerProviderService get() {
        if (GlobalServiceRegister.getDefault().isServiceRegistered(ITestContainerProviderService.class)) {
            return GlobalServiceRegister.getDefault().getService(ITestContainerProviderService.class);
        }
        return null;
    }

}
