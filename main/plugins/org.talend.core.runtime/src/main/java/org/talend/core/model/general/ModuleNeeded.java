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
package org.talend.core.model.general;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Version;
import org.talend.commons.CommonsPlugin;
import org.talend.commons.exception.CommonExceptionHandler;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ILibraryManagerService;
import org.talend.core.database.conn.version.DatabaseDriversCache;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.core.runtime.maven.MavenArtifact;
import org.talend.core.runtime.maven.MavenConstants;
import org.talend.core.runtime.maven.MavenUrlHelper;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.utils.json.JSONException;
import org.talend.utils.json.JSONObject;
import org.talend.utils.json.JSONTokener;

/**
 * This bean is use to manage needed moduless (perl) and libraries (java).<br/>
 *
 * $Id: ModuleNeeded.java 38013 2010-03-05 14:21:59Z mhirt $
 *
 */
public class ModuleNeeded {

    public static final String ATTR_USED_BY_DYNAMIC_DISTRIBUTION = "dynamicDistribution";

    public static final String ATTR_DYNAMIC_DISTRIBUTION_VERSION = "distributionVersion";
    
    private String id;

    private String context;

    private String moduleName;

    private String informationMsg;

    private boolean required;

    private boolean excluded;

    private boolean mrRequired = false; // That indicates if the module is
                                        // required by M/R job.

    private String requiredIf;

    // bundleName and bundleVersion for osgi system,feature 0023460
    private String bundleName;

    private String bundleVersion;

    private ELibraryInstallStatus status = ELibraryInstallStatus.NOT_INSTALLED;

    // status installed in maven
    private ELibraryInstallStatus installStatus = ELibraryInstallStatus.NOT_DEPLOYED;

    private boolean isShow = true;

    List<String> installURL;

    private String moduleLocaion;

    private String mavenUriFromConfiguration;

    private String mavenUri;

    private boolean excludeDependencies = false;

    private boolean dynamic;

    private Map<String, Object> extraAttributes = new HashMap<>();

    public static final String SINGLE_QUOTE = "'"; //$NON-NLS-1$

    public static final String QUOTATION_MARK = "\""; //$NON-NLS-1$

    public static final String UNKNOWN = "Unknown";

    /**
     * TODO This is a hot fix for 7.2.1 . if the maven url is not specified in component/extension point , we will
     * generate a release version with this flag=true .Need to improve it after release ,normally the studio should use
     * all jars with release version by default if maven url is not specified.
     */
    private boolean useReleaseVersion = false;

    private Map<String, String> attributes;

    ILibraryManagerService libManagerService = GlobalServiceRegister.getDefault()
            .getService(ILibraryManagerService.class);
    

    /**
     * DOC smallet ModuleNeeded class global comment. Detailled comment <br/>
     *
     * $Id: ModuleNeeded.java 38013 2010-03-05 14:21:59Z mhirt $
     *
     */
    public enum ELibraryInstallStatus {
        INSTALLED,
        NOT_INSTALLED,
        DEPLOYED,
        NOT_DEPLOYED;

    }

    public static ModuleNeeded newInstance(String context, String value, String informationMsg, boolean required) {
        String val = TalendQuoteUtils.removeQuotesIfExist(value);
        if (val.startsWith(MavenUrlHelper.MVN_PROTOCOL)) {
            return new ModuleNeeded(context, informationMsg, required, val);
        }
        // won't do migration for old MODULE_LIST but still make it compatible
        return new ModuleNeeded(context, val, informationMsg, required);
    }

    /**
     * DOC smallet ModuleNeeded constructor comment.
     *
     * @param context
     * @param moduleName
     * @param informationMsg
     * @param required
     * @param unused
     * @param status
     */
    public ModuleNeeded(String context, String moduleName, String informationMsg, boolean required) {
        this(context, moduleName, informationMsg, required, null, null, null);
    }

    /**
     * creates ModuleNeeded from its maven uri. the modeule name is the artifact_ID + "." + artifact_type
     *
     * @param context
     * @param informationMsg
     * @param required
     * @param mvnUri
     */
    public ModuleNeeded(String context, String informationMsg, boolean required, String mvnUri) {
        this(context, null, informationMsg, required, null, null, mvnUri);
        MavenArtifact mavenArtifact = MavenUrlHelper.parseMvnUrl(mvnUri);
        if (MavenConstants.DEFAULT_LIB_GROUP_ID.equals(mavenArtifact.getGroupId())
                || StringUtils.isEmpty(mavenArtifact.getVersion())) {
            setModuleName(mavenArtifact.getArtifactId() + "." + mavenArtifact.getType()); //$NON-NLS-1$
        } else {
            setModuleName(mavenArtifact.getArtifactId() + "-" + mavenArtifact.getVersion() + "." + mavenArtifact.getType()); //$NON-NLS-1$//$NON-NLS-2$
        }

    }

    public ModuleNeeded(String context, String moduleName, String informationMsg, boolean required, List<String> installURL,
            String requiredIf, String mavenUrl) {
        super();
        this.context = context;
        this.informationMsg = informationMsg;
        this.required = required;
        this.installURL = installURL;
        this.requiredIf = requiredIf;
        this.attributes = analyseMessage(informationMsg);
        if (!this.attributes.isEmpty()) {
            this.informationMsg = "";
        }
        String name = moduleName;
        String uri = mavenUrl;
        if (moduleName != null) {
            // in case the param moduleName is a maven uri
            MavenArtifact artifact = MavenUrlHelper.parseMvnUrl(moduleName);
            if (artifact != null) {
                name = artifact.getFileName();
                if (mavenUrl == null) {
                    uri = moduleName;
                }
            }
        }
        if (mavenUrl != null && moduleName == null) {
            MavenArtifact artifact = MavenUrlHelper.parseMvnUrl(mavenUrl);
            if (artifact != null) {
                name = artifact.getFileName();
            }
        }
        setModuleName(name);
        setMavenUri(uri);
    }

    private Map<String, String> analyseMessage(String msg) {
        Map<String, String> attrMap = new HashMap<>();
        if (StringUtils.isBlank(msg) || !msg.startsWith("{") || !msg.endsWith("}")) {
            return attrMap;
        }
        try {
            JSONObject jo = new JSONObject(new JSONTokener(msg));
            Iterator<String> keys = jo.keys();
            keys.forEachRemaining(key -> {
                try {
                    Object object = jo.get(key);
                    if (object != null) {
                        attrMap.put(key, object.toString());
                    }
                } catch (JSONException e) {
                    ExceptionHandler.process(e);
                }
            });
        } catch (Exception e) {
            if (Boolean.getBoolean("talend.studio.moduleNeeded.init.debug")) {
                ExceptionHandler.process(e);
            }
        }
        return attrMap;
    }

    @Override
    public ModuleNeeded clone() {
        ModuleNeeded cloned = new ModuleNeeded(context, moduleName, informationMsg, mrRequired, installURL, requiredIf, mavenUri);
        cloned.bundleName = bundleName;
        cloned.bundleVersion = bundleVersion;
        cloned.context = context;
        cloned.dynamic = dynamic;
        cloned.excludeDependencies = excludeDependencies;
        if (extraAttributes != null && !extraAttributes.isEmpty()) {
            cloned.extraAttributes = new HashMap<>(extraAttributes);
        }
        cloned.id = id;
        cloned.informationMsg = informationMsg;
        cloned.installStatus = installStatus;
        if (installURL != null && !installURL.isEmpty()) {
            cloned.installURL = new ArrayList<>(installURL);
        }
        cloned.isShow = isShow;
        cloned.libManagerService = libManagerService;
        cloned.mavenUri = mavenUri;
        cloned.mavenUriFromConfiguration = mavenUriFromConfiguration;
        cloned.moduleLocaion = moduleLocaion;
        cloned.moduleName = moduleName;
        cloned.mrRequired = mrRequired;
        cloned.required = required;
        cloned.requiredIf = requiredIf;
        cloned.status = status;
        cloned.useReleaseVersion = useReleaseVersion;
        cloned.attributes = attributes;

        return cloned;
    }

    public String getRequiredIf() {
        return requiredIf;
    }

    public void setRequiredIf(String requiredIf) {
        this.requiredIf = requiredIf;
    }

    /**
     * Check if the library is required depends the condition of "required if". Note that if the flag "required=true" in
     * the xml of component, it will never check in the required_if.
     *
     * In some cases where we only want to check the basic "required=true" and not the required_if (module view for
     * example), it's possible to simply give null parameter.
     *
     * @param listParam
     * @return
     */
    public boolean isRequired(List<? extends IElementParameter> listParam) {
        if (required) { // if flag required is set, then forget the "required if" test.
            return required;
        }
        boolean isRequired = false;

        if (requiredIf != null && !requiredIf.isEmpty() && listParam != null) {
            isRequired = CoreRuntimePlugin.getInstance().getDesignerCoreService().evaluate(requiredIf, listParam);
        }
        return isRequired;
    }

    /**
     * Getter for installURL.
     *
     * @return the installURL
     */
    public List<String> getInstallURL() {
        return this.installURL;
    }

    /**
     * Sets the installURL.
     *
     * @param installURL the installURL to set
     */
    public void setInstallURL(List<String> installURL) {
        this.installURL = installURL;
    }

    /**
     * Getter for component.
     *
     * @return the component
     */
    public String getContext() {
        return this.context;
    }

    /**
     * Sets the component.
     *
     * @param component the component to set
     */
    public void setContext(String component) {
        this.context = component;
    }

    public String getInformationMsg() {
        return this.informationMsg;
    }

    public void setInformationMsg(String informationMsg) {
        this.informationMsg = informationMsg;
    }

    public String getModuleName() {
        return this.moduleName;
    }

    public void setModuleName(String moduleName) {
        if (moduleName != null) {
            String mn = moduleName.replace(QUOTATION_MARK, "").replace(SINGLE_QUOTE, ""); //$NON-NLS-1$ //$NON-NLS-2$
            if (mn.indexOf("\\") != -1 || mn.indexOf("/") != -1) { //$NON-NLS-1$ //$NON-NLS-2$
                mn = new Path(mn).lastSegment();
            }
            this.moduleName = mn;
        } else {
            this.moduleName = moduleName;
        }
    }

    public boolean isRequired() {
        return this.required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isExcluded() {
        return this.excluded;
    }

    public void setExcluded(boolean excluded) {
        this.excluded = excluded;
    }

    public ELibraryInstallStatus getStatus() {
        ILibraryManagerService libManagerService = GlobalServiceRegister.getDefault()
                .getService(ILibraryManagerService.class);
        libManagerService.checkModuleStatus(this);
        String mvnUriStatusKey = getMavenUri();
        this.status = ModuleStatusProvider.getStatus(mvnUriStatusKey);
        return this.status;
    }

    public ELibraryInstallStatus getDeployStatus() {
        ILibraryManagerService libManagerService = GlobalServiceRegister.getDefault()
                .getService(ILibraryManagerService.class);
        libManagerService.checkModuleStatus(this);
        String mvnUriStatusKey = getMavenUri();

        this.installStatus = ModuleStatusProvider.getDeployStatus(mvnUriStatusKey);
        return this.installStatus;
    }

    /**
     * Getter for isShow.
     *
     * @return the isShow
     */
    public boolean isShow() {
        return this.isShow;
    }

    /**
     * Sets the isShow.
     *
     * @param isShow the isShow to set
     */
    public void setShow(boolean isShow) {
        this.isShow = isShow;
    }

    public String getBundleName() {
        return bundleName;
    }

    public void setBundleName(String bundleName) {
        this.bundleName = bundleName;
    }

    public String getBundleVersion() {
        return bundleVersion;
    }

    public void setBundleVersion(String bundleVersion) {
        this.bundleVersion = bundleVersion;
    }

    /**
     * Getter for mrRequired.
     *
     * @return the mrRequired
     */
    public boolean isMrRequired() {
        return this.mrRequired;
    }

    /**
     * Sets the mrRequired.
     *
     * @param mrRequired the mrRequired to set
     */
    public void setMrRequired(boolean mrRequired) {
        this.mrRequired = mrRequired;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        if (bundleName == null || "".equals(bundleName.trim())) {
            return moduleName;
        } else if (bundleVersion == null) {
            return moduleName + "[" + bundleName + "]";
        } else {
            return moduleName + "[" + bundleName + ":" + bundleVersion + "]";
        }
    }

    public String getModuleLocaion() {
        if (this.moduleLocaion == null) {
            moduleLocaion = libManagerService.getPlatformURLFromIndex(moduleName);
            // fix for cached ModuleNeeded with status NOT_INSTALLED
            if (moduleLocaion != null && ELibraryInstallStatus.NOT_INSTALLED == ModuleStatusProvider.getStatus(getMavenUri())) {
                ModuleStatusProvider.resetStatus(getMavenUri());
            }
        }
        return moduleLocaion;
    }

    public void setModuleLocaion(String moduleLocaion) {
        if (moduleLocaion != null && ELibraryInstallStatus.NOT_INSTALLED == ModuleStatusProvider.getStatus(getMavenUri())) {
            ModuleStatusProvider.resetStatus(getMavenUri());
        }
        this.moduleLocaion = moduleLocaion;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int hashCode = 31;
        if (this.getId() != null) {
            hashCode *= this.getId().hashCode();
        }
        if (this.getModuleName() != null) {
            hashCode *= this.getModuleName().hashCode();
        }
        if (this.getBundleName() != null) {
            hashCode *= this.getBundleName().hashCode();
        }
        if (this.getBundleVersion() != null) {
            hashCode *= this.getBundleVersion().hashCode();
        }
        if (this.getModuleLocaion() != null) {
            hashCode *= this.getModuleLocaion().hashCode();
        }
        if(this.getDefaultMavenURI() != null){
            hashCode *= this.getDefaultMavenURI().hashCode();
        }
        return hashCode;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ModuleNeeded)) {
            return false;
        }
        ModuleNeeded other = (ModuleNeeded) obj;

        // ModuleName
        if (other.getModuleName() == null) {
            if (this.getModuleName() != null) {
                return false;
            }
        } else {
            if (this.getModuleName() == null) {
                return false;
            } else if (!other.getModuleName().equals(this.getModuleName())) {
                return false;
            }
        }
        // BundleName
        if (other.getBundleName() == null) {
            if (this.getBundleName() != null) {
                return false;
            }
        } else {
            if (this.getBundleName() == null) {
                return false;
            } else if (!other.getBundleName().equals(this.getBundleName())) {
                return false;
            }
        }
        // BundleVersion
        if (other.getBundleVersion() == null) {
            if (this.getBundleVersion() != null) {
                return false;
            }
        } else {
            if (this.getBundleVersion() == null) {
                return false;
            } else if (!other.getBundleVersion().equals(this.getBundleVersion())) {
                return false;
            }
        }

        // Module context
        if (other.getContext() == null) {
            if (this.getContext() != null) {
                return false;
            }
        } else {
            if (this.getContext() == null) {
                return false;
            } else if (!other.getContext().equals(this.getContext())) {
                return false;
            }
        }

        // Module Location
        if (other.getModuleLocaion() == null) {
            if (this.getModuleLocaion() != null) {
                return false;
            }
        } else {
            if (this.getModuleLocaion() == null) {
                return false;
            } else if (!other.getModuleLocaion().equals(this.getModuleLocaion())) {
                return false;
            }
        }

        // maven uri
        if (other.getDefaultMavenURI() == null) {
            if (this.getDefaultMavenURI() != null) {
                return false;
            }
        } else {
            if (this.getDefaultMavenURI() == null) {
                return false;
            } else if (!other.getDefaultMavenURI().equals(this.getDefaultMavenURI())) {
                return false;
            }
        }

        return true;

    }

    /**
     * Get the maven uri from talend configuration: component-IMPORT or librariesNeeded extension
     */
    public String getMavenURIFromConfiguration() {
        return this.mavenUriFromConfiguration;
    }

    /**
     * Get the maven uri from talend configuration or generate a default one if <mavenUriFromConfiguration> is null
     */
    public String getDefaultMavenURI() {
        mavenUri = initURI();
        return mavenUri;
    }

    /**
     *
     * Get the maven URI with priority:custom URI ,URI from configuration, generated by default
     *
     * @return
     */
    public String getMavenUri() {
        if (getCustomMavenUri() != null) {
            return getCustomMavenUri();
        }
        mavenUri = initURI();
        return mavenUri;
    }

    /**
     *
     * DOC wchen Comment method "initURI".
     *
     * @return
     */
    private String initURI() {
        if (mavenUri == null) {
            if (StringUtils.isEmpty(mavenUriFromConfiguration)) {
                // get the latest snapshot maven uri from index as default
                String mvnUrisFromIndex = getGuessMavenUri();
                if (mvnUrisFromIndex != null) {
                    mavenUri = MavenUrlHelper.addTypeForMavenUri(mvnUrisFromIndex, getModuleName());
                } else {
                    boolean isDatabaseDriver = DatabaseDriversCache.isDatabaseDriver(getModuleName());
                    mavenUri = MavenUrlHelper.generateMvnUrlForJarName(getModuleName(), true,
                            !isDatabaseDriver && !useReleaseVersion);
                }
            } else {
                mavenUri = mavenUriFromConfiguration;
            }
        } else if (mavenUriFromConfiguration == null) {
            // in case the index is created after module loaded
            String mvnUrisFromIndex = getGuessMavenUri();
            if (mvnUrisFromIndex != null && !mavenUri.equals(mvnUrisFromIndex)) {
                mavenUri = MavenUrlHelper.addTypeForMavenUri(mvnUrisFromIndex, getModuleName());
            }
        }
        return mavenUri;
    }

    private String getGuessMavenUri() {
        // get the latest snapshot maven uri from index as default
        String maxVerstion = null;
        String mvnUrisFromIndex = libManagerService.getMavenUriFromIndex(getModuleName());
        if (mvnUrisFromIndex != null) {
            final String[] split = mvnUrisFromIndex.split(MavenUrlHelper.MVN_INDEX_SPLITER);
            for (String mvnUri : split) {
                if (maxVerstion == null) {
                    maxVerstion = mvnUri;
                } else {
                    MavenArtifact lastArtifact = MavenUrlHelper.parseMvnUrl(maxVerstion);
                    MavenArtifact currentArtifact = MavenUrlHelper.parseMvnUrl(mvnUri);
                    if (lastArtifact != null && currentArtifact != null) {
                        String lastV = lastArtifact.getVersion();
                        String currentV = currentArtifact.getVersion();
                        if (!lastV.equals(currentV)) {
                            Version lastVersion = getVerstion(lastArtifact);
                            Version currentVersion = getVerstion(currentArtifact);
                            if (currentVersion.compareTo(lastVersion) > 0) {
                                maxVerstion = mvnUri;
                            }
                        }
                    }
                }

            }
        }
        return maxVerstion;
    }

    private Version getVerstion(MavenArtifact artifact) {
        String versionStr = artifact.getVersion();
        int index = versionStr.indexOf("-");
        if (index != -1) {
            versionStr = versionStr.split("-")[0];
        }
        Version version = null;
        try {
            version = new Version(versionStr);
        } catch (Exception e) {
            version = new Version(0, 0, 0);
        }
        return version;
    }

    /**
     * Sets the mavenUrl.
     *
     * @param mavenUrl the mavenUrl to set
     */
    public void setMavenUri(String mavenUri) {
        this.mavenUriFromConfiguration = MavenUrlHelper.addTypeForMavenUri(mavenUri, getModuleName());
        if (!StringUtils.isEmpty(mavenUriFromConfiguration)) {
            this.mavenUri = mavenUriFromConfiguration;
        }
        String generateModuleName = MavenUrlHelper.generateModuleNameByMavenURI(this.mavenUri);
        if (StringUtils.isNotBlank(generateModuleName)) {

            if (!StringUtils.equals(getModuleName(), generateModuleName)) {

                if (CommonsPlugin.isDebugMode() && StringUtils.isNotBlank(this.context)) {

                    CommonExceptionHandler
                            .warn("module name definition should be " + generateModuleName + ", not " + getModuleName()
                                    + " :" + this.context);
                }

                setModuleName(generateModuleName);
            }
        }
    }

    public boolean usedByDynamicDistribution() {
        return Boolean.valueOf(attributes.get(ATTR_USED_BY_DYNAMIC_DISTRIBUTION));
    }

    public boolean isDynamic() {
        return this.dynamic;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

    public Map<String, Object> getExtraAttributes() {
        return this.extraAttributes;
    }

    public String getCustomMavenUri() {
        String originalURI = initURI();
        String customURI = libManagerService.getCustomMavenURI(originalURI);
        if (originalURI != null && !originalURI.equals(customURI)) {
            return customURI;
        } else {
            return null;
        }
    }

    public void setCustomMavenUri(String customURI) {
        String customURIWithType = MavenUrlHelper.addTypeForMavenUri(customURI, getModuleName());
        libManagerService.setCustomMavenURI(getDefaultMavenURI(), customURIWithType);
    }

    public boolean isExcludeDependencies() {
        return this.excludeDependencies;
    }

    public void setExcludeDependencies(boolean excludeDependencies) {
        this.excludeDependencies = excludeDependencies;
    }

    public void setUseReleaseVersion(boolean useReleaseVersion) {
        this.useReleaseVersion = useReleaseVersion;
    }
    
    public String getDynamicDistributionVersion() {
        return attributes.get(ATTR_DYNAMIC_DISTRIBUTION_VERSION);
    }
    
    public void setDynamicDistributionVersion(String distribution) {
        attributes.put(ATTR_DYNAMIC_DISTRIBUTION_VERSION, distribution);
    }
}
