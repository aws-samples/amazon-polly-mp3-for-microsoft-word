package com.amazon.polly.demos.wordconverter.io;

import com.amazon.polly.demos.wordconverter.models.OutputContext;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileOutputStrategy implements OutputStrategy {
    @Override
    public void saveFile(OutputContext outputContext) throws IOException {
        try (FileWriter sectionWriter = new FileWriter(new File(outputContext.getDir(), outputContext.getFileName() + ".txt"))) {
            sectionWriter.write(outputContext.getContent());
            sectionWriter.flush();
        }
    }
}