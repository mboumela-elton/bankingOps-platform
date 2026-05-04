package com.msel.app.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class StructuredLogger {

    public void logEvent(String event, String requestId, Map<String, Object> context) {
        Map<String, Object> logEntry = new HashMap<>();
        logEntry.put("event", event);
        logEntry.put("request_id", requestId);
        logEntry.putAll(context);
        log.info("{}", logEntry);
    }

    public void logError(String event, String requestId, String message, Exception ex) {
        Map<String, Object> logEntry = new HashMap<>();
        logEntry.put("event", event);
        logEntry.put("request_id", requestId);
        logEntry.put("message", message);
        log.error("{}", logEntry, ex);
    }
}
