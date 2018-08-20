package com.amazon.javaparser.dloc.converter;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.OtherLinkType;
import com.github.javaparser.ast.expr.SimpleName;
import com.google.common.collect.Lists;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import org.apache.commons.collections4.set.ListOrderedSet;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GraphManager {

    private TinkerGraph graph;
    private int id = 1;
    private GraphAttributeParser graphAttributeParser = new GraphAttributeParser();
    private Map<Integer, Vertex> vertexCache;

    public GraphManager() {
        graph = new TinkerGraph();
        vertexCache = new HashMap<>();
    }

    public void process(Node node) {
        List<Node> nodeList = collectNode(node);
        createAndCacheVertex(nodeList);
        processEdge(node);
    }

    public TinkerGraph getGraph() {
        return this.graph;
    }

    private void processEdge(Node node) {
        Vertex from = vertexCache.get(node.hashCode(true));
        for (Map.Entry<OtherLinkType, ListOrderedSet<Node>> entry : node.getOthersNodes().entrySet()) {
            if (entry.getKey() == OtherLinkType.TYPE) {
                List<String> referenceList = entry.getValue().asList().stream().map(n -> {
                    if (n instanceof SimpleName) {
                        return ((SimpleName) n).getIdentifier();
                    }
                    throw new RuntimeException("Not expected.");
                }).collect(Collectors.toList());
                setReference(from, referenceList);
            } else {
                for (Node child : entry.getValue().asList()) {
                    Vertex to = vertexCache.get(child.hashCode(true));
                    if (to == null) {
                        throw new RuntimeException("Not expected.");
                    }
                    Edge edge = from.addEdge(entry.getKey().name(), to);
                    edge.setProperty("type", entry.getKey().name());
                    if (entry.getKey() == OtherLinkType.AST) {
                        processEdge(child);
                    }
                }
            }
        }
    }

    private List<Node> collectNode(Node node) {
        List<Node> nodes = Lists.newArrayList(node);
        for (Node child : node.getOthersNodes().get(OtherLinkType.AST).asList()) {
            nodes.addAll(collectNode(child));
        }
        return nodes;
    }

    private void createAndCacheVertex(List<Node> nodeList) {
        nodeList.forEach(n -> {
            if (vertexCache.containsKey(n.hashCode(true))) {
                throw new RuntimeException("Not a tree.");
            }
            vertexCache.put(n.hashCode(true), createVertex(n));
        });
    }

    private Vertex createVertex(Node node) {
        Vertex vertex = graph.addVertex(id++);
        setAttributes(node, vertex);
        return vertex;
    }

    private void setAttributes(Node node, Vertex vertex) {
        vertex.setProperty("type", node.getClass().getSimpleName());
        if(node.getParentNode().isPresent()) {
            vertex.setProperty("parentType", node.getParentNode().get().getClass().getSimpleName());
        }
        vertex.setProperty("text", node.toString());
        for (Map.Entry<String, String> entry : graphAttributeParser.getAttributes(node).entrySet()) {
            vertex.setProperty(entry.getKey(), entry.getValue());
        }
    }

    private void setReference(Vertex vertex, List<String> references) {
        vertex.setProperty("reference", StringUtils.join(references, ","));
    }
}
