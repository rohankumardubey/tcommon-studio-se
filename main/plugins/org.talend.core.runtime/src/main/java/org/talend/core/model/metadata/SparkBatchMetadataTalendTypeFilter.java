package org.talend.core.model.metadata;

import java.util.Arrays;
import java.util.List;

public class SparkBatchMetadataTalendTypeFilter extends SparkMetadataTalendTypeFilter{

    public static List<String> dynamicTypeCompatibleComponents = Arrays.asList(
            "tDeltaLakeInput",
            "tDeltaLakeOutput",
            "tFileInputParquet",
            "tFileOutputParquet",
            "tJDBCInput",
            "tJDBCOutput", "tLogRow", "tSqlRow"
            );

    public SparkBatchMetadataTalendTypeFilter(String componentName) {
        super(componentName);
    }

    @Override
    protected List<String> getComponentSpecificTypes(String componentName) {
        if (dynamicTypeCompatibleComponents.contains(componentName)) {
            return  Arrays.asList(SparkMetadataTalendTypeFilter.DYNAMIC);
        }
        return null;
    }
}
