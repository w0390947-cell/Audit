package com.ruoyi.web.task;

import com.ruoyi.system.config.VectorProperties;
import com.ruoyi.system.service.audit.vector.AuditVectorTaskService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "vector.task", name = "enabled", havingValue = "true")
public class AuditVectorTaskRunner
{
    private final AuditVectorTaskService auditVectorTaskService;

    private final VectorProperties properties;

    public AuditVectorTaskRunner(AuditVectorTaskService auditVectorTaskService, VectorProperties properties)
    {
        this.auditVectorTaskService = auditVectorTaskService;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${vector.task.fixed-delay:10000}")
    public void run()
    {
        if (!properties.isEnabled() || !properties.getTask().isEnabled())
        {
            return;
        }
        auditVectorTaskService.runPendingTasks();
    }
}
