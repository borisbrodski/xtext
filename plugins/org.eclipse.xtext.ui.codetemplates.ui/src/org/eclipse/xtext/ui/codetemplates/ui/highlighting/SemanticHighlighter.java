/*******************************************************************************
 * Copyright (c) 2010 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.ui.codetemplates.ui.highlighting;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateVariableResolver;
import org.eclipse.xtext.Grammar;
import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.ILeafNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.codetemplates.templates.Codetemplate;
import org.eclipse.xtext.ui.codetemplates.templates.Codetemplates;
import org.eclipse.xtext.ui.codetemplates.templates.TemplatePart;
import org.eclipse.xtext.ui.codetemplates.templates.TemplatesPackage;
import org.eclipse.xtext.ui.codetemplates.templates.Variable;
import org.eclipse.xtext.ui.codetemplates.ui.evaluator.EvaluatedTemplate;
import org.eclipse.xtext.ui.codetemplates.ui.registry.LanguageRegistry;
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightedPositionAcceptor;
import org.eclipse.xtext.ui.editor.syntaxcoloring.ISemanticHighlightingCalculator;
import org.eclipse.xtext.ui.editor.templates.ContextTypeIdHelper;

import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 */
public class SemanticHighlighter implements ISemanticHighlightingCalculator {

	@Inject
	private LanguageRegistry registry;
	
	public void provideHighlightingFor(XtextResource resource, final IHighlightedPositionAcceptor acceptor) {
		if (resource == null || resource.getContents().isEmpty())
			return;
		Codetemplates templates = (Codetemplates) resource.getContents().get(0);
		Grammar grammar = templates.getLanguage();
		if (grammar != null && !grammar.eIsProxy()) {
			TemplateBodyHighlighter highlighter = getHighlighter(grammar);
			if (highlighter != null) {
				ContextTypeIdHelper helper = registry.getContextTypeIdHelper(grammar);
				ContextTypeRegistry contextTypeRegistry = registry.getContextTypeRegistry(grammar);
				for(Codetemplate template: templates.getTemplates()) {
					if (template.getBody() != null) {
						final EvaluatedTemplate evaluatedTemplate = new EvaluatedTemplate(template);
						highlighter.provideHighlightingFor(evaluatedTemplate.getMappedString(), new IHighlightedPositionAcceptor() {
							public void addPosition(int offset, int length, String... id) {
								int beginOffset = evaluatedTemplate.getOriginalOffset(offset);
								int endOffset = evaluatedTemplate.getOriginalOffset(offset + length);
								int fixedLength = endOffset - beginOffset;
								acceptor.addPosition(beginOffset, fixedLength, id);
							}
						});
						String id = null;
						TemplateContextType contextType = null;
						if (template.getContext() != null) {
							id = helper.getId(template.getContext());
							if (id != null)
								contextType = contextTypeRegistry.getContextType(id);
						}
						Set<String> defaultResolvers = Sets.newHashSet();
						if (contextType != null) {
							Iterator<TemplateVariableResolver> resolvers = Iterators.filter(contextType.resolvers(), TemplateVariableResolver.class);
							while(resolvers.hasNext()) {
								TemplateVariableResolver resolver = resolvers.next();
								defaultResolvers.add(resolver.getType());
							}
						}
						for(TemplatePart part: template.getBody().getParts()) {
							if (part instanceof Variable) {
								Variable variable = (Variable) part;
								ICompositeNode node = NodeModelUtils.findActualNodeFor(variable);
								if (node != null) {
									for(ILeafNode leafNode: node.getLeafNodes()) {
										if (leafNode.getGrammarElement() instanceof Keyword) {
											acceptor.addPosition(leafNode.getTotalOffset(), leafNode.getTotalLength(), TemplatesHighlightingConfiguration.TEMPLATE_VARIABLE);		
										}
									}
									List<INode> typeNodes = NodeModelUtils.findNodesForFeature(variable, TemplatesPackage.Literals.VARIABLE__TYPE);
									if (typeNodes.isEmpty()) {
										if (defaultResolvers.contains(variable.getName())) {
											List<INode> nameNodes = NodeModelUtils.findNodesForFeature(variable, TemplatesPackage.Literals.VARIABLE__NAME);
											for(INode nameNode: nameNodes) {
												highlightNode(nameNode, TemplatesHighlightingConfiguration.TEMPLATE_VARIABLE, acceptor);
											}
										}
									} else {
										for(INode typeNode: typeNodes) {
											highlightNode(typeNode, TemplatesHighlightingConfiguration.TEMPLATE_VARIABLE, acceptor);
										}
									}
									List<INode> parameterNodes = NodeModelUtils.findNodesForFeature(variable, TemplatesPackage.Literals.VARIABLE__PARAMETERS);
									for(INode parameterNode: parameterNodes) {
										highlightNode(parameterNode, TemplatesHighlightingConfiguration.TEMPLATE_VARIABLE_ARGUMENT, acceptor);
									}
								}
							}
						}
					}
				}
			}
		}
	}

	protected TemplateBodyHighlighter getHighlighter(Grammar grammar) {
		return registry.getTemplateBodyHighlighter(grammar);
	}

	/**
	 * Highlights the non-hidden parts of {@code node} with the style that is associated with {@code id}.
	 */
	protected void highlightNode(INode node, String id, IHighlightedPositionAcceptor acceptor) {
		if (node == null)
			return;
		if (node instanceof ILeafNode) {
			acceptor.addPosition(node.getOffset(), node.getLength(), id);
		} else {
			for (ILeafNode leaf : node.getLeafNodes()) {
				if (!leaf.isHidden()) {
					acceptor.addPosition(leaf.getOffset(), leaf.getLength(), id);
				}
			}
		}
	}
}
