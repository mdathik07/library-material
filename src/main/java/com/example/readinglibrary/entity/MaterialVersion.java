package com.example.readinglibrary.entity;

import com.example.readinglibrary.enums.MaterialVersionStatus;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "material_versions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_material_version_number",
                        columnNames = {"material_id", "version_number"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_material_versions_material_id",
                        columnList = "material_id"
                )
        }
)
public class MaterialVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "material_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_material_versions_material")
    )
    private Material material;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MaterialVersionStatus status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @OneToOne(
            mappedBy = "version",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = false
    )
    private PdfDetails pdfDetails;

    @OneToOne(
            mappedBy = "version",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = false
    )
    private ArticleDetails articleDetails;

    @OneToOne(
            mappedBy = "version",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = false
    )
    private BookDetails bookDetails;

    @OneToOne(
            mappedBy = "version",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = false
    )
    private SlideDeckDetails slideDeckDetails;

    protected MaterialVersion() {
        // JPA
    }

    public MaterialVersion(
            Integer versionNumber,
            MaterialVersionStatus status
    ) {
        this.versionNumber = versionNumber;
        this.status = status;
        this.createdAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Material getMaterial() {
        return material;
    }

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public MaterialVersionStatus getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public void setStatus(MaterialVersionStatus status) {
        this.status = status;
    }

    public void setPublishedAt(OffsetDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public PdfDetails getPdfDetails() {
        return pdfDetails;
    }

    public void setPdfDetails(PdfDetails pdfDetails) {
        this.pdfDetails = pdfDetails;
    }

    public ArticleDetails getArticleDetails() {
        return articleDetails;
    }

    public void setArticleDetails(ArticleDetails articleDetails) {
        this.articleDetails = articleDetails;
    }

    public BookDetails getBookDetails() {
        return bookDetails;
    }

    public void setBookDetails(BookDetails bookDetails) {
        this.bookDetails = bookDetails;
    }

    public SlideDeckDetails getSlideDeckDetails() {
        return slideDeckDetails;
    }

    public void setSlideDeckDetails(SlideDeckDetails slideDeckDetails) {
        this.slideDeckDetails = slideDeckDetails;
    }
}