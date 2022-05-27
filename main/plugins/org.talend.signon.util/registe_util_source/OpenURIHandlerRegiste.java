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

import java.awt.Desktop;
import java.awt.desktop.OpenURIEvent;
import java.awt.desktop.OpenURIHandler;
import java.net.URI;

import org.talend.signon.util.listener.SignOnEventListener;

public class OpenURIHandlerRegiste {

    private final String STUDIO_SCHEME_URI_PREFIX = "talendstudio://code?";

    private final String CODE_PREFIX = "code=";

    public void registOpenURIHandler4Mac(final SignOnEventListener listener) {
        Desktop.getDesktop().setOpenURIHandler(new OpenURIHandler() {

            @Override
            public void openURI(OpenURIEvent e) {
                URI uri = e.getURI();
                if (uri != null & uri.toString().startsWith(STUDIO_SCHEME_URI_PREFIX)) {
                    String value = uri.toString().substring(STUDIO_SCHEME_URI_PREFIX.length());
                    String[] values = value.split("&");
                    for (String v : values) {
                        if (v != null && v.startsWith(CODE_PREFIX)) {
                            listener.loginStop(v.substring(CODE_PREFIX.length()));
                        }
                    }
                }
            }
        });
    }
}
