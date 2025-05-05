package com.example.ililbooks.global.log.collector;

import com.example.ililbooks.global.log.dto.request.LogRequest;
import com.example.ililbooks.global.log.dto.response.LogResponse;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.*;

import java.time.Instant;
import java.util.List;

@Component
public class CloudWatchLogCollector implements LogCollector {

    private static final String LOG_GROUP_NAME = "ililbooks-log-group";
    private static final String LOG_STREAM_NAME = "application-log-stream";

    private final CloudWatchLogsClient cloudWatchLogsClient;
    private String sequenceToken;

    public CloudWatchLogCollector() {
        this.cloudWatchLogsClient = CloudWatchLogsClient.builder()
                .region(Region.AP_NORTHEAST_2)
                .build();
        initializeSequenceToken();
    }

    @Override
    public void collectRequestLog(LogRequest logRequest) {
        sendLogToCloudWatch("REQUEST", logRequest.toString());
    }

    @Override
    public void collectResponseLog(LogResponse logResponse) {
        sendLogToCloudWatch("RESPONSE", logResponse.toString());
    }

    private void sendLogToCloudWatch(String prefix, String message) {
        InputLogEvent event = InputLogEvent.builder()
                .message(formatLogMessage(prefix, message))
                .timestamp(Instant.now().toEpochMilli())
                .build();

        PutLogEventsRequest request = buildPutLogEventsRequest(event);

        try {
            PutLogEventsResponse response = cloudWatchLogsClient.putLogEvents(request);
            this.sequenceToken = response.nextSequenceToken();

        } catch (InvalidSequenceTokenException e) {
            initializeSequenceToken();
            sendLogToCloudWatch(prefix, message); // 재시도
        } catch (Exception ignored) {
        }
    }

    private PutLogEventsRequest buildPutLogEventsRequest(InputLogEvent event) {
        PutLogEventsRequest.Builder requestBuilder = PutLogEventsRequest.builder()
                .logGroupName(LOG_GROUP_NAME)
                .logStreamName(LOG_STREAM_NAME)
                .logEvents(event);

        if (sequenceToken != null) {
            requestBuilder.sequenceToken(sequenceToken);
        }

        return requestBuilder.build();
    }

    private void initializeSequenceToken() {
        try {
            DescribeLogStreamsResponse response = cloudWatchLogsClient.describeLogStreams(
                    DescribeLogStreamsRequest.builder()
                            .logGroupName(LOG_GROUP_NAME)
                            .logStreamNamePrefix(LOG_STREAM_NAME)
                            .build()
            );

            List<LogStream> streams = response.logStreams();
            if (!streams.isEmpty()) {
                this.sequenceToken = streams.get(0).uploadSequenceToken();
            } else {
                this.sequenceToken = null;
            }
        } catch (Exception ignored) {
            this.sequenceToken = null;
        }
    }

    private String formatLogMessage(String prefix, String message) {
        return String.format("[%s] %s", prefix, message);
    }
}
