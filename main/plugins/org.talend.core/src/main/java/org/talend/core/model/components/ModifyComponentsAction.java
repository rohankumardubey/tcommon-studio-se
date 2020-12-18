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
package org.talend.core.model.components;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.conversions.RenameComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryService;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ven., 29 sept. 2006) nrousseau $
 *
 */
public class ModifyComponentsAction {

    public static void searchAndRename(ProcessItem item, String oldName, String newName) throws PersistenceException {
        searchAndModify(item, new NameComponentFilter(oldName), Arrays
                .<IComponentConversion> asList(new RenameComponentConversion(newName)));
    }

    public static void searchAndRename(Item item, ProcessType processType, String oldName, String newName)
            throws PersistenceException {
        searchAndModify(item, processType, new NameComponentFilter(oldName), Arrays
                .<IComponentConversion> asList(new RenameComponentConversion(newName)));

    }

    public static void searchAndModify(ProcessItem item, IComponentFilter filter, List<IComponentConversion> conversions)
            throws PersistenceException {
        searchAndModify(item, item.getProcess(), filter, conversions);
    }

    public static void searchAndModify(Item item, ProcessType processType, IComponentFilter filter,
            List<IComponentConversion> conversions) throws PersistenceException {
        searchAndModify(item, processType, Arrays.asList(filter), conversions);
    }

    public static void searchAndModify(Item item, ProcessType processType, List<IComponentFilter> filters,
            List<IComponentConversion> conversions) throws PersistenceException {
        searchAndModify(item, processType, filters, conversions, true);
    }
    
    public static boolean searchAndModify(Item item, ProcessType processType, List<IComponentFilter> filters,
            List<IComponentConversion> conversions, boolean autoSave) throws PersistenceException {
        if (processType == null) {
            return false;
        }
        IRepositoryService service = (IRepositoryService) GlobalServiceRegister.getDefault().getService(IRepositoryService.class);
        IProxyRepositoryFactory factory = service.getProxyRepositoryFactory();
        boolean modified = false;
        for (Object o : processType.getNode()) {
            if (searchAndModify((NodeType) o, filters, conversions)) {
                modified = true;
            }
        }
        if (modified && autoSave) {
            factory.save(item, true);
        }
        return modified;
    }
    
    public static void searchAndModify(IComponentFilter filter, List<IComponentConversion> conversions)
            throws PersistenceException, IOException, CoreException {
        IRepositoryService service = (IRepositoryService) GlobalServiceRegister.getDefault().getService(IRepositoryService.class);
        IProxyRepositoryFactory factory = service.getProxyRepositoryFactory();

        List<IRepositoryViewObject> list = factory.getAll(ERepositoryObjectType.PROCESS, true);

        for (IRepositoryViewObject mainobject : list) {
            List<IRepositoryViewObject> allVersion = factory.getAllVersion(mainobject.getId());
            for (IRepositoryViewObject object : allVersion) {
                ProcessItem item = (ProcessItem) object.getProperty().getItem();
                searchAndModify(item, filter, conversions);
            }
        }
    }

    public static boolean searchAndModify(NodeType node, IComponentFilter filter, List<IComponentConversion> conversions) {
       return searchAndModify(node, Arrays.asList(filter), conversions);
    }
    
    public static boolean searchAndModify(NodeType node, List<IComponentFilter> filters, List<IComponentConversion> conversions) {
        if (node == null || filters == null || conversions == null) {
            return false;
        }
        boolean modified = false;
        boolean toConvert = true; // Should current component be converted
        // Applying filters
        for (IComponentFilter filter : filters) {
            if (!filter.accept(node)) {
                toConvert = false;
                break;
            }
        }
        
        if (toConvert) {
            for (IComponentConversion conversion : conversions) {
                conversion.transform(node);
                modified = true;
            }
        }
        return modified;
    }

}
