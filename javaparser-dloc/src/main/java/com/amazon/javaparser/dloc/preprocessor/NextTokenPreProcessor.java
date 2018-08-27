// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
package com.amazon.javaparser.dloc.preprocessor;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.OtherLinkType;

import java.util.List;

/**
 * Created by sbadal on 2/20/18.
 */
public class NextTokenPreProcessor extends AbstractPreProcessor {

    @Override
    protected List<Node> getChildNode(Node node) {
        return node.getOthersNodes().get(OtherLinkType.AST).asList();
    }

    @Override
    protected void processNode(Node node) {
        List<Node> childNodes = node.getOthersNodes().get(OtherLinkType.AST).asList();
        if (childNodes.size() > 1) {
            int i = 0;
            Node oldNode = null;
            for (Node currentNode : childNodes) {
                if (i != 0) {
                    oldNode.getOthersNodes().get(OtherLinkType.NEXT_TOKEN).add(currentNode);
                }
                oldNode = currentNode;
                i++;
            }
        }
    }
}
