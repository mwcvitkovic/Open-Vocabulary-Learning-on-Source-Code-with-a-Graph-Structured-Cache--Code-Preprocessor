// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
package com.amazon.javaparser.dloc.utils;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.OtherLinkType;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.ForeachStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.google.common.collect.ImmutableList;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by sbadal on 2/19/18.
 */
public class NodeUtils {

    public static List<Node> getNodesByTypes(List<Node> nodeList, List<Class<? extends Node>> classList) {
        return nodeList.stream().filter(node -> isNodeOfType(node, classList)).collect(Collectors.toList());
    }

    public static boolean isNodeOfType(final Node node, final List<Class<? extends Node>> classList) {
        for(Class<? extends Node> clazz: classList) {
            if (clazz.isInstance(node)) {
                return true;
            }
        }
        return false;
    }

    public static Optional<Node> getOneChildNodeByTypes(Node node, List<Class<? extends Node>> classes, boolean forceCheck) {
        List<Node> collect = node.getOthersNodes().get(OtherLinkType.AST).stream().filter(n -> {
            for (Class<? extends Node> clazz: classes) {
                if (clazz.isInstance(n)) return true;
            }
            return false;
        }).collect(Collectors.toList());
        if (collect.size() > 1 && forceCheck) {
            throw new RuntimeException("More than one child node not expected.");
        } else if (collect.size() == 0) {
            return Optional.empty();
        }
        return Optional.of(collect.get(0));
    }

    public static Optional<Node> getOneChildNodeByTypes(Node node, List<Class<? extends Node>> classes) {
        return NodeUtils.getOneChildNodeByTypes(node, classes, true);
    }

    public static Optional<Node> getOneChildNodeByType(Node node, Class<? extends Node> clazz, boolean forceCheck) {
        return NodeUtils.getOneChildNodeByTypes(node, ImmutableList.of(clazz), forceCheck);
    }

    public static Optional<Node> getOneChildNodeByType(Node node, Class<? extends Node> clazz) {
        return NodeUtils.getOneChildNodeByTypes(node, ImmutableList.of(clazz), true);
    }

    public static Optional<Node> getParentNodeOfType(Node node, Class clazz) {
        Optional<Node> parentNode = node.getParentNode();
        if (parentNode.isPresent() && clazz.isInstance(parentNode.get())) {
            return Optional.of(parentNode.get());
        }
        return Optional.empty();
    }

    public static Optional<Node> getLexicalParentNode(Node node) {
        return getParentNodeOfTypeRecursively(node, ImmutableList.of(BlockStmt.class, ForStmt.class, DoStmt.class,
                ForeachStmt.class, WhileStmt.class, MethodDeclaration.class, LambdaExpr.class));
    }

    public static Optional<Node> getParentNodeOfTypeRecursively(Node node, Class clazz) {
        return getParentNodeOfTypeRecursively(node, ImmutableList.of(clazz));
    }

    public static Optional<Node> getParentNodeOfTypeRecursively(Node node, List<Class> classes) {
        Optional<Node> parentNode = node.getParentNode();
        if (!parentNode.isPresent()) {
            return Optional.empty();
        }
        for (Class clazz : classes) {
            if (clazz.isInstance(parentNode.get())) {
                return Optional.of(parentNode.get());
            }
        }
        return getParentNodeOfTypeRecursively(parentNode.get(), classes);
    }

    public static List<Node> reorderByInstanceType(List<Node> nodeList, List<Class<? extends Node>> classList) {
        return nodeList.stream().sorted(Comparator.comparingInt(o -> {
            int i = 0;
            for (Class<? extends Node> clazz : classList) {
                if (clazz.isInstance(o)) {
                    return i;
                }
                i++;
            }
            throw new RuntimeException("Not expected");
        })).collect(Collectors.toList());
    }
}
