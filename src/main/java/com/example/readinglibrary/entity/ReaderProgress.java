package com.example.readinglibrary.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "reader_progress",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_reader_version_progress",
                        columnNames = {"reader_id", "material_version_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_reader_progress_reader_id",
                        columnList = "reader_id"
                )
        }
)
public class ReaderProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reader_id", nullable = false)
    private Long readerId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "material_version_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_reader_progress_version"
            )
    )
    private MaterialVersion materialVersion;

    @Column(nullable = false)
    private Integer position;

    @Column(nullable = false)
    private boolean completed;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected ReaderProgress() {
        // JPA
    }

    public ReaderProgress(
            Long readerId,
            MaterialVersion materialVersion,
            Integer position,
            boolean completed
    ) {
        this.readerId = readerId;
        this.materialVersion = materialVersion;
        this.position = position;
        this.completed = completed;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getReaderId() {
        return readerId;
    }

    public MaterialVersion getMaterialVersion() {
        return materialVersion;
    }

    public Integer getPosition() {
        return position;
    }

    public boolean isCompleted() {
        return completed;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void updateProgress(
            Integer position,
            boolean completed
    ) {
        this.position = position;
        this.completed = completed;
    }
}