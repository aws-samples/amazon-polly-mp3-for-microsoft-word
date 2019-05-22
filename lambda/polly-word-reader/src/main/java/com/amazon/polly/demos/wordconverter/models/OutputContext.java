package com.amazon.polly.demos.wordconverter.models;

import com.amazonaws.services.s3.AmazonS3;

public class OutputContext {
    String dir;
    AmazonS3 s3Client;
    String bucketName;
    String prefix;
    String content;
    String fileName;
    String voiceId;

    public OutputContext(String dir, String fileName, String voiceId) {
        this.dir = dir;
        this.fileName = fileName;
        this.voiceId = voiceId;
    }

    public OutputContext(AmazonS3 s3Client, String bucketName, String prefix, String fileName, String voiceId) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.prefix = prefix;
        this.fileName = fileName;
        this.voiceId = voiceId;
    }

    public String getDir() {
        return dir;
    }

    public AmazonS3 getS3Client() {
        return s3Client;
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFileName() {
        return fileName;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getVoiceId() {
        return voiceId;
    }

    public void setVoiceId(String voiceId) {
        this.voiceId = voiceId;
    }
}
