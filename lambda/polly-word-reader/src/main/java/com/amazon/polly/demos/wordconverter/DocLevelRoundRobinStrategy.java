package com.amazon.polly.demos.wordconverter;

import com.amazon.polly.demos.wordconverter.models.DocLevel;
import com.amazon.polly.demos.wordconverter.models.VoiceGender;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class DocLevelRoundRobinStrategy implements VoiceStrategy {

    private List<VoiceGender> genderList; //Ordered voice gender list for section, topic, para: e.g. Male, Male, Female

    private ConcurrentHashMap<DocLevel, Integer> toggleIncrementMap;

    public static final String[] MALE_VOICES = new String[]{
            "Brian",
            "Matthew"
    };

    public static final String[] FEMALE_VOICES = new String[]{
            "Amy",
            "Emma",
            "Salli",
            "Joanna"
    };

    public static final String[] ALL_VOICES = Stream.concat(Arrays.stream(MALE_VOICES), Arrays.stream(FEMALE_VOICES))
            .toArray(String[]::new);

    @Override
    public String next(DocLevel docLevel) {
        Integer newVal = toggleIncrementMap.merge(docLevel, 1, (a, b) -> a + b);

        VoiceGender voiceGender = genderList.get(docLevel.getHierarchy());

        String voice = MALE_VOICES[0];

        switch (voiceGender) {
            case male:
                voice = MALE_VOICES[newVal % MALE_VOICES.length];
                break;
            case female:
                voice = FEMALE_VOICES[newVal % MALE_VOICES.length];
                break;
            case any:
                voice = ALL_VOICES[newVal % MALE_VOICES.length];
                break;
        }

        return voice;
    }

    public DocLevelRoundRobinStrategy(List<VoiceGender> genderList) {
        this.genderList = genderList;
        this.toggleIncrementMap = new ConcurrentHashMap<>();
    }

}
