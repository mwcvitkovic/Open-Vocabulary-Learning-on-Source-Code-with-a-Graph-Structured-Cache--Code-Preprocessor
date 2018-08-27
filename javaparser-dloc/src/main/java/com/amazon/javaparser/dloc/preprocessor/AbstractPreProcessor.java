// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
package com.amazon.javaparser.dloc.preprocessor;

import com.github.javaparser.ast.Node;

import java.util.List;

/**
 * Created by sbadal on 2/19/18.
 */
public abstract class AbstractPreProcessor {

    public void process(Node node) {
        processNode(node);
        for(Node child: getChildNode(node)) {
            process(child);
        }
    }

    protected List<Node> getChildNode(final Node node) {
        return node.getChildNodes();
    }

    protected abstract void processNode(Node node);
}
