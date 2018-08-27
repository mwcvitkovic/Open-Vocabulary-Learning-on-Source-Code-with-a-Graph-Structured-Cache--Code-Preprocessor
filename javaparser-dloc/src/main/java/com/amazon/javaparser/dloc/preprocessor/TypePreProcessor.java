// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
package com.amazon.javaparser.dloc.preprocessor;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.OtherLinkType;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.AnnotationMemberDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.BreakStmt;
import com.github.javaparser.ast.stmt.LabeledStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedArrayType;
import com.github.javaparser.resolution.types.ResolvedPrimitiveType;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.resolution.types.ResolvedTypeVariable;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.model.typesystem.LazyType;
import com.github.javaparser.symbolsolver.model.typesystem.ReferenceTypeImpl;
import com.google.common.collect.ImmutableList;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by sbadal on 2/16/18.
 */
public class TypePreProcessor extends AbstractPreProcessor {

    private TypeSolver typeSolver;
    //private ReferenceCache referenceCache;
    private PrintStream err = System.err;

    public TypePreProcessor(TypeSolver typeSolver) {
        this.typeSolver = typeSolver;
        //this.referenceCache = new ReferenceCache();
    }

    @Override
    protected List<Node> getChildNode(final Node node) {
        return node.getOthersNodes().get(OtherLinkType.AST).asList();
    }

    @Override
    protected void processNode(Node node) {
        node.getOthersNodes().get(OtherLinkType.TYPE).addAll(solve(node));
    }

    private List<SimpleName> solve(Node node) {
        if (node instanceof SimpleName) {
            Optional<Node> parentNode = node.getParentNode();
            if (!parentNode.isPresent()) {
                throw new RuntimeException("Not expected.");
            }
            Node parent = parentNode.get();
            if (parent instanceof ClassOrInterfaceDeclaration || parent instanceof EnumDeclaration
                    || parent instanceof AnnotationMemberDeclaration || parent instanceof MemberValuePair) {
                return solveTypeDecl(parent);
            } else if (parent instanceof ClassOrInterfaceType) {
                return ImmutableList.of(new SimpleName("nonQualifiedClassName"));
            } else if (parent instanceof VariableDeclarator) {
                return getType(parent);
            } else if (parent instanceof MethodDeclaration || parent instanceof ConstructorDeclaration) {
                return ImmutableList.of(new SimpleName("userDefinedMethodName"));
            } else if (parent instanceof MethodCallExpr) {
                return ImmutableList.of(new SimpleName("otherMethodCall"));
            } else if (parent instanceof EnumConstantDeclaration) {
                return ImmutableList.of(new SimpleName("userDefinedEnum"));
            } else if (parent instanceof Parameter) {
                return getType(parent);
            } else if (parent instanceof NameExpr || parent instanceof ObjectCreationExpr) {
                return getType(parent);
            } else if (parent instanceof FieldAccessExpr) {
                return getType(parent);
            } else if (parent instanceof TypeParameter) {
                return ImmutableList.of(new SimpleName("runtimeGenericType"));
            } else if (parent instanceof LabeledStmt) {
                return ImmutableList.of(new SimpleName("userLabels"));
            } else if (parent instanceof BreakStmt) {
                return ImmutableList.of(new SimpleName("userLabels"));
            } else if(parent instanceof AnnotationDeclaration) {
                return ImmutableList.of(new SimpleName("annotationDeclaration"));
            }

            System.out.println("TypePreProcessor: Not Expected parent node type:" + parent.getClass().getName());
            //throw new RuntimeException("Not Expected parent node type:" + parent.getClass().getName());
        }
        return ImmutableList.of();
    }

    private List<SimpleName> getType(final Node node) {
        try {
            ResolvedType ref = JavaParserFacade.get(typeSolver).getType(node);
            if (ref instanceof ReferenceTypeImpl) {
                ReferenceTypeImpl referenceType = (ReferenceTypeImpl) ref;
                return ImmutableList.of(new SimpleName(referenceType.getQualifiedName()));
            } else if (ref instanceof ResolvedPrimitiveType || ref instanceof ResolvedArrayType) {
                return ImmutableList.of(new SimpleName(ref.describe()));
            } else if (ref instanceof ResolvedTypeVariable || ref instanceof LazyType) {
                return ImmutableList.of(new SimpleName("genericType"));
            } else {
                throw new RuntimeException("Not implemented for:" + ref.getClass().getName());
            }
        } catch (UnsolvedSymbolException ex) {
            err.println(ex.getMessage());
            return getUnknownReference();
        } catch (UnsupportedOperationException upe) {
            err.println(upe.getMessage());
            return getUnknownReference();
        } catch (RuntimeException re) {
            err.println(re.getMessage());
            return getUnknownReference();
        }
    }

    private ImmutableList<SimpleName> getUnknownReference() {
        return ImmutableList.of(new SimpleName("UnknownType"));
    }

    private List<SimpleName> solveTypeDecl(Node node) {
        try {
            ResolvedTypeDeclaration typeDeclaration = JavaParserFacade.get(typeSolver).getTypeDeclaration(node);
            List<SimpleName> nodeList = new ArrayList<>();
            if (typeDeclaration.isClass()) {
                for (ResolvedReferenceType sc : typeDeclaration.asClass().getAllSuperClasses()) {
                    nodeList.add(new SimpleName(sc.getQualifiedName()));
                }
                for (ResolvedReferenceType sc : typeDeclaration.asClass().getAllInterfaces()) {
                    nodeList.add(new SimpleName(sc.getQualifiedName()));
                }
            }
            return nodeList;
        } catch (UnsolvedSymbolException ex) {
            err.println(ex.getMessage());
            return getUnknownReference();
        } catch (UnsupportedOperationException upe) {
            err.println(upe.getMessage());
            return getUnknownReference();
        } catch (RuntimeException re) {
            err.println(re.getMessage());
            return getUnknownReference();
        }
    }

}
