package com.amazon.javaparser.dloc;

import com.amazon.javaparser.dloc.visualize.DataController;
import com.github.javaparser.ParseException;

import java.io.IOException;
import java.util.Set;

/**
 * Created by sbadal on 4/4/18.
 */
public class Main {

    public static void main(String[] args) throws IOException, ParseException {
        Set<String> files = new DataController().process(args[0], args[1]);
        System.out.println("Processed files:" + String.join(",", files));
    }
}
