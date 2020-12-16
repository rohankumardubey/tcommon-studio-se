package org.talend.core.model.components.filters;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.eclipse.emf.common.util.BasicEList;
import org.junit.Test;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;

public class PropertyComponentFilterTest {

    @Test
    public void testAccept() {
        String componentName = "MyComponent'";
        String paramName = "ParamName";
        String paramValue = "ParamValue";
        PropertyComponentFilter filterEqualsOk = new PropertyComponentFilter(componentName, paramName, paramValue);
        PropertyComponentFilter filterEqualsKo = new PropertyComponentFilter(componentName, paramName, paramValue+"ko");
        PropertyComponentFilter filterContainsOk = new PropertyComponentFilter(componentName, paramName, "amV", PropertyComponentFilter.containsOperator);
        PropertyComponentFilter filterContainsKo = new PropertyComponentFilter(componentName, paramName, "Values", PropertyComponentFilter.containsOperator);
        
        NodeType node = mock(NodeType.class);
        ElementParameterType param = mock(ElementParameterType.class);
        when(param.getName()).thenReturn(paramName);
        when(param.getValue()).thenReturn(paramValue);
        when(node.getComponentName()).thenReturn(componentName);
        when(node.getElementParameter()).thenReturn(new BasicEList<ElementParameterType>(Arrays.asList(param)));
        assertTrue(filterEqualsOk.accept(node));
        assertFalse(filterEqualsKo.accept(node));
        assertTrue(filterContainsOk.accept(node));
        assertFalse(filterContainsKo.accept(node));
    }
}
