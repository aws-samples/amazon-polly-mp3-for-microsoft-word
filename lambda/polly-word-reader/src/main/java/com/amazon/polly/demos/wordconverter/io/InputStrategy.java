package com.amazon.polly.demos.wordconverter.io;

import com.amazon.polly.demos.wordconverter.models.InputContext;

import java.io.IOException;
import java.io.InputStream;

public interface InputStrategy {
    InputStream fetchFile(InputContext inputContext) throws IOException;
}
