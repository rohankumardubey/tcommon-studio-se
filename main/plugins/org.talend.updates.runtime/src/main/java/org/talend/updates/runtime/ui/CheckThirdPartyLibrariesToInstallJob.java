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
package org.talend.updates.runtime.ui;

import org.eclipse.swt.widgets.Display;

public class CheckThirdPartyLibrariesToInstallJob {

    public void checkInstallThirdPartyLibraries() {
        ThirdPartyLibrariesWizard thirdPartyLibrariesWizard = new ThirdPartyLibrariesWizard(null);
        thirdPartyLibrariesWizard.show(Display.getDefault().getActiveShell());

    }
}
