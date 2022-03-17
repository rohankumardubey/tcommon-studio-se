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

import org.talend.core.database.conn.DatabaseConnStrUtil;

public abstract class AbstractUrlDbNameExtractor implements IUrlDbNameExtractor {

    private String url;

    private String driverName;

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String getUrl() {
        return url;
    }

    /**
     * Sets the driverName.
     * 
     * @param driverName the driverName to set
     */
    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    /**
     * Getter for driverName.
     * 
     * @return the driverName
     */
    public String getDriverName() {
        return driverName;
    }

    @Override
    public void initUiSchemaOrSID() {
        String[] analyseURL = DatabaseConnStrUtil.analyseURL(getCurrentDbType(), getDbVersion(), url);
    }

    protected abstract String getDbVersion();

    protected abstract String getCurrentDbType();

}
