/*******************************************************************************
 * Copyright (c) 2012 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.xbase.typesystem.util;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.xtext.common.types.JvmTypeParameter;
import org.eclipse.xtext.xbase.typesystem.references.ITypeReferenceOwner;
import org.eclipse.xtext.xbase.typesystem.references.LightweightBoundTypeArgument;
import org.eclipse.xtext.xbase.typesystem.references.LightweightMergedBoundTypeArgument;
import org.eclipse.xtext.xbase.typesystem.references.LightweightTypeReference;
import org.eclipse.xtext.xbase.typesystem.references.ParameterizedTypeReference;
import org.eclipse.xtext.xbase.typesystem.references.UnboundTypeReference;
import org.eclipse.xtext.xbase.typesystem.references.WildcardTypeReference;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 * TODO JavaDoc, toString - focus on differences to ActualTypeArgumentCollector and UnboundTypeParameterAwareTypeArgumentCollector
 */
@NonNullByDefault
public class ExpectationTypeParameterHintCollector extends DeferredTypeParameterHintCollector {

	protected class DeferredParameterizedTypeReferenceTraverser extends ParameterizedTypeReferenceTraverser {
		@Override
		public void doVisitUnboundTypeReference(UnboundTypeReference reference,
				ParameterizedTypeReference declaration) {
			boolean constraintSeen = false;
			boolean constraintsMatch = true;
			boolean othersSeen = false;
			if (reference.getTypeParameter() != declaration.getType()) {
				List<LightweightBoundTypeArgument> hints = reference.getAllHints();
				for(int i = 0; i < hints.size(); i++) {
					LightweightBoundTypeArgument hint = hints.get(i);
					if (hint.getSource() == BoundTypeArgumentSource.CONSTRAINT) {
						constraintSeen = true;
						outerVisit(hint.getTypeReference(), declaration, hint.getSource(), hint.getDeclaredVariance(), hint.getActualVariance());
						if (constraintsMatch && !hint.getTypeReference().isAssignableFrom(declaration)) {
							constraintsMatch = false;
						}
					} else {
						othersSeen = true;
						// we don't break the list traversal here since we want to do the paired outerVisit for all constraints
					}
				}
			} else {
				if (getOwner().getDeclaredTypeParameters().contains(reference.getTypeParameter())) {
					reference.acceptHint(declaration, BoundTypeArgumentSource.RESOLVED, this, VarianceInfo.INVARIANT, VarianceInfo.INVARIANT);
					return;
				}
			}
			if (constraintSeen && constraintsMatch && !othersSeen) {
				reference.acceptHint(declaration, BoundTypeArgumentSource.RESOLVED, this, VarianceInfo.INVARIANT, VarianceInfo.INVARIANT);
			} else if (!constraintSeen && !reference.internalIsResolved() && declaration.isResolved() && !getOwner().isResolved(reference.getHandle()) && reference.canResolveTo(declaration)) {
				reference.acceptHint(declaration, BoundTypeArgumentSource.RESOLVED, this, VarianceInfo.INVARIANT, VarianceInfo.INVARIANT);
			} else {
				reference.tryResolve();
				if (reference.internalIsResolved()) {
					outerVisit(reference, declaration);
				} else {
					addHint(reference, declaration);
				}
			}
		}

		@Override
		protected boolean shouldProcessInContextOf(JvmTypeParameter declaredTypeParameter, Set<JvmTypeParameter> boundParameters,
				Set<JvmTypeParameter> visited) {
			if (getOwner().getDeclaredTypeParameters().contains(declaredTypeParameter))
				return true;
			if (boundParameters.contains(declaredTypeParameter) && !visited.add(declaredTypeParameter)) {
				return false;
			}
			return true;
		}
	}
	
	protected class DeferredWildcardTypeReferenceTraverser extends WildcardTypeReferenceTraverser {
		@Override
		public void doVisitUnboundTypeReference(UnboundTypeReference reference,
				WildcardTypeReference declaration) {
			if (declaration.getLowerBound() == null) {
				if (!reference.internalIsResolved()) {
					List<LightweightTypeReference> upperBounds = declaration.getUpperBounds();
					for(LightweightTypeReference upperBound: upperBounds) {
						if (!upperBound.isResolved() || !reference.canResolveTo(upperBound)) {
							super.doVisitUnboundTypeReference(reference, declaration);
							return;
						}
					}
					reference.tryResolve();
					if (reference.internalIsResolved()) {
						outerVisit(reference, declaration);
					} else {
						addHint(reference, declaration);
					}
					return;
				}
			}
			super.doVisitUnboundTypeReference(reference, declaration);
		}
	}

	public ExpectationTypeParameterHintCollector(ITypeReferenceOwner owner) {
		super(owner);
	}
	
	@Override
	protected TypeParameterSubstitutor<?> createTypeParameterSubstitutor(
			Map<JvmTypeParameter, LightweightMergedBoundTypeArgument> mapping) {
		return new UnboundTypeParameterPreservingSubstitutor(mapping, getOwner());
	}
	
	@Override
	protected WildcardTypeReferenceTraverser createWildcardTypeReferenceTraverser() {
		return new DeferredWildcardTypeReferenceTraverser();
	}
	
	@Override
	protected ParameterizedTypeReferenceTraverser createParameterizedTypeReferenceTraverser() {
		return new DeferredParameterizedTypeReferenceTraverser();
	}
	
}