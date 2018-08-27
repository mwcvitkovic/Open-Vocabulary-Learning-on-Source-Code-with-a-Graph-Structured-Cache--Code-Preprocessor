// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
package com.amazon.javaparser.dloc.preprocessor.readwrite;

import com.amazon.javaparser.dloc.utils.NodeUtils;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.OtherLinkType;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.ForeachStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

/**
 * Created by sbadal on 3/21/18.
 */
public class LastReadWritePreProcessorV2 {

    private List<Class<? extends Node>> lexicalClasses = ImmutableList.of(MethodDeclaration.class, BlockStmt.class, LambdaExpr.class);

    public void process(Node node) {
        if (NodeUtils.isNodeOfType(node, lexicalClasses)) {
            Stack<LexicalNode> lexicalNodes = new Stack<>();
            final LexicalNode lexicalNode;
            try {
                lexicalNode = LexicalNodeFactory.getLexicalNode(node);
            } catch (MethodDeclarationIsAnInterfaceException e) {
                // Don't do anything for such cases
                return;
            }
            lexicalNodes.push(lexicalNode);
            traverse(getChildNode(lexicalNode.getNode()), lexicalNodes);
        } else {
            for (Node child : getChildNode(node)) {
                process(child);
            }
        }
    }


    private List<Node> getChildNode(final Node node) {
        return node.getOthersNodes().get(OtherLinkType.AST).asList();
    }

    private void traverse(List<Node> nodeList, Stack<LexicalNode> lexicalNodes) {
        traverse(nodeList, lexicalNodes, false);
    }

    private void traverse(List<Node> nodeList, Stack<LexicalNode> lexicalNodes, boolean newWrite)
            throws MethodDeclarationIsAnInterfaceException {
        for (Node node : nodeList) {
            traverse(node, lexicalNodes, newWrite);
        }
    }

    private void traverse(Node node, Stack<LexicalNode> lexicalNodes) {
        traverse(node, lexicalNodes, false);
    }

    private void traverse(Node node, Stack<LexicalNode> lexicalNodes, boolean newWrite) {
        List<Node> childNode = getChildNode(node);
        if (node instanceof VariableDeclarator) {
            Optional<Node> nameNode = NodeUtils.getOneChildNodeByType(node, SimpleName.class, false);
            if (!nameNode.isPresent()) {
                throw new RuntimeException("Not expected.");
            }
            lexicalNodes.peek().addVariable((SimpleName) nameNode.get());
            childNode = new ArrayList<>(childNode);
            childNode.remove(nameNode.get());
            traverse(childNode, lexicalNodes);
        } else if (node instanceof ForStmt) {
            ForStmt forStmt = (ForStmt) node;
            lexicalNodes.push(LexicalNodeFactory.getLexicalNode(node));
            traverse(Lists.newArrayList(forStmt.getInitialization()), lexicalNodes);
            if (forStmt.getCompare().isPresent()) {
                traverse(forStmt.getCompare().get(), lexicalNodes);
            }
            stackLexicalNode(lexicalNodes);
            traverse(forStmt.getBody(), lexicalNodes);
            traverse(Lists.newArrayList(forStmt.getUpdate()), lexicalNodes);
            if (forStmt.getCompare().isPresent()) {
                traverse(forStmt.getCompare().get(), lexicalNodes);
            }
            deStackLexicalNode(lexicalNodes, true);
            lexicalNodes.pop();
        } else if (node instanceof WhileStmt) {
            WhileStmt whileStmt = (WhileStmt)node;
            traverse(Lists.newArrayList(whileStmt.getCondition()), lexicalNodes);
            stackLexicalNode(lexicalNodes);
            traverse(Lists.newArrayList(whileStmt.getBody()), lexicalNodes);
            traverse(Lists.newArrayList(whileStmt.getCondition()), lexicalNodes);
            deStackLexicalNode(lexicalNodes, true);
        } else if (node instanceof DoStmt) {
            DoStmt doStmt = (DoStmt) node;
            traverse(Lists.newArrayList(doStmt.getBody()), lexicalNodes);
            traverse(Lists.newArrayList(doStmt.getCondition()), lexicalNodes);
            stackLexicalNode(lexicalNodes);
            traverse(Lists.newArrayList(doStmt.getBody()), lexicalNodes);
            deStackLexicalNode(lexicalNodes, true);
        } else if (node instanceof ForeachStmt) {
            ForeachStmt foreachStmt = (ForeachStmt) node;
            lexicalNodes.push(LexicalNodeFactory.getLexicalNode(node));
            traverse(foreachStmt.getIterable(), lexicalNodes);
            traverse(foreachStmt.getVariable(), lexicalNodes);
            traverse(foreachStmt.getBody(), lexicalNodes);
            lexicalNodes.pop();
        } else if (node instanceof UnaryExpr) {
            traverse(childNode, lexicalNodes, true);
        } else if (node instanceof AssignExpr) {
            AssignExpr assign = (AssignExpr) node;
            if (childNode.size() != 2) {
                throw new RuntimeException("Not expected.");
            }
            traverse(assign.getValue(), lexicalNodes, false);
            traverse(assign.getTarget(), lexicalNodes, true);
        } else if (NodeUtils.isNodeOfType(node, lexicalClasses)) {
            lexicalNodes.push(LexicalNodeFactory.getLexicalNode(node));
            traverse(childNode, lexicalNodes);
            lexicalNodes.pop();
        } else if (node instanceof IfStmt) {
            IfStmt ifStmt = (IfStmt) node;
            traverse(Lists.newArrayList(ifStmt.getCondition()), lexicalNodes);
            stackLexicalNode(lexicalNodes);
            traverse(Lists.newArrayList(ifStmt.getThenStmt()), lexicalNodes);
            if (ifStmt.getElseStmt().isPresent()) {
                for (LexicalNode lexicalNode : lexicalNodes) {
                    lexicalNode.shuffleStackVariable();
                }
                traverse(Lists.newArrayList(ifStmt.getElseStmt().get()), lexicalNodes);
            }
            deStackLexicalNode(lexicalNodes, false);
        } else if (node instanceof SimpleName) {
            SimpleName nameNode = (SimpleName) node;
            for (LexicalNode lexicalNode : lexicalNodes) {
                if (lexicalNode.hasVariable(nameNode)) {
                    lexicalNode.setAndUpdateVariablePointer(nameNode, OtherLinkType.LAST_READ);
                    if (newWrite) {
                        lexicalNode.setAndUpdateVariablePointer(nameNode, OtherLinkType.LAST_WRITE);
                    } else {
                        lexicalNode.setVariablePointer(nameNode, OtherLinkType.LAST_WRITE);
                    }
                    break;
                }
            }
        } else if (node instanceof ThisExpr) {
            // Do nothing
        } else {
            traverse(childNode, lexicalNodes, newWrite);
        }
    }

    private void deStackLexicalNode(Stack<LexicalNode> lexicalNodes, boolean completeLoop) {
        for (LexicalNode lexicalNode : lexicalNodes) {
            lexicalNode.destackVariable(completeLoop);
        }
    }

    private void stackLexicalNode(Stack<LexicalNode> lexicalNodes) {
        for (LexicalNode lexicalNode : lexicalNodes) {
            lexicalNode.stackVariable();
        }
    }

}
