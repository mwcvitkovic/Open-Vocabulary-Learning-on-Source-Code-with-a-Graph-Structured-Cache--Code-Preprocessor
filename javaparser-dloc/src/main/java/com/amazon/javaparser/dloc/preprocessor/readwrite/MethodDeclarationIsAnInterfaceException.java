// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
package com.amazon.javaparser.dloc.preprocessor.readwrite;

/**
 * Created by sbadal on 4/2/18.
 */
public class MethodDeclarationIsAnInterfaceException extends RuntimeException {

    public MethodDeclarationIsAnInterfaceException(final String message) {
        super(message);
    }
}
