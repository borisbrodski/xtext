/*
* generated by Xtext
*/
package org.eclipse.xtext.ui.tests.editor.bracketmatching.validation;
 
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EPackage;

public class AbstractBmTestLanguageJavaValidator extends org.eclipse.xtext.validation.AbstractDeclarativeValidator {

	@Override
	protected List<EPackage> getEPackages() {
	    List<EPackage> result = new ArrayList<EPackage>();
	    result.add(org.eclipse.xtext.ui.tests.editor.bracketmatching.bracketmatching.BracketmatchingPackage.eINSTANCE);
		return result;
	}

}
