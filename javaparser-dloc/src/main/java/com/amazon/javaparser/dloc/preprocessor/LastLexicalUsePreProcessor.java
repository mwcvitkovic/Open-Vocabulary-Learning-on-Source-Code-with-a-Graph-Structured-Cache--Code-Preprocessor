// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
package com.amazon.javaparser.dloc.preprocessor;

import com.amazon.javaparser.dloc.utils.NodeUtils;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.OtherLinkType;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;

/**
 * Created by sbadal on 4/6/18.
 */
public class LastLexicalUsePreProcessor {

    private List<Class<? extends Node>> lexicalClasses = ImmutableList.of(MethodDeclaration.class, BlockStmt.class, LambdaExpr.class);

    private List<Node> getChildNode(final Node node) {
        return node.getOthersNodes().get(OtherLinkType.AST).asList();
    }

    public void process(Node node) {
        if (NodeUtils.isNodeOfType(node, lexicalClasses)) {
            processLexicalChildNode(node, new Stack<>());
        } else {
            for(Node child: getChildNode(node)) {
                process(child);
            }
        }
    }

    private void processLexicalChildNode(Node node, Stack<Map<String, SimpleName>> variableStack) {
        List<Node> childNode = getChildNode(node);
        if (NodeUtils.isNodeOfType(node, lexicalClasses)) {
            variableStack.add(new HashMap<>());
            processLexicalChildNode(childNode, variableStack);
            variableStack.pop();
        } else if (node instanceof SimpleName) {
            SimpleName simpleName = (SimpleName)node;
            for(Map<String, SimpleName> variable: variableStack) {
                if (variable.containsKey(simpleName.getIdentifier())) {
                    simpleName.getOthersNodes().get(OtherLinkType.LAST_LEXICAL_SCOPE_USE).add(variable.get(simpleName.getIdentifier()));
                    variable.put(simpleName.getIdentifier(), simpleName);
                    break;
                }
            }
        } else if (node instanceof Parameter) {
            Optional<Node> nameNode = NodeUtils.getOneChildNodeByType(node, SimpleName.class);
            if (!nameNode.isPresent()) {
                throw new RuntimeException("Not expected.");
            }
            SimpleName simpleName = (SimpleName) nameNode.get();
            variableStack.peek().put(simpleName.getIdentifier(), simpleName);
        } else if (node instanceof VariableDeclarator) {
            Optional<Node> nameNode = NodeUtils.getOneChildNodeByType(node, SimpleName.class, false);
            if (!nameNode.isPresent()) {
                throw new RuntimeException("Not expected.");
            }
            SimpleName simpleName = (SimpleName)nameNode.get();
            variableStack.peek().put(simpleName.getIdentifier(), simpleName);
            childNode = new ArrayList<>(childNode);
            childNode.remove(nameNode.get());
            processLexicalChildNode(childNode, variableStack);
        } else if (node instanceof FieldAccessExpr) {
            // Do Nothing
        } else {
            processLexicalChildNode(childNode, variableStack);
        }
    }

    private void processLexicalChildNode(List<Node> childNode, Stack<Map<String, SimpleName>> variableStack) {
        for(Node child: childNode) {
            processLexicalChildNode(child, variableStack);
        }
    }
}
