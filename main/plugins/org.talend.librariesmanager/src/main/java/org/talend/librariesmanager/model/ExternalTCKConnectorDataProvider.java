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
package org.talend.librariesmanager.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.model.general.ModuleToInstall;
import org.talend.core.runtime.util.SharedStudioUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ExternalTCKConnectorDataProvider {

    private static Logger logger = Logger.getLogger(ExternalTCKConnectorDataProvider.class);

    private static final String DATA_FILE_NAME = "ext_connector.json";

    private static final File DATA_FILE = getDataFile();

    private ExternalConnectorData data;

    private static final ExternalTCKConnectorDataProvider instance = new ExternalTCKConnectorDataProvider();

    public static ExternalTCKConnectorDataProvider getIntance() {
        return instance;
    }

    private ExternalTCKConnectorDataProvider() {
        loadExtConnectorData();
    }

    public void connectorDownloaded(ModuleToInstall module) throws Exception {
        ExternalConnector connector = new ExternalConnector();
        connector.setFileName(module.getModuleFile().getName());
        connector.setMvnUrl(module.getMavenUri());
        connector.setInstalled(false);

        data.getConnectorList().add(connector);
        saveExtConnectorData();
    }

    public void connectorInstalled(String fileName) throws Exception {
        ExternalConnector connector = null;
        for (ExternalConnector c : data.getConnectorList()) {
            if (StringUtils.equals(c.getFileName(), fileName)) {
                connector = c;
                break;
            }
        }

        if (connector != null) {
            connector.setInstalled(true);
            saveExtConnectorData();
        } else {
            logger.error("Can't install connector file:" + fileName);
        }
    }

    private void loadExtConnectorData() {
        TypeReference<ExternalConnectorData> typeReference = new TypeReference<ExternalConnectorData>() {
            // no need to overwrite
        };
        if (DATA_FILE.exists()) {
            try {
                data = new ObjectMapper().readValue(DATA_FILE, typeReference);
            } catch (IOException e) {
                ExceptionHandler.process(e);
            }
        }
    }

    private synchronized void saveExtConnectorData() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            if (!DATA_FILE.exists()) {
                DATA_FILE.createNewFile();
            }
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(DATA_FILE, data);
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    private static File getDataFile() {
        File folder = getTCKConnectorExtFolder();
        return new File(folder, DATA_FILE_NAME);
    }

    public static File getTCKConnectorExtFolder() {
        File componentFolder = SharedStudioUtils.getSharedStudioComponentsParentFolder();
        IPath path = new Path(IComponentsFactory.COMPONENTS_INNER_FOLDER);
        path = path.append(IComponentsFactory.EXTERNAL_COMPONENTS_INNER_FOLDER);
        File extFolder = new File(componentFolder, path.toOSString());
        if (!extFolder.exists()) {
            extFolder.mkdirs();
        }
        return extFolder;
    }
}

class ExternalConnectorData {

    @JsonProperty("externalConnectorList")
    private List<ExternalConnector> connectorList = new ArrayList<ExternalConnector>();

    public List<ExternalConnector> getConnectorList() {
        return connectorList;
    }

    public void setConnectorList(List<ExternalConnector> connectorList) {
        this.connectorList = connectorList;
    }

}

class ExternalConnector {

    @JsonProperty("mvnUrl")
    private String mvnUrl;

    @JsonProperty("fileName")
    private String fileName;

    @JsonProperty("isInstalled")
    private boolean isInstalled;

    public String getMvnUrl() {
        return mvnUrl;
    }

    public void setMvnUrl(String mvnUrl) {
        this.mvnUrl = mvnUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isInstalled() {
        return isInstalled;
    }

    public void setInstalled(boolean isInstalled) {
        this.isInstalled = isInstalled;
    }

}

