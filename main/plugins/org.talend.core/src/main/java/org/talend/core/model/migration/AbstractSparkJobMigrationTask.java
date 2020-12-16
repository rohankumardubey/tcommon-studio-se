// ============================================================================
//
// Copyright (C) 2006-2020 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.core.model.migration;

import java.util.Arrays;
import java.util.List;

import org.talend.core.model.repository.ERepositoryObjectType;

/**
 * Base class for  Spark jobs migration tasks  
 * 
 * @author lbourgeois
 *
 */
public abstract class AbstractSparkJobMigrationTask extends AbstractJobMigrationTask {

	@Override
    public List<ERepositoryObjectType> getTypes() {
	    // Spark jobs are stored under process_mr and process_storm folders
        return Arrays.asList(ERepositoryObjectType.PROCESS_MR, ERepositoryObjectType.PROCESS_STORM);
    }
}
