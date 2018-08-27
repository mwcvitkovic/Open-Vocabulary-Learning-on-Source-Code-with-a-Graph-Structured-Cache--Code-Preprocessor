// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
package com.amazon.javaparser.dloc.preprocessor.readwrite;

import com.amazon.javaparser.dloc.utils.NodeUtils;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.ForeachStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.google.common.collect.ImmutableList;

import java.util.Optional;

/**
 * Created by sbadal on 4/2/18.
 */
public class LexicalNodeFactory {

    public static LexicalNode getLexicalNode(Node node) throws MethodDeclarationIsAnInterfaceException {
        if (node instanceof MethodDeclaration) {
            MethodDeclaration method = (MethodDeclaration) node;
            Optional<Node> block = NodeUtils.getOneChildNodeByType(node, BlockStmt.class);
            if (!block.isPresent()) {
                throw new MethodDeclarationIsAnInterfaceException("Method is part of interface.");
            }
            LexicalNode lexicalNode = new LexicalNode(block.get());
            for (Parameter parameter : method.getParameters()) {
                lexicalNode.addParameter(parameter);
            }
            return lexicalNode;
        } else if(node instanceof BlockStmt) {
            return new LexicalNode(node);
        } else if(node instanceof LambdaExpr) {
            LambdaExpr lambda = (LambdaExpr) node;
            Optional<Node> statement = NodeUtils.getOneChildNodeByTypes(node, ImmutableList.of(Statement.class,
                    Expression.class));
            if (!statement.isPresent()) {
                throw new RuntimeException("Not expected.");
            }
            LexicalNode lexicalNode = new LexicalNode(statement.get());
            for(Parameter parameter: lambda.getParameters()){
                lexicalNode.addParameter(parameter);
            }
            return lexicalNode;
        } else if(node instanceof ForStmt || node instanceof ForeachStmt) {
            return new LexicalNode(node);
        } else {
            throw new RuntimeException("Not implemented.");
        }
    }
}
