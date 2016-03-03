/**
 *
 * $Id$
 */
package org.talend.designer.core.model.utils.emf.talendfile.validation;

import org.eclipse.emf.common.util.EMap;

import org.eclipse.emf.ecore.util.FeatureMap;

import org.talend.designer.core.model.utils.emf.talendfile.ConnectionType;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.NoteType;
import org.talend.designer.core.model.utils.emf.talendfile.ParametersType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.RequiredType;
import org.talend.designer.core.model.utils.emf.talendfile.SubjobType;

/**
 * A sample validator interface for {@link org.talend.designer.core.model.utils.emf.talendfile.DocumentRoot}.
 * This doesn't really do anything, and it's not a real EMF artifact.
 * It was generated by the org.eclipse.emf.examples.generator.validator plug-in to illustrate how EMF's code generator can be extended.
 * This can be disabled with -vmargs -Dorg.eclipse.emf.examples.generator.validator=false.
 */
public interface DocumentRootValidator {
    boolean validate();

    boolean validateMixed(FeatureMap value);
    boolean validateXMLNSPrefixMap(EMap value);
    boolean validateXSISchemaLocation(EMap value);
    boolean validateConnection(ConnectionType value);
    boolean validateContext(ContextType value);
    boolean validateElementParameter(ElementParameterType value);
    boolean validateNode(NodeType value);
    boolean validateNote(NoteType value);
    boolean validateParameters(ParametersType value);
    boolean validateProcess(ProcessType value);
    boolean validateRequired(RequiredType value);
    boolean validateSubjob(SubjobType value);
}
