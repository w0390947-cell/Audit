package com.ruoyi.system.service.audit.vector.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;

import com.ruoyi.system.config.VectorProperties;
import com.ruoyi.system.domain.audit.AuditCommonResource;
import com.ruoyi.system.domain.audit.vector.AuditVectorDocument;
import com.ruoyi.system.mapper.audit.AuditLibraryMapper;
import com.ruoyi.system.service.audit.vector.AuditVectorTaskService;
import com.ruoyi.system.service.audit.vector.VectorStoreRepository;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

class AuditVectorLifecycleServiceImplTest
{
    private VectorProperties properties;

    private AuditVectorTaskService taskService;

    private VectorStoreRepository repository;

    private AuditLibraryMapper auditLibraryMapper;

    private AuditVectorLifecycleServiceImpl lifecycleService;

    @BeforeEach
    void setUp()
    {
        properties = new VectorProperties();
        properties.setEnabled(true);
        taskService = mock(AuditVectorTaskService.class);
        repository = mock(VectorStoreRepository.class);
        auditLibraryMapper = mock(AuditLibraryMapper.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<VectorStoreRepository> repositoryProvider = mock(ObjectProvider.class);
        when(repositoryProvider.getIfAvailable()).thenReturn(repository);
        lifecycleService = new AuditVectorLifecycleServiceImpl(properties, taskService, repositoryProvider, auditLibraryMapper);
    }

    @Test
    void createdResourceCreatesIndexTask()
    {
        AuditCommonResource resource = resource(1L, "/profile/upload/a.docx");
        resource.setCreateBy("admin");

        lifecycleService.onCommonResourceCreated(resource);

        verify(taskService).createIndexTask(1L, "admin");
    }

    @Test
    void changedFileUrlCreatesReindexTask()
    {
        AuditCommonResource before = resource(1L, "/profile/upload/a.docx");
        AuditCommonResource after = resource(1L, "/profile/upload/b.docx");
        after.setUpdateBy("admin");

        lifecycleService.onCommonResourceUpdated(before, after);

        verify(taskService).createReindexTask(1L, "admin");
        verify(repository, never()).updateDocumentMetadata(
                AuditVectorDocument.RESOURCE_TYPE_COMMON, 1L, after.getFileName(), after.getFileUrl(),
                after.getCurrentVersionNo());
    }

    @Test
    void unchangedFileUrlOnlySyncsMetadata()
    {
        AuditCommonResource before = resource(1L, "/profile/upload/a.docx");
        AuditCommonResource after = resource(1L, "/profile/upload/a.docx");
        after.setFileName("renamed.docx");
        after.setCurrentVersionNo("v2.0");

        lifecycleService.onCommonResourceUpdated(before, after);

        verify(taskService, never()).createReindexTask(anyLong(), any());
        verify(repository).updateDocumentMetadata(
                AuditVectorDocument.RESOURCE_TYPE_COMMON, 1L, "renamed.docx", "/profile/upload/a.docx", "v2.0");
    }

    @Test
    void movedResourceSyncsFolderId()
    {
        lifecycleService.onCommonResourceMoved(1L, 9L);

        verify(repository).updateFolderId(AuditVectorDocument.RESOURCE_TYPE_COMMON, 1L, 9L);
    }

    @Test
    void deletedResourcesCancelTasksAndDeleteIndex()
    {
        lifecycleService.onCommonResourcesDeleted(Arrays.asList(1L, 2L));

        verify(taskService).cancelPendingTasks(argThat(ids -> Arrays.equals(ids, new Long[] { 1L, 2L })), any());
        verify(repository).deleteByResourceIds(AuditVectorDocument.RESOURCE_TYPE_COMMON, Arrays.asList(1L, 2L));
    }

    private AuditCommonResource resource(Long resourceId, String fileUrl)
    {
        AuditCommonResource resource = new AuditCommonResource();
        resource.setResourceId(resourceId);
        resource.setFileUrl(fileUrl);
        resource.setFileName(fileUrl.substring(fileUrl.lastIndexOf('/') + 1));
        resource.setCurrentVersionNo("v1.0");
        return resource;
    }
}
