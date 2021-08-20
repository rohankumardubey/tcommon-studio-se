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
package org.talend.migration;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.ui.ITestContainerCoreService;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.repository.model.IRepositoryService;

/**
 * DOC jding  class global comment. Detailled comment
 */
public class MigrationReportRecorder {

    private IProjectMigrationTask task;

    private MigrationOperationType operationType;

    private Item item;

    private NodeType node;

    private String paramName;

    private String oldValue;

    private String newValue;

    private String detailMessage;

    public enum MigrationOperationType {
        ADD,
        MODIFY,
        DELETE
    }

    public MigrationReportRecorder(IProjectMigrationTask task, Item item) {
        super();
        this.task = task;
        this.item = item;
    }

    public MigrationReportRecorder(IProjectMigrationTask task, MigrationOperationType operationType, Item item, NodeType node,
            String paramName, String oldValue, String newValue) {
        super();
        this.task = task;
        this.operationType = operationType;
        this.item = item;
        this.node = node;
        this.paramName = paramName;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public MigrationReportRecorder(IProjectMigrationTask task, Item item, String detailMessage) {
        super();
        this.task = task;
        this.item = item;
        this.detailMessage = detailMessage;
    }

    public String getTaskClassName() {
        return task.getClass().getSimpleName();
    }

    public String getTaskDescription() {
        String description = "";
        if (StringUtils.isNotBlank(task.getDescription())) {
            description = task.getDescription();
        }
        return description;
    }

    public String getItemType() {
        String type = "";
        ERepositoryObjectType itemType = ERepositoryObjectType.getItemType(item);
        if (itemType != null) {
            if (ERepositoryObjectType.getAllTypesOfTestContainer().contains(itemType)) {
                Item parentJobItem = getTestCaseParentJobItem(item);
                if (parentJobItem != null) {
                    ERepositoryObjectType parentJobType = ERepositoryObjectType.getItemType(parentJobItem);
                    if (parentJobType != null) {
                        String parentTypePath = getCompleteObjectTypePath(parentJobType);
                        if (StringUtils.isNotBlank(parentTypePath)) {
                            type = parentTypePath + "/";
                        }
                    }
                }
                type += itemType;
            } else {
                type = getCompleteObjectTypePath(itemType);
            }
        }
        return type;
    }

    public String getItemPath() {
        String path = "";
        StringBuffer buffer = new StringBuffer();
        ERepositoryObjectType itemType = ERepositoryObjectType.getItemType(item);

        if (ERepositoryObjectType.getAllTypesOfTestContainer().contains(itemType)) {
            StringBuffer testcaseBuffer = new StringBuffer();
            Item parentJobItem = getTestCaseParentJobItem(item);
            if (parentJobItem != null) {
                if (parentJobItem.getState() != null && StringUtils.isNotBlank(parentJobItem.getState().getPath())) {
                    testcaseBuffer.append(parentJobItem.getState().getPath()).append("/");
                }
                testcaseBuffer.append(parentJobItem.getProperty() != null ? parentJobItem.getProperty().getLabel() : "");
                if (StringUtils.isNotBlank(testcaseBuffer.toString())) {
                    buffer.append(testcaseBuffer.toString()).append("/");
                }
            }
        } else {
            if (item.getState() != null && StringUtils.isNotBlank(item.getState().getPath())) {
                buffer.append(item.getState().getPath()).append("/");
            }
        }

        Property property = item.getProperty();
        if (property != null) {
            buffer.append(property.getLabel() + "_" + property.getVersion());
        }
        path = buffer.toString();
        return path;
    }

    private Item getTestCaseParentJobItem(Item testcaseItem) {
        Item parentJobItem = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(ITestContainerCoreService.class)) {
            ITestContainerCoreService testcaseService = GlobalServiceRegister.getDefault()
                    .getService(ITestContainerCoreService.class);
            if (testcaseService != null) {
                try {
                    parentJobItem = testcaseService.getParentJobItem(item);
                } catch (PersistenceException e) {
                    ExceptionHandler.process(e);
                }
            }
        }
        return parentJobItem;
    }

    private String getCompleteObjectTypePath(ERepositoryObjectType itemType) {
        ERepositoryObjectType rootItemType = itemType;
        if (ERepositoryObjectType.JDBC != null && ERepositoryObjectType.JDBC.equals(rootItemType)) {
            rootItemType = ERepositoryObjectType.METADATA_CONNECTIONS;
        }
        List<String> typeLabels = new ArrayList<String>();
        findOutCompleteTypePath(rootItemType, typeLabels);
        if (ERepositoryObjectType.PROCESS != null && ERepositoryObjectType.PROCESS.equals(rootItemType)) {
            IRepositoryService repositoryService = IRepositoryService.get();
            if (repositoryService != null) {
                String standardNodeLabel = repositoryService.getStandardNodeLabel();
                if (StringUtils.isNotBlank(standardNodeLabel)) {
                    typeLabels.add(standardNodeLabel);
                }
            }
        }
        StringBuffer buffer = new StringBuffer();
        if (!typeLabels.isEmpty()) {
            for (int i = 0; i < typeLabels.size(); i++) {
                if (i != 0) {
                    buffer.append("/");
                }
                buffer.append(typeLabels.get(i));
            }
        }
        return buffer.toString();
    }

    private void findOutCompleteTypePath(ERepositoryObjectType type, List<String> typeLabels) {
        ERepositoryObjectType parentType = ERepositoryObjectType.findParentType(type);
        if (parentType != null) {
            findOutCompleteTypePath(parentType, typeLabels);
        }
        typeLabels.add(type.getLabel());
    }

    public String getMigrationDetails() {
        String details = detailMessage;
        if (StringUtils.isNotBlank(detailMessage)) {
            return details;
        }

        if (operationType == null || StringUtils.isBlank(paramName)) {
            details = getTaskClassName() + " task is applied";
            return details;
        }

        StringBuffer detailBuffer = new StringBuffer();
        if (node != null) {
            // migration for node, e.g. tRESTClient component "tRESTClient_2":
            detailBuffer.append(node.getComponentName()).append(" component ");
            ElementParameterType uniqueName = ComponentUtilities.getNodeProperty(node, "UNIQUE_NAME");
            detailBuffer.append(TalendQuoteUtils.addQuotes(uniqueName.getValue())).append(":");
        } else {
            // migration for item, e.g. context item "testContext":
            detailBuffer.append(getItemType() + " item ")
                    .append(TalendQuoteUtils.addQuotes(item.getProperty().getLabel())).append(":");
        }

        detailBuffer.append(paramName).append(" was ");
        switch (operationType) {
        case ADD:
            detailBuffer.append("added");
            if (StringUtils.isNotBlank(newValue)) {
                detailBuffer.append(" with ").append(newValue);
            }
            break;
        case MODIFY:
            detailBuffer.append("changed");
            if (StringUtils.isNotBlank(oldValue)) {
                detailBuffer.append(" from ").append(oldValue);
            }
            if (StringUtils.isNotBlank(newValue)) {
                detailBuffer.append(" to ").append(newValue);
            }
            break;
        case DELETE:
            detailBuffer.append("deleted");
            break;
        default:
            break;
        }
        details = detailBuffer.toString();

        return details;
    }

    /**
     * Getter for task.
     * 
     * @return the task
     */
    public IProjectMigrationTask getTask() {
        return task;
    }

    /**
     * Sets the task.
     * 
     * @param task the task to set
     */
    public void setTask(IProjectMigrationTask task) {
        this.task = task;
    }

    /**
     * Sets the operationType.
     * 
     * @param operationType the operationType to set
     */
    public void setOperationType(MigrationOperationType operationType) {
        this.operationType = operationType;
    }

    /**
     * Getter for item.
     * 
     * @return the item
     */
    public Item getItem() {
        return item;
    }

    /**
     * Sets the item.
     * 
     * @param item the item to set
     */
    public void setItem(Item item) {
        this.item = item;
    }

    /**
     * Sets the node.
     * 
     * @param node the node to set
     */
    public void setNode(NodeType node) {
        this.node = node;
    }

    /**
     * Sets the paramName.
     * 
     * @param paramName the paramName to set
     */
    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    /**
     * Sets the oldValue.
     * 
     * @param oldValue the oldValue to set
     */
    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    /**
     * Sets the newValue.
     * 
     * @param newValue the newValue to set
     */
    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

}
