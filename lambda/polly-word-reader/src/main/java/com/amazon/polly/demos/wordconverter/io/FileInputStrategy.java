package com.amazon.polly.demos.wordconverter.io;

import com.amazon.polly.demos.wordconverter.models.InputContext;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileInputStrategy implements InputStrategy {
    @Override
    public InputStream fetchFile(InputContext inputContext) throws IOException {
        return new FileInputStream(inputContext.getFilePath());
    }
}
