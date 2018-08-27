// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
package com.amazon.javaparser.dloc.preprocessor;

import com.amazon.javaparser.dloc.utils.NodeUtils;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.OtherLinkType;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Created by sbadal on 4/6/18.
 */
public class LastFieldLexPreprocessor {

    private List<Class<? extends Node>> lexicalClasses = ImmutableList.of(MethodDeclaration.class, BlockStmt.class, LambdaExpr.class);

    public void process(Node node) {
        if (node instanceof ClassOrInterfaceDeclaration) {
            traverse(node, new Stack<>());
        }
        for(Node child: getChildNode(node)) {
            process(child);
        }
    }

    private List<Node> getChildNode(final Node node) {
        return node.getOthersNodes().get(OtherLinkType.AST).asList();
    }

    private void traverse(Node node, Stack<Map<String, Node>> classFieldStack) {
        List<Node> childNode = getChildNode(node);
        if (node instanceof ClassOrInterfaceDeclaration) {
            childNode = new ArrayList<>(childNode);
            ClassOrInterfaceDeclaration clazz = (ClassOrInterfaceDeclaration)node;
            Map<String, Node> fieldMap = new HashMap<>();
            for (FieldDeclaration fieldDeclaration : clazz.getFields()) {
                for (VariableDeclarator vd: fieldDeclaration.getVariables()) {
                    fieldMap.put("this." + vd.getNameAsString(), vd);
                }
                childNode.remove(fieldDeclaration);
            }
            classFieldStack.add(fieldMap);
            traverseChildNode(childNode, classFieldStack);
            classFieldStack.pop();
        } else if (NodeUtils.isNodeOfType(node, lexicalClasses)) {
            classFieldStack.add(new HashMap<>());
            traverseChildNode(childNode, classFieldStack);
            classFieldStack.pop();
        } else if (node instanceof FieldAccessExpr) {
            FieldAccessExpr fieldAccessExpr = (FieldAccessExpr) node;
            String use = fieldAccessExpr.toString();
            for(Map<String, Node> classField: classFieldStack) {
                if (classField.containsKey(use)) {
                    fieldAccessExpr.getOthersNodes().get(OtherLinkType.LAST_FIELD_LEX).add(classField.get(use));
                    classField.put(use, fieldAccessExpr);
                    return;
                }
            }
            classFieldStack.peek().put(use, fieldAccessExpr);
        } else {
            traverseChildNode(childNode, classFieldStack);
        }
    }

    private void traverseChildNode(List<Node> nodes, Stack<Map<String, Node>> fieldStack) {
        for(Node childNode: nodes) {
            traverse(childNode, fieldStack);
        }
    }

}
