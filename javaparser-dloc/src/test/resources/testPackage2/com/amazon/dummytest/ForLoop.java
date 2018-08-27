// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
package com.amazon.dummytest;

public class ForLoop {

    public static int main() {
        int j = 10;
        for (int i=0; i<j; i++) {
            j = j - i;
        }
        return j;
    }

}