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

}
