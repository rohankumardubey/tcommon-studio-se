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

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.database.conn.version.EDatabaseVersion4Drivers;
import org.talend.core.model.metadata.IMetadataConnection;

public class ExtractorFactory {

    public static IUrlDbNameExtractor getExtractorInstance(DatabaseMetaData dbMetadata,
            IMetadataConnection metadataConnection) {
        if (dbMetadata == null) {
            return null;
        }
        if (!checkIsGenericJDBCType(metadataConnection)) {
            return null;
        }
        IUrlDbNameExtractor theExtractor = null;
        String databaseProductName = null;
        try {
            databaseProductName = dbMetadata.getDatabaseProductName();
            theExtractor = createExtractorByDP(databaseProductName);
            initExtractor(theExtractor, dbMetadata, metadataConnection);
        } catch (SQLException e) {
        }
        theExtractor = createExtractorByMappingID(metadataConnection);
        initExtractor(theExtractor, dbMetadata, metadataConnection);
        return null;
    }

    private static boolean checkIsGenericJDBCType(IMetadataConnection metadataConnection) {
        if (metadataConnection == null) {
            return false;
        }
        return EDatabaseTypeName.GENERAL_JDBC.getXMLType().equals(metadataConnection.getDbType());
    }

    protected static void initExtractor(IUrlDbNameExtractor theExtractor, DatabaseMetaData dbMetadata,
            IMetadataConnection metadataConnection) {
        if (theExtractor != null) {
            initURL(theExtractor, dbMetadata, metadataConnection);
            initDriverName(theExtractor, dbMetadata, metadataConnection);
        }
    }

    private static void initDriverName(IUrlDbNameExtractor theExtractor, DatabaseMetaData dbMetadata,
            IMetadataConnection metadataConnection) {
        try {
            theExtractor.setDriverName(dbMetadata.getDriverName());
        } catch (SQLException e) {
        }
        if (theExtractor.getDriverName() == null && metadataConnection != null) {
            theExtractor.setDriverName(metadataConnection.getDriverJarPath());
        }

    }

    protected static void initURL(IUrlDbNameExtractor theExtractor, DatabaseMetaData dbMetadata,
            IMetadataConnection metadataConnection) {
        try {
            theExtractor.setUrl(dbMetadata.getURL());
        } catch (SQLException e) {
        }
        if (theExtractor.getUrl() == null && metadataConnection != null) {
            theExtractor.setUrl(metadataConnection.getUrl());
        }
    }

    private static IUrlDbNameExtractor createExtractorByDP(String databaseProductName) {
        if (databaseProductName == null) {
            return null;
        }
        EDBTypeProductNameMapping dbType = EDBTypeProductNameMapping.findTypeByProductName(databaseProductName);
        return createExtractor(dbType);
    }

    private static IUrlDbNameExtractor createExtractor(EDBTypeProductNameMapping dbType) {
        if (dbType == null) {
            return null;
        }
        switch (dbType) {
        case Mysql:
            return new MysqlExtractor();
        }
        return null;
    }

    private static IUrlDbNameExtractor createExtractorByMappingID(IMetadataConnection metadataConnection) {
        if (metadataConnection == null) {
            return null;
        }
        String mappingID = metadataConnection.getMapping();
        if (mappingID == null) {
            return null;
        }
        EDBTypeProductNameMapping dbType = EDBTypeProductNameMapping.findTypeByMappingID(mappingID);
        return createExtractor(dbType);
    }

}
