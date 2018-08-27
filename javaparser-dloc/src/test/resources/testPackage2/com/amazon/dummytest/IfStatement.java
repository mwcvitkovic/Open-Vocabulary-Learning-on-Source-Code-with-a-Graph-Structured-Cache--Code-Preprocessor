// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
package com.amazon.dummytest;

public class IfStatement {

    public static int main(int x) {
        int i = 20, j = 10;
        if (i < j) {
            j = i - 1;
        } else if (i == j) {
            j = i + 1;
        } else {
            j = i;
        }
        return j;
    }
}
