/*******************************************************************************
 * Copyright (c) 2010 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.common.types.ui.notification;

import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IReferenceDescription;
import org.eclipse.xtext.resource.impl.AbstractResourceDescription;

/**
 * Resource descriptions for Xtext's view on java types.
 * It contains descriptions for members of the mirrored type. The members
 * are indexed by their fully qualified name and not by signature.
 * @author Sebastian Zarnekow - Initial contribution and API
 */
public class TypeResourceDescription extends AbstractResourceDescription {

	private final URI uri;
	private final List<IEObjectDescription> exportedObjects;

	public TypeResourceDescription(URI uri, List<IEObjectDescription> exportedObjects) {
		this.uri = uri;
		this.exportedObjects = exportedObjects;
	}
	
	public Iterable<QualifiedName> getImportedNames() {
		return Collections.emptyList();
	}

	public Iterable<IReferenceDescription> getReferenceDescriptions() {
		return Collections.emptyList();
	}

	public URI getURI() {
		return uri;
	}

	@Override
	protected List<IEObjectDescription> computeExportedObjects() {
		return exportedObjects;
	}

}
