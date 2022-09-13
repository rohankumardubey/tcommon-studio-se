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
package org.talend.commons.runtime.service;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.talend.commons.exception.ExceptionHandler;

/**
 * DOC cmeng  class global comment. Detailled comment
 */
public interface ITalendThemeService {

    Object getGlobalThemeColor(String cssProp);

    String getGlobalThemeProp(String key);

    static ITalendThemeService get() {
        try {
            BundleContext bc = FrameworkUtil.getBundle(ITalendThemeService.class).getBundleContext();
            ServiceReference<ITalendThemeService> serviceReference = bc.getServiceReference(ITalendThemeService.class);
            if (serviceReference == null) {
                return null;
            }
            return bc.getService(serviceReference);
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        return null;
    }

}
