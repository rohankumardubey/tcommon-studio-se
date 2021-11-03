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
package org.talend.core.pendo;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * DOC jding  class global comment. Detailled comment
 */
public class PendoTrackSenderTest {

    @Test
    public void testGetBaseUrl() throws Exception {
        PendoTrackSender sender = PendoTrackSender.getInstance();
        String adminUrl = "https://tmc.int.cloud.talend.com/studio_cloud_connection";
        String expectBaseUrl = "https://api.int.cloud.talend.com";
        sender.setAdminUrl(adminUrl);
        assertEquals(expectBaseUrl, sender.getBaseUrl());
        sender.setApiBaseUrl(null);

        adminUrl = "https://tmc.us.cloud.talend.com/studio_cloud_connection";
        expectBaseUrl = "https://api.us.cloud.talend.com";
        sender.setAdminUrl(adminUrl);
        assertEquals(expectBaseUrl, sender.getBaseUrl());
        sender.setApiBaseUrl(null);

        adminUrl = "https://tmc.eu.cloud.talend.com/studio_cloud_connection";
        expectBaseUrl = "https://api.eu.cloud.talend.com";
        sender.setAdminUrl(adminUrl);
        assertEquals(expectBaseUrl, sender.getBaseUrl());
        sender.setApiBaseUrl(null);

        adminUrl = "https://tmc.ap.cloud.talend.com/studio_cloud_connection";
        expectBaseUrl = "https://api.ap.cloud.talend.com";
        sender.setAdminUrl(adminUrl);
        assertEquals(expectBaseUrl, sender.getBaseUrl());
        sender.setApiBaseUrl(null);

        adminUrl = "https://tmc.au.cloud.talend.com/studio_cloud_connection";
        expectBaseUrl = "https://api.au.cloud.talend.com";
        sender.setAdminUrl(adminUrl);
        assertEquals(expectBaseUrl, sender.getBaseUrl());
        sender.setApiBaseUrl(null);

        adminUrl = "https://tmc.us-west.cloud.talend.com/studio_cloud_connection";
        expectBaseUrl = "https://api.us-west.cloud.talend.com";
        sender.setAdminUrl(adminUrl);
        assertEquals(expectBaseUrl, sender.getBaseUrl());
        sender.setApiBaseUrl(null);
        sender.setAdminUrl(null);
    }

}
