// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
package com.amazon.javaparser.dloc.preprocessor;

import com.amazon.javaparser.dloc.utils.NodeUtils;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.resolution.types.ResolvedPrimitiveType;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.google.common.collect.ImmutableList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by sbadal on 2/19/18.
 */
public class ReferenceCache {

    private Map<String, Node> referenceCache;

    public ReferenceCache() {
        this.referenceCache = new HashMap<>();
    }

    public void put(ClassOrInterfaceDeclaration node) {
        Optional<Node> parent = NodeUtils.getParentNodeOfType(node, CompilationUnit.class);
        String qualifiedName;
        if(!parent.isPresent() && ((CompilationUnit)parent.get()).getPackageDeclaration().isPresent()) {
            qualifiedName = ((CompilationUnit)parent.get()).getPackageDeclaration().get().getName() + "." + node.getNameAsString();
        } else {
            qualifiedName = node.getNameAsString();

        }
        this.referenceCache.put(qualifiedName, node);
    }

    public List<Node> getOrCreate(ResolvedPrimitiveType referenceType) {
        String name = referenceType.describe();
        if (!referenceCache.containsKey(name)) {
            referenceCache.put(name, createPrimitiveNode(name));
        }
        return ImmutableList.of(referenceCache.get(name));
    }

    public List<Node> getOrCreate(ResolvedReferenceType sc) {
        String className = sc.getQualifiedName();
        if (!referenceCache.containsKey(className)) {
            Node node = JavaParser.parseClassOrInterfaceType(className);
            replaceReference(node);
            referenceCache.put(className, node);
        }
        return ImmutableList.of(referenceCache.get(className));
    }

    private Node createPrimitiveNode(String name) {
        switch (name) {
            case "boolean":
                return PrimitiveType.booleanType();
            case "int":
                return PrimitiveType.intType();
            case "byte":
                return PrimitiveType.byteType();
            case "char":
                return PrimitiveType.charType();
            case "double":
                return PrimitiveType.doubleType();
            case "float":
                return PrimitiveType.floatType();
            case "long":
                return PrimitiveType.longType();
            case "short":
                return PrimitiveType.shortType();
            default:
                throw new RuntimeException("Not implemented");
        }
    }

    private void replaceReference(Node node) {
        List<Node> childNodes = node.getChildNodes();
        List<Node> newChildNode = childNodes.stream().map(childNode -> {
            if (childNode instanceof ClassOrInterfaceType) {
                ClassOrInterfaceType classType = (ClassOrInterfaceType) childNode;
                String className = classType.asString(false);
                if (!referenceCache.containsKey(className)) {
                    referenceCache.put(className, classType);
                }
                replaceReference(childNode);
                return referenceCache.get(className);
            } else {
                return childNode;
            }
        }).collect(Collectors.toList());
        node.setChildNodes(newChildNode);
    }
}
