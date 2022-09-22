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

public class TMCRepositoryUtil {

    public static final String REPOSITORY_CLOUD_US_ID = "cloud_us"; //$NON-NLS-1$

    public static final String REPOSITORY_CLOUD_EU_ID = "cloud_eu"; //$NON-NLS-1$

    public static final String REPOSITORY_CLOUD_APAC_ID = "cloud_apac"; //$NON-NLS-1$

    public static final String REPOSITORY_CLOUD_US_WEST_ID = "cloud_us_west"; //$NON-NLS-1$

    public static final String REPOSITORY_CLOUD_AUS_ID = "cloud_aus"; //$NON-NLS-1$

    public static final String REPOSITORY_CLOUD_US_DISPALY = "United States - East on AWS"; //$NON-NLS-1$

    public static final String REPOSITORY_CLOUD_EU_DISPALY = "Europe on AWS"; //$NON-NLS-1$

    public static final String REPOSITORY_CLOUD_APAC_DISPALY = "Asia Pacific on AWS"; //$NON-NLS-1$

    public static final String REPOSITORY_CLOUD_US_WEST_DISPALY = "United States - West on Azure"; //$NON-NLS-1$

    public static final String REPOSITORY_CLOUD_AUS_DISPALY = "Australia on AWS"; //$NON-NLS-1$

    public static final String REPOSITORY_CLOUD_CUSTOM_ID = "cloud_custom"; //$NON-NLS-1$

    public static final String AUTHORIZE_URL = "https://iam.%s.cloud.talend.com/oidc/idp/authorize"; //$NON-NLS-1$

    public static final String ADMIN_URL = "https://tmc.%s.cloud.talend.com/studio_cloud_connection"; //$NON-NLS-1$

    public static final String TOKEN_URL = "https://iam.%s.cloud.talend.com/oidc/oauth2/token"; //$NON-NLS-1$

    public static final String SUCCESS_REDIRECT_URL = "https://iam.%s.cloud.talend.com/idp/login-sso-success"; //$NON-NLS-1$
    
    public static final String ONLINE_HELP_URL = "https://document-link.us.cloud.talend.com/ts_ug_launch-studio?version=%s&lang=%s&env=prd";

    public static String getBaseLoginURL(String dataCenter) {
        if (dataCenter == null) {
            dataCenter = TMCRepositoryUtil.getDefaultDataCenter();
        }
        return String.format(AUTHORIZE_URL, dataCenter);
    }

    public static String getDefaultDataCenter() {
        String defaultDataCenter = "us";
        if (System.getProperty(SSOClientUtil.DATA_CENTER_KEY) != null) {
            defaultDataCenter = System.getProperty(SSOClientUtil.DATA_CENTER_KEY);
        }
        return defaultDataCenter;
    }

    public static String getCloudAdminURL(String dataCenter) {
        return String.format(ADMIN_URL, dataCenter);
    }

    public static String getTokenURL(String dataCenter) {
        return String.format(TOKEN_URL, dataCenter);
    }

    public static String getRedirectURL(String dataCenter) {
        if (dataCenter == null) {
            dataCenter = getDefaultDataCenter();
        }
        return String.format(SUCCESS_REDIRECT_URL, dataCenter);
    }

    public static String getDisplayNameByDatacenter(String dataCenter) {
        if ("ap".equals(dataCenter)) {
            return REPOSITORY_CLOUD_APAC_DISPALY;
        }
        if ("us".equals(dataCenter)) {
            return REPOSITORY_CLOUD_US_DISPALY;
        }
        if ("us-west".equals(dataCenter)) {
            return REPOSITORY_CLOUD_US_WEST_DISPALY;
        }
        if ("eu".equals(dataCenter)) {
            return REPOSITORY_CLOUD_EU_DISPALY;
        }
        if ("au".equals(dataCenter)) {
            return REPOSITORY_CLOUD_AUS_DISPALY;
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
