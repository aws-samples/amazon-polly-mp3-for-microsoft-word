package com.amazon.polly.demos.wordconverter;

import com.amazon.polly.demos.wordconverter.io.S3InputStrategy;
import com.amazon.polly.demos.wordconverter.io.S3OutputStrategy;
import com.amazon.polly.demos.wordconverter.models.InputContext;
import com.amazon.polly.demos.wordconverter.models.OutputContext;
import com.amazon.polly.demos.wordconverter.models.SectionRecord;
import com.amazon.polly.demos.wordconverter.models.VoiceGender;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.event.S3EventNotification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FAQDocToSSML extends AbstractSSMLProcessor implements RequestHandler<S3Event, String> {

    private static Logger logger = LogManager.getLogger(FAQDocToSSML.class);
    private String[] headers;

    private void buildHeadersFromS3Config(AmazonS3 s3Client, String s3Bucket, String configKey) {
        InputContext configCtx = new InputContext(s3Client, s3Bucket, configKey);
        try (InputStream is = new S3InputStrategy().fetchFile(configCtx)) {
            String configText = convertStreamToString(is);
            this.headers = configText.split("\n");
        } catch (IOException e) {
            logger.error("Couldn't build headers", e);
            throw new RuntimeException("Couldn't build headers", e);
        }
    }

    private void buildHeaders(AmazonS3 s3Client, String s3Bucket, String configKey) {
        InputContext configCtx = new InputContext(s3Client, s3Bucket, configKey);
        try (InputStream is = new S3InputStrategy().fetchFile(configCtx)) {
            List<String> headings = parseHeadings(is);
            this.headers = headings.toArray(new String[0]);
        } catch (IOException e) {
            logger.error("Couldn't build headers", e);
            throw new RuntimeException("Couldn't build headers", e);
        }
    }

    @Override
    public List<String> sectionHeaders() {
        return Arrays.asList(headers);
    }

    @Override
    public boolean itemStartSignal(String text) {
        return isQuestion(text);
    }

    private String processDoc(AmazonS3 s3Client, String s3Bucket, String srcKey) {
        String result;
        String configKey = srcKey.replace("src/", "config/").replace(".docx", ".txt");
        buildHeaders(s3Client, s3Bucket, srcKey);
        InputContext inputContext = new InputContext(s3Client, s3Bucket, srcKey);
        try {
            Map<String, SectionRecord> sectionMap = parseSections(new S3InputStrategy(), inputContext);
            String outputPrefix = srcKey.substring(0, srcKey.lastIndexOf("/") + 1).replace("src", "ssml");

            OutputContext outputContext = new OutputContext(s3Client, s3Bucket, outputPrefix, "", "");
            VoiceGender[] voiceGenders = new VoiceGender[]{VoiceGender.male, VoiceGender.male, VoiceGender.female};
            saveSSML(sectionMap, new S3OutputStrategy(), outputContext, new DocLevelRoundRobinStrategy(Arrays.asList(voiceGenders)));

            result = "Success";
        } catch (IOException e) {
            logger.error("Exception occurred", e);
            result = e.getMessage();
        }
        return result;
    }

    @Override
    public String handleRequest(S3Event s3Event, Context context) {
        logger.info("Received event: " + s3Event);

        S3EventNotification.S3EventNotificationRecord record = s3Event.getRecords().get(0);
        logger.info(String.format("Event records count: %d, first record: %s",s3Event.getRecords().size(), record));

        AmazonS3 s3Client = AmazonS3Client.builder().build();
        String s3Bucket = record.getS3().getBucket().getName();
        String srcKey = record.getS3().getObject().getKey();

        logger.info(String.format("Processing bucket: %s, file: %s", s3Bucket, srcKey));
        return processDoc(s3Client, s3Bucket, srcKey);
    }

    private boolean isQuestion(String text) {
        return text.trim().contains("?");
    }

    public static void main(String[] args) {
        FAQDocToSSML app = new FAQDocToSSML();

        AmazonS3 s3Client = AmazonS3Client.builder().build();
        String s3Bucket = "YOUR_BUCKET";
        String srcKey = "polly-faq-reader/src/polly-faq.docx";

        app.processDoc(s3Client, s3Bucket, srcKey);

    }

}
