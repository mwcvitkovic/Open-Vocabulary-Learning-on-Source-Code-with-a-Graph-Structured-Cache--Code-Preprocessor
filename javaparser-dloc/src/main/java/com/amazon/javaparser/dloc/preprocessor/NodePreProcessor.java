// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
package com.amazon.javaparser.dloc.preprocessor;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.OtherLinkType;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.ExpressionStmt;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by sbadal on 2/19/18.
 */
public class NodePreProcessor extends AbstractPreProcessor {

    @Override
    protected void processNode(Node node) {
        List<Node> newChildNode = getChildNodes(node);
        List<Node> changedChildList = newChildNode.stream().map(n -> {
            if (n instanceof NameExpr) {
                return cleanNameExpression((NameExpr) n);
            } else if (n instanceof ExpressionStmt) {
                return cleanExpressionStatement((ExpressionStmt) n);
            } else {
                return n;
            }
        }).collect(Collectors.toList());
        node.getOthersNodes().get(OtherLinkType.AST).addAll(changedChildList);
    }

    private List<Node> getChildNodes(final Node node) {
        return node.getChildNodes().stream().filter(n -> {
                if (n instanceof LineComment) {
                    return false;
                } else if(n instanceof BlockComment) {
                    return false;
                } else if(n instanceof JavadocComment) {
                    return false;
                }
                return true;
            }).collect(Collectors.toList());
    }

    private Node cleanNameExpression(NameExpr node) {
        List<Node> childNodes = getChildNodes(node);
        if (childNodes.size() != 1 && !(childNodes.get(0) instanceof SimpleName)) {
            throw new RuntimeException("Not expected.");
        }
        return childNodes.get(0);
    }

    private Node cleanExpressionStatement(ExpressionStmt node) {
        List<Node> childNodes = getChildNodes(node);
        if (childNodes.size() != 1) {
            throw new RuntimeException("Not expected");
        }
        return childNodes.get(0);
    }
}
