// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
package com.amazon.dummytest;

public class ForLoop2 {

    public static int main() {
        int sum = 0;
        for (int i=0; i<10; i++) {
            sum += i;
            sum += 1;
        }
        return sum;
    }

}