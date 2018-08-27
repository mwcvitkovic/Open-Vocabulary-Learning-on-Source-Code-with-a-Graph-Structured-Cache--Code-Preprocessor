// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
package com.amazon.javaparser.dloc.preprocessor;

import com.amazon.javaparser.dloc.utils.NodeUtils;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.OtherLinkType;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by sbadal on 2/20/18.
 */
public class LastReadWritePreProcessor {

    public void process(List<Node> listNode) {
        for (Node node : listNode) {
            Set<Integer> visited = new TreeSet<>();
            process(node, visited);
        }
    }

    private void process(Node node, Set<Integer> visited) {
        processNode(node, visited);
        List<Node> childNodes = getChildNodes(node);
        if (node instanceof AssignExpr) {
            childNodes = Lists.reverse(childNodes);
        }
        for (Node child : childNodes) {
            process(child, visited);
        }
    }

    private void processNode(Node node, Set<Integer> visited) {
        parseLocalVariableTokenInNode(node, visited);
        visited.add(node.hashCode(true));
    }

    private List<Node> getChildNodes(Node node) {
        return node.getChildNodes();
    }

    private void parseLocalVariableTokenInNode(Node node, Set<Integer> visited) {
        if (isLocalVariable(node)) {
            Optional<Node> nameNode = NodeUtils.getOneChildNodeByType(node, SimpleName.class);
            if (!nameNode.isPresent()) {
                throw new RuntimeException("Not expected.");
            }
            Optional<Node> parentNode = NodeUtils.getLexicalParentNode(node);
            if (!parentNode.isPresent()) {
                throw new RuntimeException("Not expected.");
            }
            createLastRead((SimpleName) nameNode.get(), parentNode.get(), ImmutableSet.copyOf(visited));
        }
    }

    private boolean isLocalVariable(Node node) {
        return node instanceof Parameter ||
                (node instanceof VariableDeclarator && node.getParentNode().isPresent()
                        && !(node.getParentNode().get() instanceof FieldDeclaration));
    }

    private static List<Class<? extends Node>> forStmtOrderList = ImmutableList.of(
            VariableDeclarationExpr.class,
            AssignExpr.class,
            BinaryExpr.class,
            Statement.class,
            UnaryExpr.class);

    private static List<Class<? extends Node>> forStmtReAddList = ImmutableList.of(
            BinaryExpr.class);

    private static List<Class<? extends Node>> whileStmtReAddList = ImmutableList.of(
            BinaryExpr.class);

    private static List<Class<? extends Node>> doStmtReAddList = ImmutableList.of(
            Statement.class);

    private SimpleName createLastRead(SimpleName token, Node node, Set<Integer> visited) {
        if (node instanceof ThisExpr) {
            return token;
        }
        if (!visited.contains(node.hashCode(true)) && token.hashCode(true ) != node.hashCode(true)) {
            if (node instanceof SimpleName) {
                SimpleName nameNode = (SimpleName) node;
                if (token.getIdentifier().equals(nameNode.getIdentifier())) {
                    token.getOthersNodes().get(OtherLinkType.LAST_READ).add(node);
                    return nameNode;
                } else {
                    return token;
                }
            }
        }
        List<Node> nodeList = getChildNodes(node);
        if (node instanceof ForStmt) {
            nodeList = NodeUtils.reorderByInstanceType(nodeList, forStmtOrderList);
            nodeList.addAll(NodeUtils.getNodesByTypes(nodeList, forStmtReAddList));
        } else if (node instanceof WhileStmt) {
            nodeList = new ArrayList<>(nodeList);
            nodeList.addAll(NodeUtils.getNodesByTypes(nodeList, whileStmtReAddList));
        } else if (node instanceof DoStmt) {
            nodeList = new ArrayList<>(nodeList);
            nodeList.addAll(NodeUtils.getNodesByTypes(nodeList, doStmtReAddList));
        } else if (node instanceof IfStmt) {
            nodeList = new ArrayList<>(nodeList);
            nodeList.addAll(NodeUtils.getNodesByTypes(nodeList, doStmtReAddList));
        } else if(node instanceof AssignExpr) {
            if (nodeList.size() != 2) {
                throw new RuntimeException("Not expected.");
            }
            nodeList = ImmutableList.of(nodeList.get(1), nodeList.get(0));
        }
        return createLastReadForChild(token, visited, nodeList);
    }

    private SimpleName createLastReadForChild(SimpleName token, final Set<Integer> visited,
            final List<Node> nodeList) {
        for (Node child : nodeList) {
            token = createLastRead(token, child, visited);
        }
        return token;
    }

    public static int main() {
        int i = 20, j = 10, p = 0;
        if (i < j) {
            int q = 0;
            j = i - 1;
        } else if(i == j) {
            j = i + 1;
        } else {
            j = i;
        }
        return j;
    }

}
