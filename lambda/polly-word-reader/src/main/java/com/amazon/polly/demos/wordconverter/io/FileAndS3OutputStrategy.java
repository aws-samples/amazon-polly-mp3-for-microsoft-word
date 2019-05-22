package com.amazon.polly.demos.wordconverter.io;

import com.amazon.polly.demos.wordconverter.models.OutputContext;

import java.io.IOException;

public class FileAndS3OutputStrategy implements OutputStrategy {
    @Override
    public void saveFile(OutputContext outputContext) throws IOException {
        new FileOutputStrategy().saveFile(outputContext);
        new S3OutputStrategy().saveFile(outputContext);
    }
}
