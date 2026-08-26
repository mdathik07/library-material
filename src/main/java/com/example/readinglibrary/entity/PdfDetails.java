package com.example.readinglibrary.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "pdf_details",
        indexes = {
                @Index(
                        name = "idx_pdf_details_page_count",
                        columnList = "page_count"
                )
        }
)
public class PdfDetails {

    @Id
    @Column(name = "version_id")
    private Long versionId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(
            name = "version_id",
            foreignKey = @ForeignKey(name = "fk_pdf_details_version")
    )
    private MaterialVersion version;

    @Column(name = "page_count", nullable = false)
    private Integer pageCount;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    protected PdfDetails() {
        // JPA
    }

    public PdfDetails(Integer pageCount, String fileUrl) {
        this.pageCount = pageCount;
        this.fileUrl = fileUrl;
    }

    public Long getVersionId() {
        return versionId;
    }

    public MaterialVersion getVersion() {
        return version;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setVersion(MaterialVersion version) {
        this.version = version;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
}