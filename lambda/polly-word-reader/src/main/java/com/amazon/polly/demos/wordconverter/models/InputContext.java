package com.amazon.polly.demos.wordconverter.models;

import com.amazonaws.services.s3.AmazonS3;

public class InputContext {
    String filePath;
    AmazonS3 s3Client;
    String bucketName;
    String fileKey;

    public InputContext(AmazonS3 s3Client, String bucketName, String fileKey) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.fileKey = fileKey;
    }

    public InputContext(String filePath) {
        this.filePath = filePath;
    }

    public AmazonS3 getS3Client() {
        return s3Client;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getFileKey() {
        return fileKey;
    }
}
