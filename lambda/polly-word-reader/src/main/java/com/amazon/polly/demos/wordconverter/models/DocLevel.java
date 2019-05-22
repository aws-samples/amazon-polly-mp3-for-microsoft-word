package com.amazon.polly.demos.wordconverter.models;

public enum DocLevel {
    section(0),
    topic(1),
    para(2);

    private Integer hierarchy;

    private DocLevel(final Integer hierarchy) {
        this.hierarchy = hierarchy;
    }

    public Integer getHierarchy() {
        return hierarchy;
    }
}
