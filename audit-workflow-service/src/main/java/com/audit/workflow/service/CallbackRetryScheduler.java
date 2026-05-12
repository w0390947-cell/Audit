package com.audit.workflow.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CallbackRetryScheduler {

    private final CallbackService callbackService;
    private final int batchSize;

    public CallbackRetryScheduler(CallbackService callbackService,
                                  @Value("${audit.callback.retry-batch-size:20}") int batchSize) {
        this.callbackService = callbackService;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${audit.callback.retry-interval-ms:60000}")
    public void retryCallbacks() {
        callbackService.retryDueCallbacks(batchSize);
    }
}
