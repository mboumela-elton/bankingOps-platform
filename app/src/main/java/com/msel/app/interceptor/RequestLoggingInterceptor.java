package com.msel.app.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;

@Component
@Slf4j
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String REQUEST_START_TIME = "requestStartTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }
        
        request.setAttribute(REQUEST_ID_HEADER, requestId);
        request.setAttribute(REQUEST_START_TIME, System.currentTimeMillis());

        log.info("Incoming {} request to {} from {} with request_id: {}",
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr(),
                requestId);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                               Object handler, Exception ex) {
        String requestId = (String) request.getAttribute(REQUEST_ID_HEADER);
        Long startTime = (Long) request.getAttribute(REQUEST_START_TIME);
        
        if (startTime != null) {
            long latencyMs = System.currentTimeMillis() - startTime;
            
            log.info("Completed {} request to {} with status {} and latency_ms: {} and request_id: {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    latencyMs,
                    requestId);
        }

        if (ex != null) {
            log.error("Error processing request with request_id: {} - {}", requestId, ex.getMessage(), ex);
        }
    }
}
