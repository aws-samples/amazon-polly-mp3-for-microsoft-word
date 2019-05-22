package com.amazon.polly.demos.wordconverter;

import com.amazon.polly.demos.wordconverter.models.DocLevel;

public interface VoiceStrategy {
    String next(DocLevel docLevel);
}
