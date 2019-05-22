package com.amazon.polly.demos.wordconverter.io;

import com.amazon.polly.demos.wordconverter.models.OutputContext;

import java.io.IOException;

public interface OutputStrategy {
    void saveFile(OutputContext outputContext) throws IOException;
}
