// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
package com.amazon.dummytest;

public class ForLoop {

    public static void main(List<String> listName) {
        List<String> output = new ArrayList<>();
        if (listName != null)
            for (String name : listName) output.add(name);
        return output;
    }

}