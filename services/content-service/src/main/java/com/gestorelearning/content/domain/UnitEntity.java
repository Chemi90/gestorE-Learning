package com.gestorelearning.content.domain;

import com.gestorelearning.common.domain.GenerationStatus;
import com.gestorelearning.common.domain.ResourceType;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "units", schema = "content")
public class UnitEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private ModuleEntity module;

    @Column(nullable = false)
    private String title;

    @Column(name = "content_placeholder", columnDefinition = "TEXT")
    private String contentPlaceholder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "resource_type", columnDefinition = "content.resource_type")
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private ResourceType resourceType;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "content.generation_status")
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private GenerationStatus status = GenerationStatus.PENDING;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private boolean active = true;

    @PrePersist
    void onCreate() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public ModuleEntity getModule() { return module; }
    public void setModule(ModuleEntity module) { this.module = module; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContentPlaceholder() { return contentPlaceholder; }
    public void setContentPlaceholder(String contentPlaceholder) { this.contentPlaceholder = contentPlaceholder; }

    public ResourceType getResourceType() { return resourceType; }
    public void setResourceType(ResourceType resourceType) { this.resourceType = resourceType; }

    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }

    public GenerationStatus getStatus() { return status; }
    public void setStatus(GenerationStatus status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
