package com.amazon.polly.demos.wordconverter.models;

import java.util.ArrayList;
import java.util.List;

public class ItemText {
    List<String> paragraphs;

    public ItemText() {
        this.paragraphs = new ArrayList<>();
    }

    public ItemText(List<String> paragraphs) {
        this.paragraphs = paragraphs;
    }

    public List<String> getParagraphs() {
        return paragraphs;
    }

    public void setParagraphs(List<String> paragraphs) {
        this.paragraphs = paragraphs;
    }

    public ItemText addParagraph(String paragraph) {
        if(paragraph != null && !paragraph.isEmpty()) {
            this.paragraphs.add(paragraph);
        }
        return this;
    }

    @Override
    public String toString() {
        return "ItemText{" +
                "paragraphs=" + paragraphs +
                '}';
    }
}
