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
package org.talend.signon.util;
import org.talend.signon.util.i18n.Messages;

public class TMCRepositoryUtil {
    public static final String REPOSITORY_CLOUD_US_ID = "cloud_us"; //$NON-NLS-1$

    public static final String REPOSITORY_CLOUD_EU_ID = "cloud_eu"; //$NON-NLS-1$

    public static final String REPOSITORY_CLOUD_APAC_ID = "cloud_apac"; //$NON-NLS-1$

    public static final String REPOSITORY_CLOUD_US_WEST_ID = "cloud_us_west"; //$NON-NLS-1$
    
    public static final String REPOSITORY_CLOUD_AUS_ID = "cloud_aus"; //$NON-NLS-1$
    
    public static final String REPOSITORY_CLOUD_CUSTOM_ID = "cloud_custom"; //$NON-NLS-1$
    
    public static String getBaseLoginURL(String dataCenter) {
        if (dataCenter == null) {
            dataCenter = TMCRepositoryUtil.getDefaultDataCenter();
        }
        return "https://iam." + dataCenter + ".cloud.talend.com/oidc/idp/authorize";
    }

    public static String getDefaultDataCenter() {
        String defaultDataCenter = "int";
        if (System.getProperty(SSOClientUtil.DATA_CENTER_KEY) != null) {
            defaultDataCenter = System.getProperty(SSOClientUtil.DATA_CENTER_KEY);
        }
        return defaultDataCenter;
    }

    public static String getCloudAdminURL(String dataCenter) {
        return "https://tmc." + dataCenter + ".cloud.talend.com/studio_cloud_connection";
    }

    public static String getTokenURL(String dataCenter) {
        return "https://iam." + dataCenter + ".cloud.talend.com/oidc/oauth2/token";
    }
    
    public static String getRedirectURL(String dataCenter) {
        if (dataCenter == null) {
            dataCenter =  getDefaultDataCenter();
        }
        return "https://iam." + dataCenter + ".cloud.talend.com/idp/login-sso-success";
    }

    public static String getDisplayNameByDatacenter(String dataCenter) {
        if ("ap".equals(dataCenter)) {
            return Messages.getString("TMCRepositoryUtil.ap.displayName");
        }
        if ("us".equals(dataCenter)) {
            return Messages.getString("TMCRepositoryUtil.us.displayName");
        }
        if ("us-west".equals(dataCenter)) {
            return Messages.getString("TMCRepositoryUtil.us-west.displayName");
        }
        if ("eu".equals(dataCenter)) {
            return Messages.getString("TMCRepositoryUtil.eu.displayName");
        }
        if ("au".equals(dataCenter)) {
            return Messages.getString("TMCRepositoryUtil.au.displayName");
        }
    
        if (System.getProperty(SSOClientUtil.DATA_CENTER_DISPLAY_KEY) != null) {
            return System.getProperty(SSOClientUtil.DATA_CENTER_DISPLAY_KEY);
        }
    
        return dataCenter;
    }
    
    public static String getRepositoryId(String dataCenter) {
        if ("ap".equals(dataCenter)) {
            return REPOSITORY_CLOUD_APAC_ID;
        }
        if ("us".equals(dataCenter)) {
            return REPOSITORY_CLOUD_US_ID;
        }
        if ("us-west".equals(dataCenter)) {
            return REPOSITORY_CLOUD_US_WEST_ID;
        }
        if ("eu".equals(dataCenter)) {
            return REPOSITORY_CLOUD_EU_ID;
        }
        if ("au".equals(REPOSITORY_CLOUD_AUS_ID)) {
            return REPOSITORY_CLOUD_EU_ID;
        }
        return REPOSITORY_CLOUD_CUSTOM_ID;
    }

}
