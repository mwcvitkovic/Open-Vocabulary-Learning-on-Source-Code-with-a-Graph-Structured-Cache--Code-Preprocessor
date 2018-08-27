// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
package com.amazon.javaparser.dloc.preprocessor;

import com.amazon.javaparser.dloc.utils.NodeUtils;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.OtherLinkType;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.google.common.collect.ImmutableList;

import java.util.Optional;

/**
 * Created by sbadal on 2/20/18.
 */
public class ReturnToTokenPreProcessor extends AbstractPreProcessor {

    @Override
    protected void processNode(Node node) {
        if(node instanceof ReturnStmt) {
            Optional<Node> methodDeclaration = NodeUtils.getParentNodeOfTypeRecursively(node, ImmutableList.of(MethodDeclaration.class,
                    ConstructorDeclaration.class));
            if(!methodDeclaration.isPresent()) {
                throw new RuntimeException("Not expected.");
            }
            node.getOthersNodes().get(OtherLinkType.RETURNS_TO).add(methodDeclaration.get());
        }
    }
}
