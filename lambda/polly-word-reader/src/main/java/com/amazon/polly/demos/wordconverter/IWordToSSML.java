package com.amazon.polly.demos.wordconverter;

import com.amazon.polly.demos.wordconverter.models.InputContext;
import com.amazon.polly.demos.wordconverter.models.OutputContext;
import com.amazon.polly.demos.wordconverter.models.SectionRecord;
import com.amazon.polly.demos.wordconverter.models.PollySSMLContext;
import com.amazon.polly.demos.wordconverter.io.InputStrategy;
import com.amazon.polly.demos.wordconverter.io.OutputStrategy;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface IWordToSSML {
    /**
     * Method to identify and return section headers of the document e.g. Chapter 1, Chapter 2 etc.
     * This could be something provided externally or parsed from the document e.g. all heading1 text.
     * It is out of scope how the list is derived.
     * @return List of section headers
     */
    List<String> sectionHeaders();

    /**
     * Within a section, signal the start of a topic. It is a unique marker that determines that this is a new in the document section.
     * For example, this could be based on a prefix, .e.g. in a FAQ document it could be a if a line that contains "FAQ"
      * @param text a line in the word document
     * @return true if this is a new item
     */
    boolean itemStartSignal(String text);

    /**
     * Removes reserved characters from text. The default implementation only changes '&' to 'AND'
     * @param text a line in the word document
     * @return sanitized text
     */
    String tidy(String text);

    /**
     * Returns a map parsed SectionRecords from the document with Section name as the key
     * @param is InputStream to be parsed
     * @return Map of (Section name, SectionRecord) key-value pairs
     * @throws IOException Exception if there is an error reading the file
     */
    Map<String, SectionRecord> parseSections(InputStream is) throws IOException;

    /**
     * Returns a map parsed SectionRecords from the document with Section name as the key
     * @param strategy Strategy to find the input file - file or s3 key
     * @param inputContext Input context holding information about the file to fetch
     * @return Map of (Section name, SectionRecord) key-value pairs
     * @throws IOException Exception if there is an error reading the file
     */
    Map<String, SectionRecord> parseSections(InputStrategy strategy, InputContext inputContext) throws IOException;

    /**
     * Signals the start of a section in the document e.g. Chapter 1.
     * Default implementation returns true if text matches one of the items in sectionHeaders list.
     * @param text a line in the word document
     * @return true if this line starts a section
     */
    boolean sectionStart(String text);


    /**
     * Save SSML files from the map derived using parseSections method
     * @param sectionRecordMap Map of (Section name, SectionRecord) key-value pairs
     * @throws IOException Exception if there is an error saving the files
     */
    void saveSSML(Map<String, SectionRecord> sectionRecordMap,
                  OutputStrategy outputStrategy,
                  OutputContext outputContext,
                  VoiceStrategy voiceStrategy) throws IOException;

    /**
     * Generate a SSML string, passing in a context. By default the context only takes input of a break specified
     * in milliseconds. There is an additional map that can be used for future customizations and extensions
     * @param text a line/paragraph in the word document
     * @param context SSML transformation context e.g. break in milliseconds
     * @return ssml string representation of the text
     */
    String generateSSMLBlock(String text, PollySSMLContext context);
}
