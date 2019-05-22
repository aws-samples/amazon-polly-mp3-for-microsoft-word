package com.amazon.polly.demos.wordconverter.io;

import com.amazon.polly.demos.wordconverter.models.InputContext;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import java.io.IOException;
import java.io.InputStream;

public class S3InputStrategy implements InputStrategy {

    @Override
    public InputStream fetchFile(InputContext inputContext) throws IOException {
        GetObjectRequest getObjectRequest = new GetObjectRequest(inputContext.getBucketName(), inputContext.getFileKey());
        S3Object s3Object = inputContext.getS3Client().getObject(getObjectRequest);
        return s3Object.getObjectContent();
    }
}
