// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
package com.amazon.javaparser.dloc.preprocessor;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.OtherLinkType;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.ThisExpr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Created by sbadal on 4/6/18.
 */
public class FieldPreProcessor {

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

    private void traverse(Node node, Stack<Map<String, SimpleName>> classFieldStack) {
        List<Node> childNode = getChildNode(node);
        if (node instanceof ClassOrInterfaceDeclaration) {
            childNode = new ArrayList<>(childNode);
            ClassOrInterfaceDeclaration clazz = (ClassOrInterfaceDeclaration)node;
            Map<String, SimpleName> fieldMap = new HashMap<>();
            for (FieldDeclaration fieldDeclaration : clazz.getFields()) {
                for (VariableDeclarator vd: fieldDeclaration.getVariables()) {
                    fieldMap.put(vd.getNameAsString(), vd.getName());
                }
                childNode.remove(fieldDeclaration);
            }
            classFieldStack.add(fieldMap);
            traverseChildNode(childNode, classFieldStack);
            classFieldStack.pop();
        } else if (node instanceof FieldAccessExpr) {
            FieldAccessExpr fieldAccessExpr = (FieldAccessExpr) node;
            if (fieldAccessExpr.getScope() instanceof ThisExpr) {
                SimpleName fieldName = fieldAccessExpr.getName();
                Map<String, SimpleName> classField = classFieldStack.peek();
                if (classField.containsKey(fieldName.getIdentifier())) {
                    fieldName.getOthersNodes().get(OtherLinkType.FIELD)
                            .add(classField.get(fieldName.getIdentifier()));
                }
            }
        } else {
            traverseChildNode(childNode, classFieldStack);
        }
    }

    private void traverseChildNode(List<Node> nodes, Stack<Map<String, SimpleName>> fieldStack) {
        for(Node childNode: nodes) {
            traverse(childNode, fieldStack);
        }
    }
}
