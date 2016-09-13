package com.equinox.imports.fixture;

public class PathFinder {

    private final String fileName;

    public PathFinder(String fileName) {
        this.fileName = fileName;
    }

    public String getPath() {
        return getClass().getClassLoader().getResource(fileName).getPath();
    }
}
