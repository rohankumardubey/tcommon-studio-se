// ============================================================================
//
// Copyright (C) 2006-2022 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.core.model.metadata.builder.database.jdbc;

import org.talend.core.database.EDatabaseTypeName;

public enum EDBTypeProductNameMapping {

    Mysql(EDatabaseTypeName.MYSQL, "Mysql", "mysql_id");

    EDatabaseTypeName type;

    String productName;

    String mappingID;

    EDBTypeProductNameMapping(EDatabaseTypeName type, String productName, String mappingID) {
        this.type = type;
        this.productName = productName;
        this.mappingID = mappingID;
    }

    public static EDBTypeProductNameMapping findTypeByProductName(String productName) {
        if (productName == null) {
            return null;
        }

        for (EDBTypeProductNameMapping type : EDBTypeProductNameMapping.values()) {
            if (type.productName.equalsIgnoreCase(productName)) {
                return type;
            }
        }
        return null;
    }

    public static EDBTypeProductNameMapping findTypeByMappingID(String mappingID) {
        if (mappingID == null) {
            return null;
        }

        for (EDBTypeProductNameMapping type : EDBTypeProductNameMapping.values()) {
            if (type.mappingID.equalsIgnoreCase(mappingID)) {
                return type;
            }
        }

        return null;
    }

}
