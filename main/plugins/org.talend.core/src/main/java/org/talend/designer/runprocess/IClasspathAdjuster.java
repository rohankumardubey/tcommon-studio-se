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
package org.talend.designer.runprocess;

import java.util.Set;

import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.process.IProcess;

/**
 * created by wchen on Jul 20, 2017 Detailled comment
 *
 */
public interface IClasspathAdjuster {

    public void initialize();

    public void collectInfo(IProcess process, Set<ModuleNeeded> modules);

    @Deprecated
    public Set<ModuleNeeded> adjustClassPath(Set<ModuleNeeded> modulesToAjust);

    default public Set<ModuleNeeded> adjustClassPath(IProcess process, Set<ModuleNeeded> modulesToAjust)
    {
        return adjustClassPath(modulesToAjust);//modulesToAjust;
    };

    /**
     * Have a chance to add some modules when generate pom file
     */
    default public Set<ModuleNeeded> adjustPomGeneration(IProcess process, Set<ModuleNeeded> modulesToAjust) {
        return modulesToAjust;
    };
}
