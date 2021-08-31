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
package org.talend.rcp.intro;

import org.eclipse.jface.notifications.AbstractNotificationPopup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;

/**
 * @author bhe created on Aug 31, 2021
 *
 */
public class NotificationPopUp extends AbstractNotificationPopup {

    private Display display;
    
    /**
     * @param display
     */
    public NotificationPopUp(Display display) {
        super(display);
        this.display = display;
    }
    
    @Override
    protected String getPopupShellTitle() {
        return "Update R2022-01 is available";
    }

    @Override
    protected void createContentArea(Composite parent) {
        Link link = new Link(parent, SWT.WRAP);
        link.setText("<a>More information</a>");
        
        Button btn = new Button(parent,SWT.NONE);
        btn.setText("Update now");
    }

    @Override
    public boolean close() {
        boolean closed = super.close();
        return closed;
    }
    
    protected Shell getParentShell() {
        return this.display.getActiveShell();
    }
}
