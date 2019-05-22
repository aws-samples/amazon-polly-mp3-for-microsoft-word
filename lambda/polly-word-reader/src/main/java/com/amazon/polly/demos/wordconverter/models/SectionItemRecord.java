package com.amazon.polly.demos.wordconverter.models;

public class SectionItemRecord {
    String itemTopic;
    ItemText itemText;

    public SectionItemRecord(String itemTopic, ItemText itemText) {
        this.itemTopic = itemTopic;
        this.itemText = itemText;
    }

    public SectionItemRecord() {
    }

    public String getItemTopic() {
        return itemTopic;
    }

    public void setItemTopic(String itemTopic) {
        this.itemTopic = itemTopic;
    }

    public ItemText getItemText() {
        return itemText;
    }

    public void setItemText(ItemText itemText) {
        this.itemText = itemText;
    }
}
