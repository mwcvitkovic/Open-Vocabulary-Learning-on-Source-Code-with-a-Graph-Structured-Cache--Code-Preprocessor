// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
package com.amazon.javaparser.dloc.preprocessor;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.OtherLinkType;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by sbadal on 2/20/18.
 */
public class ComputedFromPreProcessor extends AbstractPreProcessor {

    @Override
    protected List<Node> getChildNode(Node node) {
        return node.getChildNodes();
    }

    @Override
    protected void processNode(Node node) {
        if (node instanceof AssignExpr) {
            AssignExpr assign = (AssignExpr) node;
            List<SimpleName> values = traverse(assign.getValue());
            List<SimpleName> targets = traverse(assign.getTarget());
            if (targets.size() == 0) {
               throw new RuntimeException("Not expected.");
            }
            SimpleName target = targets.get(0);
            for (SimpleName computedFrom: values) {
                target.getOthersNodes().get(OtherLinkType.COMPUTED_FROM).add(computedFrom);
            }
        }
    }

    private List<SimpleName> traverse(Node node) {
        List<SimpleName> list = Lists.newArrayList();
        if (node instanceof NameExpr) {
            NameExpr nameExpr = (NameExpr) node;
            list.add(nameExpr.getName());
        } else if (node instanceof FieldAccessExpr) {
            FieldAccessExpr fieldAccessExpr = (FieldAccessExpr) node;
            list.add(fieldAccessExpr.getName());
        }
        for (Node childNode: node.getChildNodes()) {
            list.addAll(traverse(childNode));
        }
        return list;
    }


}
