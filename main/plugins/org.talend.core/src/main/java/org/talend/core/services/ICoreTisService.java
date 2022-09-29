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
package org.talend.core.services;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.osgi.service.prefs.BackingStoreException;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.exception.SystemException;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.IService;
import org.talend.core.model.general.Project;
import org.talend.core.model.process.INode;
import org.talend.core.model.properties.Property;
import org.talend.core.runtime.maven.MavenArtifact;

/**
 * wchen class global comment. Detailled comment
 */
public interface ICoreTisService extends IService {

    public void drawWelcomeLogo(String version);

    public boolean isSupportDynamicType(INode node);

    public boolean validProject(Project project, boolean flag) throws PersistenceException;

    public List<MavenArtifact> getInstalledPatchArtifacts() throws BackingStoreException;

    public boolean needRestartAfterUpdate();

    public void setNeedResartAfterUpdate(boolean needRestart);

    /**
     * DOC ycbai Comment method "exportAsCWM".
     *
     * @param itemUri
     * @param destDir
     */
    public void exportAsCWM(URI itemUri, String destDir);

    public boolean needUpdate(String userName, String password, String adminUrl)  throws SystemException;

    public void downLoadAndInstallUpdates(String userName, String password, String adminUrl) throws Exception;

    public void downLoadAndInstallUpdates(IProgressMonitor monitor, String userName, String password, String adminUrl)
            throws Exception;

    public boolean isLicenseExpired();

    public boolean isTheSameType(String userName, String password, String adminUrl);

    public void clearCustomLicensePathArg();

    public File getLicenseFile();

    public String generateSignerSessionId();

    public void updateConfiguratorBundles(File configFile, File tempConfigFile) throws IOException;

    Map<String, String> getExtraBundleInfo4Patch(File featureIndexFile) throws IOException;

    Map<String, String> getDropBundleInfo() throws IOException;

    Set<String> getComponentBlackList();
    
    String getStandardNodeLabel();

    public void afterImport (Property property) throws PersistenceException;  

    Integer getSignatureVerifyResult(Property property, IPath resourcePath, boolean considerGP) throws Exception;

    String getLicenseCustomer();
    
    void storeLicenseAndUpdateConfig(String licenseString) throws IOException;

    boolean isInValidGP();

    boolean hasNewPatchInPatchesFolder();

    boolean isDefaultLicenseAndProjectType();
    
    String getLicenseProductName(String licenseString) throws Exception;
    
    String getLicenseProductEdition(String licenseString) throws Exception; 
    
    boolean isLicenseExpired(String licenseString) throws Exception;
    
    boolean isLicenseVersionCorrect(String licenseString) throws Exception;

    void syncProjectUpdateSettingsFromServer(IProgressMonitor monitor, Project proj) throws Exception;

    void refreshPatchesFolderCache();

    static ICoreTisService get() {
        if (GlobalServiceRegister.getDefault().isServiceRegistered(ICoreTisService.class)) {
            return GlobalServiceRegister.getDefault().getService(ICoreTisService.class);
        }
        return null;
    }
}
