// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
package com.amazon.javaparser.dloc.preprocessor.readwrite;

import com.amazon.javaparser.dloc.utils.NodeUtils;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.OtherLinkType;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.SimpleName;
import org.apache.commons.collections4.set.ListOrderedSet;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.TreeSet;

/**
 * Created by sbadal on 4/2/18.
 */
public class LexicalNode {

    private Node node;

    private Map<String, Map<OtherLinkType, Stack<VariableSet>>> variableLastOccurrence;

    public LexicalNode(Node node) {
        this.node = node;
        this.variableLastOccurrence = new HashMap<>();
    }

    public void addVariable(SimpleName node) {
        Map<OtherLinkType, Stack<VariableSet>> map = new HashMap<>();
        map.put(OtherLinkType.LAST_READ, createStackOfVariableSet(node));
        map.put(OtherLinkType.LAST_WRITE, createStackOfVariableSet(node));
        this.variableLastOccurrence.put(node.getIdentifier(), map);
    }

    public void addParameter(Parameter parameter) {
        Optional<Node> nameNode = NodeUtils.getOneChildNodeByType(parameter, SimpleName.class);
        if (!nameNode.isPresent()) {
            throw new RuntimeException("Not expected.");
        }
        SimpleName simpleName = (SimpleName) nameNode.get();
        this.addVariable(simpleName);
    }

    public boolean hasVariable(SimpleName nameNode){
        return this.variableLastOccurrence.containsKey(nameNode.getIdentifier());
    }

    public void setAndUpdateVariablePointer(SimpleName nameNode, OtherLinkType linkType) {
        Map<OtherLinkType, Stack<VariableSet>> otherLinkTypeListMap = this.variableLastOccurrence
                .get(nameNode.getIdentifier());
        Stack<VariableSet> simpleNames = otherLinkTypeListMap.get(linkType);
        VariableSet variableSet = simpleNames.peek();
        for(SimpleName oldNameNode: variableSet.getSimpleNameSet()) {
            nameNode.getOthersNodes().get(linkType).add(oldNameNode);
        }
        variableSet.replace(nameNode);
    }

    public void setVariablePointer(SimpleName nameNode, OtherLinkType linkType) {
        Map<OtherLinkType, Stack<VariableSet>> otherLinkTypeListMap = this.variableLastOccurrence
                .get(nameNode.getIdentifier());
        Stack<VariableSet> simpleNames = otherLinkTypeListMap.get(linkType);
        VariableSet variableSet = simpleNames.peek();
        for(SimpleName oldNameNode: variableSet.getSimpleNameSet()) {
            nameNode.getOthersNodes().get(linkType).add(oldNameNode);
        }
    }

    public void stackVariable() {
        for (Map<OtherLinkType, Stack<VariableSet>> variable: variableLastOccurrence.values()) {
            for(Stack<VariableSet> stack: variable.values()) {
                stack.push(createVariableSet(stack.peek()));
            }
        }
    }

    public void shuffleStackVariable() {
        for (Map<OtherLinkType, Stack<VariableSet>> variable: variableLastOccurrence.values()) {
            for(Stack<VariableSet> stack: variable.values()) {
                VariableSet pop1 = stack.pop();
                VariableSet pop2 = stack.pop();
                stack.push(pop1);
                stack.push(pop2);
            }
        }
    }

    public void destackVariable(boolean closeLoop) {
        for (Map<OtherLinkType, Stack<VariableSet>> variable: variableLastOccurrence.values()) {
            for (Map.Entry<OtherLinkType, Stack<VariableSet>> entry : variable.entrySet()) {
                Stack<VariableSet> stack = entry.getValue();
                VariableSet pop = stack.pop();
                SimpleName firstAccessed = pop.getFirstNameNode();
                if (closeLoop && firstAccessed != null) {
                    List<SimpleName> simpleNames = pop.getSimpleNameSet().asList();
                    SimpleName lastAccessed = simpleNames.get(simpleNames.size() - 1);
                    firstAccessed.getOthersNodes().get(entry.getKey()).add(lastAccessed);
                }
                for(SimpleName nameNode: pop.getSimpleNameSet()) {
                    if(stack.size() == 0) {
                        throw new RuntimeException("Not expected.");
                    }
                    VariableSet peek = stack.peek();
                    peek.add(nameNode);
                }
            }
        }
    }

    public Node getNode() {
        return this.node;
    }

    private static ListOrderedSet<SimpleName> getOrderedSet() {
        return ListOrderedSet.listOrderedSet(new TreeSet<>(Comparator.comparingInt(n -> n.hashCode(true))));
    }

    private static VariableSet createVariableSet(VariableSet variableSet) {
        ListOrderedSet<SimpleName> treeSet = getOrderedSet();
        treeSet.addAll(variableSet.getSimpleNameSet());
        return new VariableSet(treeSet);
    }

    private static VariableSet createVariableSet(SimpleName nameNode) {
        return new VariableSet(nameNode);
    }

    private static ListOrderedSet<SimpleName> createNameNodeSet(SimpleName nameNode) {
        ListOrderedSet<SimpleName> treeSet = getOrderedSet();
        treeSet.add(nameNode);
        return treeSet;
    }

    private static Stack<VariableSet> createStackOfVariableSet(final SimpleName node) {
        Stack<VariableSet> lastReadStack = new Stack<>();
        lastReadStack.push(createVariableSet(node));
        return lastReadStack;
    }

    public static class VariableSet {

        private ListOrderedSet<SimpleName> simpleNameSet;
        private SimpleName firstNameNode;

        public VariableSet(SimpleName simpleName) {
            simpleNameSet = LexicalNode.createNameNodeSet(simpleName);
        }

        public VariableSet(ListOrderedSet<SimpleName> simpleNameSet) {
            this.simpleNameSet = simpleNameSet;
        }

        public void replace(SimpleName simpleName) {
            if (this.firstNameNode == null) {
                this.firstNameNode = simpleName;
            }
            simpleNameSet = LexicalNode.createNameNodeSet(simpleName);
        }

        public boolean add(SimpleName simpleName) {
            return simpleNameSet.add(simpleName);
        }

        public ListOrderedSet<SimpleName> getSimpleNameSet() {
            return simpleNameSet;
        }

        public SimpleName getFirstNameNode() {
            return firstNameNode;
        }
    }
}
