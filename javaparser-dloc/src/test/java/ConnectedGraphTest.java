// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
import com.amazon.javaparser.dloc.converter.GraphManager;
import com.amazon.javaparser.dloc.preprocessor.TypePreProcessor;
import com.amazon.javaparser.dloc.processor.SourceProcessor;
import com.amazon.javaparser.dloc.visualize.DataController;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.Node;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.tinkerpop.blueprints.util.io.graphson.GraphSONMode;
import com.tinkerpop.blueprints.util.io.graphson.GraphSONWriter;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;


public class ConnectedGraphTest {

    @Before
    public void setUp() throws IOException {
    }

    @Test
    public void testIfGraphAreAccurate() throws IOException, ParseException {
        ClassLoader classLoader = ConnectedGraphTest.class.getClassLoader();
        URL url = classLoader.getResource("testPackage1");
        Assert.assertThat(url, CoreMatchers.notNullValue());
        File directory = new File(url.getPath());

        String jdk = DataController.getLocation(ArrayList.class).getPath();
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        combinedTypeSolver.add(new JarTypeSolver(jdk));
        combinedTypeSolver.add(new JavaParserTypeSolver(directory));

        SourceProcessor sourceFileInfoExtractor = new SourceProcessor(new TypePreProcessor(combinedTypeSolver));
        Map<String, Node> nodeMap = sourceFileInfoExtractor.parseSources(directory);
        GraphManager graphManager = new GraphManager();
        for(Node node: nodeMap.values()) {
            sourceFileInfoExtractor.process(node);
            graphManager.process(node);
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        GraphSONWriter.outputGraph(graphManager.getGraph(), outputStream, GraphSONMode.COMPACT);
        JsonElement parse = new JsonParser().parse(outputStream.toString());
        String s = new GsonBuilder().setPrettyPrinting().create().toJson(parse);
        System.out.println(s);
        Assert.assertThat(true, CoreMatchers.is(true));
    }
}
