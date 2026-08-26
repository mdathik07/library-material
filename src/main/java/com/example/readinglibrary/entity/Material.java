package com.example.readinglibrary.entity;

import com.example.readinglibrary.enums.MaterialKind;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "materials",
        indexes = {
                @Index(name = "idx_materials_kind", columnList = "kind")
        }
)
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MaterialKind kind;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @OneToMany(
            mappedBy = "material",
            cascade = CascadeType.ALL,
            orphanRemoval = false
    )
    @OrderBy("versionNumber ASC")
    private List<MaterialVersion> versions = new ArrayList<>();

    protected Material() {
        // JPA
    }

    public Material(String title, MaterialKind kind) {
        this.title = title;
        this.kind = kind;
        this.createdAt = OffsetDateTime.now();
    }

    public void addVersion(MaterialVersion version) {
        versions.add(version);
        version.setMaterial(this);
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public MaterialKind getKind() {
        return kind;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public List<MaterialVersion> getVersions() {
        return versions;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setKind(MaterialKind kind) {
        this.kind = kind;
    }
}