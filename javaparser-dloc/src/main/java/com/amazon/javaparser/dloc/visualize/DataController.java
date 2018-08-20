package com.amazon.javaparser.dloc.visualize;

import com.amazon.javaparser.dloc.converter.GraphManager;
import com.amazon.javaparser.dloc.preprocessor.TypePreProcessor;
import com.amazon.javaparser.dloc.processor.SourceProcessor;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.Node;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLWriter;
import com.tinkerpop.blueprints.util.io.graphson.GraphSONMode;
import com.tinkerpop.blueprints.util.io.graphson.GraphSONWriter;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Created by sbadal on 2/13/18.
 */
@RestController
public class DataController {

    @RequestMapping("/data")
    public String data(@RequestParam("file") String file) throws IOException, ParseException {
        File directory = new File(file);
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        String jdk = DataController.getLocation(ArrayList.class).getPath();
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
        TinkerGraph graph = graphManager.getGraph();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        GraphSONWriter.outputGraph(graph, outputStream, GraphSONMode.COMPACT);
        return new GsonBuilder().setPrettyPrinting().create().toJson(new JsonParser().parse(outputStream.toString()));
    }

    @RequestMapping("/process")
    public Set<String> process(@RequestParam("inputDir") String inputDir, @RequestParam("outputDir") String outputDir) throws IOException, ParseException {
        File sourceDirectory = new File(inputDir + "/sources");
        File dependencies = new File(inputDir + "/dependencies");
        File output = new File(outputDir);
        if (!sourceDirectory.exists() || !dependencies.exists() || !output.exists()) {
           throw new RuntimeException("Folder is not in specified format.");
        }

        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        String jdk = DataController.getLocation(ArrayList.class).getPath();
        combinedTypeSolver.add(new JarTypeSolver(jdk));
        for(File jar: dependencies.listFiles()) {
            combinedTypeSolver.add(new JarTypeSolver(jar.getAbsolutePath()));
        }
        combinedTypeSolver.add(new JavaParserTypeSolver(sourceDirectory));

        SourceProcessor sourceFileInfoExtractor = new SourceProcessor(new TypePreProcessor(combinedTypeSolver));
        Map<String, Node> parsedSource = sourceFileInfoExtractor.parseSources(sourceDirectory);
        for(Entry<String, Node> nodeEntry: parsedSource.entrySet()) {
            GraphManager graphManager = new GraphManager();
            Node node = nodeEntry.getValue();
            sourceFileInfoExtractor.process(node);
            graphManager.process(node);
            TinkerGraph graph = graphManager.getGraph();
            OutputStream outputStream = new FileOutputStream(new File(outputDir + File.separator + nodeEntry.getKey() + ".gml"));
            GraphMLWriter.outputGraph(graph, outputStream);
        }
        return parsedSource.keySet();
    }


    public static URL getLocation(final Class<?> c) {
        if (c == null) return null; // could not load the class

        // try the easy way first
        try {
            final URL codeSourceLocation =
                    c.getProtectionDomain().getCodeSource().getLocation();
            if (codeSourceLocation != null) return codeSourceLocation;
        } catch (final SecurityException e) {
            // NB: Cannot access protection domain.
        } catch (final NullPointerException e) {
            // NB: Protection domain or code source is null.
        }

        // NB: The easy way failed, so we try the hard way. We ask for the class
        // itself as a resource, then strip the class's path from the URL string,
        // leaving the base path.

        // get the class's raw resource path
        final URL classResource = c.getResource(c.getSimpleName() + ".class");
        if (classResource == null) return null; // cannot find class resource

        final String url = classResource.toString();
        final String suffix = c.getCanonicalName().replace('.', '/') + ".class";
        if (!url.endsWith(suffix)) return null; // weird URL

        // strip the class's path from the URL string
        final String base = url.substring(0, url.length() - suffix.length());

        String path = base;

        // remove the "jar:" prefix and "!/" suffix, if present
        if (path.startsWith("jar:")) path = path.substring(4, path.length() - 2);

        try {
            return new URL(path);
        } catch (final MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
