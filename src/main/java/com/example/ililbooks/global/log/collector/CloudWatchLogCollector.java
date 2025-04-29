package com.example.ililbooks.global.log.collector;

import com.example.ililbooks.global.log.dto.request.LogRequest;
import com.example.ililbooks.global.log.dto.response.LogResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.*;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
public class CloudWatchLogCollector implements LogCollector {

    private static final String LOG_GROUP_NAME = "ililbooks-log-group";
    private static final String LOG_STREAM_NAME = "application-log-stream";

    private final CloudWatchLogsClient cloudWatchLogsClient;
    private String sequenceToken;

    public CloudWatchLogCollector() {
        this.cloudWatchLogsClient = CloudWatchLogsClient.builder()
                .region(Region.AP_NORTHEAST_2) // 서울 리전
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

    /*
     * CloudWatch 로 로그를 전송
     */
    private void sendLogToCloudWatch(String prefix, String message) {
        InputLogEvent event = InputLogEvent.builder()
                .message(formatLogMessage(prefix, message))
                .timestamp(Instant.now().toEpochMilli())
                .build();

        PutLogEventsRequest request = buildPutLogEventsRequest(event);

        try {
            PutLogEventsResponse response = cloudWatchLogsClient.putLogEvents(request);

            if (response.rejectedLogEventsInfo() != null) {
                log.error("[CloudWatch] 로그 전송 실패: {}", response.rejectedLogEventsInfo());
            } else {
                log.info("[CloudWatch] 로그 전송 성공 (nextSequenceToken: {})", response.nextSequenceToken());
                this.sequenceToken = response.nextSequenceToken();
            }

        } catch (InvalidSequenceTokenException e) {
            handleInvalidSequenceToken(prefix, message, e);
        } catch (Exception e) {
            log.error("[CloudWatch] 로그 전송 중 예외 발생", e);
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

    /*
     * InvalidSequenceToken 예외 처리: sequenceToken 초기화 후 재전송
     */
    private void handleInvalidSequenceToken(String prefix, String message, InvalidSequenceTokenException e) {
        log.warn("[CloudWatch] InvalidSequenceTokenException 발생 - sequenceToken 초기화 후 재시도합니다. 오류 메시지: {}", e.getMessage());
        initializeSequenceToken();
        sendLogToCloudWatch(prefix, message);
    }

    /*
     * CloudWatch LogStream 의 현재 SequenceToken 을 초기화
     */
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
                log.info("[CloudWatch] 초기 SequenceToken 설정 완료: {}", sequenceToken);
            } else {
                log.warn("[CloudWatch] 해당 로그 스트림이 존재하지 않습니다. (스트림 이름: {})", LOG_STREAM_NAME);
                this.sequenceToken = null;
            }
        } catch (Exception e) {
            log.error("[CloudWatch] SequenceToken 초기화 실패", e);
            this.sequenceToken = null;
        }
    }

    /*
     * 로그 메시지 형식화
     */
    private String formatLogMessage(String prefix, String message) {
        return String.format("[%s] %s", prefix, message);
    }
}
