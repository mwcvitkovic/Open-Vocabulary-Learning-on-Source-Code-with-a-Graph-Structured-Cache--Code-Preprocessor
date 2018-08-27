// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
package com.amazon.javaparser.dloc.processor;

import com.amazon.javaparser.dloc.preprocessor.FieldPreProcessor;
import com.amazon.javaparser.dloc.preprocessor.ComputedFromPreProcessor;
import com.amazon.javaparser.dloc.preprocessor.LastFieldLexPreprocessor;
import com.amazon.javaparser.dloc.preprocessor.LastLexicalUsePreProcessor;
import com.amazon.javaparser.dloc.preprocessor.readwrite.LastReadWritePreProcessorV2;
import com.amazon.javaparser.dloc.preprocessor.NextTokenPreProcessor;
import com.amazon.javaparser.dloc.preprocessor.NodePreProcessor;
import com.amazon.javaparser.dloc.preprocessor.ReturnToTokenPreProcessor;
import com.amazon.javaparser.dloc.preprocessor.TypePreProcessor;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sbadal on 2/9/18.
 */
public class SourceProcessor {

    private NextTokenPreProcessor nextTokenPreProcessor;
    private TypePreProcessor typePreProcessor;
    private NodePreProcessor nodePreProcessor;
    private ReturnToTokenPreProcessor returnToTokenPreProcessor;
    private ComputedFromPreProcessor computedFromPreProcessor;
    private LastReadWritePreProcessorV2 lastReadWritePreProcessor;
    private LastLexicalUsePreProcessor lastLexicalUsePreProcessor;
    private FieldPreProcessor fieldPreProcessor;
    private LastFieldLexPreprocessor lastFieldLexPreprocessor;

    public SourceProcessor(TypePreProcessor typePreProcessor) {
        this.returnToTokenPreProcessor = new ReturnToTokenPreProcessor();
        this.nextTokenPreProcessor = new NextTokenPreProcessor();
        this.typePreProcessor = typePreProcessor;
        this.nodePreProcessor = new NodePreProcessor();
        this.lastReadWritePreProcessor = new LastReadWritePreProcessorV2();
        this.computedFromPreProcessor = new ComputedFromPreProcessor();
        this.lastLexicalUsePreProcessor = new LastLexicalUsePreProcessor();
        this.fieldPreProcessor = new FieldPreProcessor();
        this.lastFieldLexPreprocessor = new LastFieldLexPreprocessor();
    }

    public void process(Node node) throws IOException, ParseException {
        nodePreProcessor.process(node);
        lastFieldLexPreprocessor.process(node);
        fieldPreProcessor.process(node);
        lastLexicalUsePreProcessor.process(node);
        computedFromPreProcessor.process(node);
        lastReadWritePreProcessor.process(node);
        returnToTokenPreProcessor.process(node);
        nextTokenPreProcessor.process(node);
        typePreProcessor.process(node);
    }

    public Map<String, Node> parseSources(File file) throws IOException, ParseException {
        if (file.isFile()) {
            return parseSources("", new File[]{file});
        }
        return parseSources("", file.listFiles());
    }

    public Map<String, Node> parseSources(String key, File[] files) throws IOException, ParseException {
        Map<String, Node> modeMap = new HashMap<>();
        if (files == null) {
            return modeMap;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                modeMap.putAll(parseSources(getKey(key, file), file.listFiles()));
            } else {
                if (file.getName().endsWith(".java")) {
                    CompilationUnit cu = JavaParser.parse(file);
                    modeMap.put(getKey(key, file), cu);
                }
            }
        }
        return modeMap;
    }

    private String getKey(final String key, final File file) {
        if (key.isEmpty()) {
            return file.getName();
        }
        return key + "." + file.getName();
    }


}
