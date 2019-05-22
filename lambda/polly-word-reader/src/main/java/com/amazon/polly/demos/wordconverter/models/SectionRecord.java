package com.amazon.polly.demos.wordconverter.models;

import java.util.ArrayList;
import java.util.List;

public class SectionRecord {
    public String sectionName;
    public List<SectionItemRecord> sectionItemRecords;

    public SectionRecord(String sectionName, List<SectionItemRecord> sectionItemRecords) {
        this.sectionName = sectionName;
        this.sectionItemRecords = sectionItemRecords;
    }

    public SectionRecord() {
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public List<SectionItemRecord> getSectionItemRecords() {
        return sectionItemRecords;
    }

    public void setSectionItemRecords(List<SectionItemRecord> sectionItemRecords) {
        this.sectionItemRecords = sectionItemRecords;
    }

    public SectionRecord addRecord(SectionItemRecord record) {
        if(this.sectionItemRecords == null) {
            this.sectionItemRecords = new ArrayList<>();
        }
        this.sectionItemRecords.add(record);
        return this;
    }
}
