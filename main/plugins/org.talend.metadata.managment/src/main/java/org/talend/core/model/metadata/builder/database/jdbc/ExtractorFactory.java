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
import org.talend.core.model.metadata.IMetadataConnection;
import org.talend.core.model.metadata.builder.ConvertionHelper;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.cwm.helper.TaggedValueHelper;

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
            return theExtractor;
        } catch (SQLException e) {
        }
        return null;
    }
    
    public static IUrlDbNameExtractor getExtractorInstance(ConnectionItem connItem, String selectedContext,
            String originalContext) {
        if (connItem == null) {
            return null;
        }
        DatabaseConnection dbConn=null;
        if(connItem instanceof DatabaseConnectionItem) {
            dbConn=(DatabaseConnection)connItem.getConnection();
        }
        
        return getExtractorInstance(dbConn, selectedContext, originalContext);
    }

    public static IUrlDbNameExtractor getExtractorInstance(DatabaseConnection dbconn, String selectedContext,
            String originalContext) {
        if (dbconn == null) {
            return null;
        }

        if (!checkIsGenericJDBCType(dbconn)) {
            return null;
        }
        IUrlDbNameExtractor theExtractor = null;
        String databaseProductName = TaggedValueHelper.getValueString(TaggedValueHelper.DB_PRODUCT_NAME, dbconn);
        theExtractor = createExtractorByDP(databaseProductName);
        IMetadataConnection metadataConnection = ConvertionHelper.convert(dbconn, false, selectedContext);
        initExtractor(theExtractor, metadataConnection);
        return theExtractor;
    }

    private static boolean checkIsGenericJDBCType(DatabaseConnection dbConn) {
        if (dbConn == null) {
            return false;
        }
        return EDatabaseTypeName.GENERAL_JDBC.getXMLType().equals(dbConn.getDatabaseType());
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
            initVersion(theExtractor, dbMetadata, metadataConnection);
        }
    }

    protected static void initExtractor(IUrlDbNameExtractor theExtractor, IMetadataConnection metadataConnection) {
        if (theExtractor != null) {
            theExtractor.setUrl(metadataConnection.getUrl());
            theExtractor.setDriverName(metadataConnection.getDriverJarPath());
            theExtractor.setVersion(metadataConnection.getDbVersionString());
        }
    }

    private static void initVersion(IUrlDbNameExtractor theExtractor, DatabaseMetaData dbMetadata,
            IMetadataConnection metadataConnection) {
        try {
            theExtractor.setVersion(dbMetadata.getDatabaseProductVersion());
        } catch (SQLException e) {
        }
        if (metadataConnection != null && theExtractor.getVersion() == null) {
            theExtractor.setVersion(metadataConnection.getDbVersionString());
        }
    }

    private static void initDriverName(IUrlDbNameExtractor theExtractor, DatabaseMetaData dbMetadata,
            IMetadataConnection metadataConnection) {
        try {
            theExtractor.setDriverName(dbMetadata.getDriverName());
        } catch (SQLException e) {
        }
        if (metadataConnection != null
                && (theExtractor.getDriverName() == null || !theExtractor.getDriverName().contains(".jar"))) {
            theExtractor.setDriverName(metadataConnection.getDriverJarPath());
        }

    }

    protected static void initURL(IUrlDbNameExtractor theExtractor, DatabaseMetaData dbMetadata,
            IMetadataConnection metadataConnection) {
        try {
            theExtractor.setUrl(dbMetadata.getURL());
        } catch (SQLException e) {
        }
        if (metadataConnection != null
                && (theExtractor.getUrl() == null || metadataConnection.getUrl().contains(theExtractor.getUrl()))) {
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
        // case Mysql:
        // return new MysqlExtractor();
        case Snowflake:
            return new SnowflakeExtractor();
        }
        return null;
    }

    public static boolean isSupportDB(EDBTypeProductNameMapping dbType) {
        return createExtractor(dbType) != null;
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
