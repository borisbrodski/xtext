/*******************************************************************************
 * Copyright (c) 2012 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.xbase.tests.typesystem

import com.google.inject.Inject
import org.eclipse.xtend.lib.Property
import org.eclipse.xtext.xbase.tests.AbstractXbaseTestCase
import org.junit.Before
import org.junit.Test
import org.eclipse.xtext.common.types.TypesFactory
import org.eclipse.xtext.xbase.XbaseFactory

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 */
class ResolvedTypesTest extends AbstractXbaseTestCase {
	@Inject 
	@Property 
	PublicReentrantTypeResolver resolver
	
	TypesFactory typesFactory = TypesFactory::eINSTANCE
	
	XbaseFactory xbaseFactory = XbaseFactory::eINSTANCE
	
	@Property
	PublicResolvedTypes testMe
	
	@Before
	def void initResolvedTypes() {
		testMe = new PublicResolvedTypes(resolver)
	}
	
	@Test(expected=typeof(UnsupportedOperationException))
	def void testDiagnostics() {
		testMe.queuedDiagnostics
	}
	
	@Test(expected=typeof(UnsupportedOperationException))
	def void testActualTypeArguments() {
		testMe.getActualTypeArguments(null)
	}
	
	@Test
	def void testCreateAndGetUnboundTypeParameter() {
		val unbound = testMe.createUnboundTypeParameter(xbaseFactory.createXFeatureCall, typesFactory.createJvmTypeParameter)
		assertSame(unbound, testMe.getUnboundTypeParameter(unbound.handle))
	}
	
	@Test(expected=typeof(IllegalStateException))
	def void testTryGetUnknownUnboundTypeParameter() {
		testMe.getUnboundTypeParameter('unknown handle')
	}
}