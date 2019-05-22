package com.amazon.polly.demos.wordconverter;

import com.amazon.polly.demos.wordconverter.models.*;
import com.amazon.polly.demos.wordconverter.io.InputStrategy;
import com.amazon.polly.demos.wordconverter.io.OutputStrategy;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFStyles;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractSSMLProcessor implements IWordToSSML {

    @Override
    public boolean sectionStart(String text) {
        String trimmedText = text.trim();
        Stream<String> stream = sectionHeaders().stream();
        return stream.anyMatch(trimmedText::equalsIgnoreCase);
    }

    @Override
    public Map<String, SectionRecord> parseSections(InputStrategy strategy, InputContext inputContext) throws IOException {
        Map<String, SectionRecord> sectionRecordMap;
        try (InputStream is = strategy.fetchFile(inputContext)) {
            sectionRecordMap = parseSections(is);
        }
        return sectionRecordMap;
    }

    List<String> parseHeadings(InputStrategy strategy, InputContext inputContext) throws IOException {
        List<String> headings;
        try (InputStream is = strategy.fetchFile(inputContext)) {
            headings = parseHeadings(is);
        }
        return headings;
    }

    List<String> parseHeadings(InputStream is) throws IOException {
        List<String> headings;
        XWPFDocument xwpfDocument = new XWPFDocument(is);
        XWPFStyles styles = xwpfDocument.getStyles();
        List<XWPFParagraph> paragraphs = xwpfDocument.getParagraphs();
        headings = paragraphs.stream()
                .filter(p -> p.getStyleID() != null)
                .filter(p -> styles.getStyle(p.getStyleID()).getName().startsWith("heading 1"))
                .map(p -> p.getText().trim())
                .collect(Collectors.toList());
        return headings;
    }

    @Override
    public Map<String, SectionRecord> parseSections(InputStream is) throws IOException {
        XWPFDocument document = new XWPFDocument(is);

        Map<String, SectionRecord> sectionMap = new HashMap<>();
        SectionRecord sectionRecord = new SectionRecord();
        SectionItemRecord sectionItemRecord = new SectionItemRecord();
        ItemText itemText = new ItemText();
        String sectionName = "";
        String questionText = "";

        for (IBodyElement bodyElement : document.getBodyElements()) {
            if (bodyElement instanceof XWPFParagraph) {
                XWPFParagraph paragraph = (XWPFParagraph) bodyElement;
                String text = paragraph.getText();
                String tidyText = tidy(text);
                if (sectionStart(text)) {
                    updateSectionMapAndItemRecords(sectionMap, sectionRecord, sectionItemRecord, sectionName, itemText);
                    sectionRecord = new SectionRecord();
                    sectionName = tidyText.trim();
                    sectionRecord.setSectionName(sectionName);
                    sectionItemRecord = new SectionItemRecord();
                    itemText = new ItemText();
                    System.out.println(sectionName);
                } else if (itemStartSignal(text)) {
                    updateItemRecords(sectionRecord, sectionItemRecord, itemText);
                    itemText = new ItemText();
                    questionText = tidyText;
                    sectionItemRecord = new SectionItemRecord();
                    sectionItemRecord.setItemTopic(questionText);
                } else {
                    itemText.addParagraph(tidyText);
                }
            }
        }

        updateSectionMapAndItemRecords(sectionMap, sectionRecord, sectionItemRecord, sectionName, itemText);

        return sectionMap;
    }

    public String sanitizeFileName(String text) {
        return tidy(text).toLowerCase()
                .replace(" ", "-")
                .replace(",", "")
                .replace("(", "")
                .replace(")", "");
    }

    @Override
    public void saveSSML(Map<String, SectionRecord> sectionRecordMap,
                         OutputStrategy outputStrategy,
                         OutputContext outputContext,
                         VoiceStrategy voiceStrategy) throws IOException {

        int sectionNum = 0;
        for (String header: sectionHeaders()) {
            String sectionFile = String.format("section_%d_%s", ++sectionNum, sanitizeFileName(header));
            String sectionContent = generateSSMLBlock(header, new PollySSMLContext<>(1000, null));
            outputContext.setFileName(sectionFile);
            outputContext.setContent(sectionContent);
            outputContext.setVoiceId(voiceStrategy.next(DocLevel.section));
            outputStrategy.saveFile(outputContext);

            SectionRecord sectionRecord = sectionRecordMap.get(tidy(header));
            int topicNum = 0;
            for (SectionItemRecord item: sectionRecord.getSectionItemRecords()) {
                String topicFile = String.format("%s_topic_%d", sectionFile, ++topicNum);
                String topicContent = generateSSMLBlock(item.getItemTopic(), new PollySSMLContext<>(1000, null));
                outputContext.setFileName(topicFile);
                outputContext.setContent(topicContent);
                outputContext.setVoiceId(voiceStrategy.next(DocLevel.topic));
                outputStrategy.saveFile(outputContext);

                int itemRow = 0;
                for (String para: item.getItemText().getParagraphs()) {
                    String itemFile = String.format("%s_para_%d", topicFile, ++itemRow);
                    String itemContent = generateSSMLBlock(para, new PollySSMLContext<>(600, null));
                    outputContext.setFileName(itemFile);
                    outputContext.setContent(itemContent);
                    outputContext.setVoiceId(voiceStrategy.next(DocLevel.para));
                    outputStrategy.saveFile(outputContext);
                }
            }
        }
    }

    private void updateItemRecords(SectionRecord sectionRecord, SectionItemRecord sectionItemRecord, ItemText itemText) {
        sectionItemRecord.setItemText(itemText);
        if(sectionItemRecord.getItemTopic() != null || !sectionItemRecord.getItemText().getParagraphs().isEmpty()) {
            sectionRecord.addRecord(sectionItemRecord);
        }
    }

    private void updateSectionMapAndItemRecords(Map<String, SectionRecord> sectionMap,
                                                SectionRecord sectionRecord,
                                                SectionItemRecord sectionItemRecord,
                                                String sectionName,
                                                ItemText itemText) {
        sectionItemRecord.setItemText(itemText);
        sectionRecord.addRecord(sectionItemRecord);
        sectionMap.put(sectionName, sectionRecord);
    }


    public String generateSSMLBlock(String text, PollySSMLContext context) {
        String ssmlText = text;
        if (text == null) {
            ssmlText = " ";
        }
        return "<speak>\n" +
                tidy(ssmlText) +
                "\n<break time=\"" + context.getBreakMillis() + "ms\"/>\n" +
                "</speak>";
    }

    @Override
    public String tidy(String text) {
        return text.replace("&", "AND");
    }

    String convertStreamToString(InputStream is) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer)) != -1) {
            outputStream.write(buffer, 0, length);
        }
        return outputStream.toString(StandardCharsets.UTF_8.name());
    }

}
