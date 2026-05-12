package com.audit.workflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AuditWorkflowApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuditWorkflowApplication.class, args);
    }
}
