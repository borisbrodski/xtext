/*******************************************************************************
 * Copyright (c) 2011 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.junit4.parameterized;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This class will be removed in the next release after 2.4.2
 * 
 * use org.xpect.runner.Xpect from www.xpect-tests.org instead.
 * 
 * @author Moritz Eysholdt - Initial contribution and API
 */
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@TestExpectationValidator(validator = TestExpectationValidator.NullTestResultValidator.class)
public @interface Xpect {
}
