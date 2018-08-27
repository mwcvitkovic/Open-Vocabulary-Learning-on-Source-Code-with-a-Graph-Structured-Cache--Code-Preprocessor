// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
package com.amazon.javaparser.dloc.converter;

import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.ArrayAccessExpr;
import com.github.javaparser.ast.expr.ArrayCreationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.InstanceOfExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.LiteralStringValueExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.SuperExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithIdentifier;
import com.github.javaparser.ast.nodeTypes.NodeWithModifiers;
import com.github.javaparser.ast.stmt.AssertStmt;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.BreakStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ContinueStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.ForeachStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.LabeledStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.SwitchEntryStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.SynchronizedStmt;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.ast.type.UnknownType;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.ast.type.WildcardType;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by sbadal on 2/15/18.
 */
public class GraphAttributeParser {

    private static List<Class<? extends Node>> doNothingList = ImmutableList.of(CompilationUnit.class,
            PackageDeclaration.class,
            VariableDeclarator.class,
            ClassOrInterfaceType.class,
            Parameter.class,
            VoidType.class,
            BlockStmt.class,
            ExpressionStmt.class,
            FieldAccessExpr.class,
            ThisExpr.class,
            NameExpr.class,
            ReturnStmt.class,
            MarkerAnnotationExpr.class,
            ObjectCreationExpr.class,
            MethodCallExpr.class,
            VariableDeclarationExpr.class,
            LambdaExpr.class,
            UnknownType.class,
            IfStmt.class,
            BinaryExpr.class,
            NullLiteralExpr.class,
            ThrowStmt.class,
            ForStmt.class,
            WhileStmt.class,
            UnaryExpr.class,
            DoStmt.class,
            ArrayType.class,
            ArrayCreationExpr.class,
            ArrayCreationLevel.class,
            ArrayAccessExpr.class,
            CastExpr.class,
            EnclosedExpr.class,
            TryStmt.class,
            CatchClause.class,
            ExplicitConstructorInvocationStmt.class,
            WildcardType.class,
            ClassExpr.class,
            InstanceOfExpr.class,
            ConditionalExpr.class,
            ArrayInitializerExpr.class,
            ForeachStmt.class,
            SwitchStmt.class,
            SwitchEntryStmt.class,
            SingleMemberAnnotationExpr.class,
            TypeParameter.class,
            BreakStmt.class,
            SuperExpr.class,
            InitializerDeclaration.class,
            SynchronizedStmt.class,
            EnumDeclaration.class,
            EnumConstantDeclaration.class,
            ContinueStmt.class,
            LabeledStmt.class,
            AssertStmt.class,
            ImportDeclaration.class);

    public Map<String, String> getAttributes(Node node) {
        Map<String, String> attributes = new HashMap<>();
        if (node instanceof LiteralStringValueExpr) {
            setAttributes((LiteralStringValueExpr) node, attributes);
        }
        if (node instanceof NodeWithModifiers) {
            setAttributes((NodeWithModifiers<? extends Node>) node, attributes);
        }
        if (node instanceof AssignExpr) {
            setAttributes((AssignExpr) node, attributes);
        }
        if (node instanceof NodeWithIdentifier) {
            setAttributes((NodeWithIdentifier) node, attributes);
        }
        if (node instanceof ClassOrInterfaceDeclaration) {
            setAttributes((ClassOrInterfaceDeclaration) node, attributes);
        }
        if (node instanceof ImportDeclaration) {
            setAttributes((ImportDeclaration) node, attributes);
        }
        if (node instanceof PrimitiveType) {
            setAttributes((PrimitiveType) node, attributes);
        }
        if (node instanceof BooleanLiteralExpr) {
            setAttributes((BooleanLiteralExpr) node, attributes);
        }
        if (attributes.size() == 0 && !doNothingList.contains(node.getClass())) {
            System.out.println("Need to replace do nothing list for class:" + node.getClass().toString());
            //throw new RuntimeException("Not expected.");
        }
        return attributes;
    }

    private void setAttributes(final BooleanLiteralExpr node, final Map<String, String> attributes) {
        if (attributes.get("identifier") != null ){
            throw new RuntimeException("Not expected.");
        }
        attributes.put("identifier", Boolean.toString(node.getValue()));
    }

    private void setAttributes(NodeWithIdentifier node, Map<String, String> attributes) {
        if (attributes.get("identifier") != null ){
            throw new RuntimeException("Not expected.");
        }
        attributes.put("identifier", node.getIdentifier());
    }

    private void setAttributes(AssignExpr node, Map<String, String> attributes ) {
        attributes.put("identifier", node.getOperator().asString());
    }

    private void setAttributes(PrimitiveType node, Map<String, String> attributes) {
        if (attributes.get("identifier") != null ){
            throw new RuntimeException("Not expected.");
        }
        attributes.put("identifier", node.getType().asString());
    }

    private void setAttributes(LiteralStringValueExpr node, Map<String, String> attributes) {
        if (attributes.get("identifier") != null ){
           throw new RuntimeException("Not expected.");
        }
        attributes.put("identifier", node.getValue());
    }

    private void setAttributes(ImportDeclaration node, Map<String, String> attributes) {
        if (node.isStatic()) {
            setModifier(ImmutableList.of("static"), attributes);
        }
        if (node.isAsterisk()) {
            setModifier(ImmutableList.of("*"), attributes);
        }
    }

    private void setAttributes(ClassOrInterfaceDeclaration node, Map<String, String> attributes) {
        if (node.isInterface()) {
            setModifier(ImmutableList.of("interface"), attributes);
        }
    }

    private void setAttributes(NodeWithModifiers<? extends Node> nodeWithModifiers, Map<String, String> attributes) {
        List<String> modifiers_str = nodeWithModifiers.getModifiers().stream()
                .map(Modifier::asString).collect(Collectors.toList());
        setModifier(modifiers_str, attributes);
    }

    private void setModifier(List<String> modifiers_str, Map<String, String> attributes) {
        String modifier = StringUtils.join(modifiers_str, ",");
        String old_modifier = attributes.get("modifier");
        if (old_modifier != null) {
            modifier = old_modifier + "," + modifier;
        }
        attributes.put("modifier", modifier);
    }
}
