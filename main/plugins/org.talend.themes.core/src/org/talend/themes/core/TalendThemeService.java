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
package org.talend.themes.core;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.dom.WidgetElement;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.e4.ui.css.swt.theme.IThemeManager;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.runtime.service.ITalendThemeService;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSValue;

/**
 * DOC cmeng class global comment. Detailled comment
 */
public class TalendThemeService implements ITalendThemeService {

    private IThemeEngine themeEngine;

    @Override
    public Object getGlobalThemeColor(String cssProp) {
        Shell shell = Display.getDefault().getActiveShell();
        CSSValue cssValue = getCssValue(shell, cssProp);
        if (cssValue == null) {
            return null;
        }
        String key = cssValue.getCssText();
        ColorRegistry colorRegistry = JFaceResources.getColorRegistry();
        if (!colorRegistry.hasValueFor(key)) {
            CSSEngine cssEngin = WidgetElement.getEngine(shell);
            try {
                RGB rgb = (RGB) cssEngin.convert(cssValue, RGB.class, Display.getDefault());
                colorRegistry.put(key, rgb);
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
        }
        return colorRegistry.get(key);
    }

    @Override
    public String getGlobalThemeProp(String key) {
        Shell shell = Display.getDefault().getActiveShell();
        CSSValue cssValue = getCssValue(shell, key);
        if (cssValue == null) {
            return null;
        }
        return cssValue.getCssText();
    }

    private IThemeEngine getThemeEngine() {
        if (themeEngine == null) {
            BundleContext context = TalendThemesCorePlugin.getDefault().getBundle().getBundleContext();
            ServiceReference ref = context.getServiceReference(IThemeManager.class.getName());
            IThemeManager mgr = (IThemeManager) context.getService(ref);
            themeEngine = mgr.getEngineForDisplay(Display.getDefault());
        }
        return themeEngine;
    }

    private CSSValue getCssValue(Object element, String property) {
        IThemeEngine engine = getThemeEngine();
        if (engine == null) {
            return null;
        }
        CSSStyleDeclaration style = engine.getStyle(element);
        if (style == null) {
            return null;
        }
        return style.getPropertyCSSValue(property);
    }

}
