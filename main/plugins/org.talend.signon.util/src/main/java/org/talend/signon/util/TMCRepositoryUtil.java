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

    public static String getDisplayNameByDatacenter(String dataCenter) {
        if ("ap".equals(dataCenter)) {
            return Messages.getString("SSOClientUtil.ap.displayName");
        }
        if ("us".equals(dataCenter)) {
            return Messages.getString("SSOClientUtil.us.displayName");
        }
        if ("us-west".equals(dataCenter)) {
            return Messages.getString("SSOClientUtil.us-west.displayName");
        }
        if ("eu".equals(dataCenter)) {
            return Messages.getString("SSOClientUtil.eu.displayName");
        }
        if ("au".equals(dataCenter)) {
            return Messages.getString("SSOClientUtil.au.displayName");
        }
    
        if (System.getProperty(SSOClientUtil.DATA_CENTER_DISPLAY_KEY) != null) {
            return System.getProperty(SSOClientUtil.DATA_CENTER_DISPLAY_KEY);
        }
    
        return dataCenter;
    }

}
