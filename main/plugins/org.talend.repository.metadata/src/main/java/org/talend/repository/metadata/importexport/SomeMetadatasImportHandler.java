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
package org.talend.repository.metadata.importexport;

import org.talend.core.database.conn.template.EDatabaseConnTemplate;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.items.importexport.handlers.imports.MetadataConnectionImportHandler;
import org.talend.repository.items.importexport.handlers.model.ImportItem;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class SomeMetadatasImportHandler extends MetadataConnectionImportHandler {

    /**
     *
     * DOC ggu SomeMetadatasImportHandler constructor comment.
     */
    public SomeMetadatasImportHandler() {
        super();
    }
    
    @Override
    public boolean valid(ImportItem importItem) {
        Item item = importItem.getItem();
        ERepositoryObjectType eType = importItem.getProperty() == null ? null: importItem.getRepositoryType();
        if (ERepositoryObjectType.METADATA_CONNECTIONS == eType && item instanceof DatabaseConnectionItem) {
            DatabaseConnectionItem dbconn = (DatabaseConnectionItem) item;
            String databaseType = dbconn.getTypeName(); //
            
            EDatabaseConnTemplate template = EDatabaseConnTemplate.indexOfTemplate(databaseType);
            if(template == null) {
                return false;
            }
        }
        
        return super.valid(importItem);
    }
}
