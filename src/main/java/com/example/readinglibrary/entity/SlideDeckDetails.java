package com.example.readinglibrary.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "slide_deck_details")
public class SlideDeckDetails {

    @Id
    @Column(name = "version_id")
    private Long versionId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(
            name = "version_id",
            foreignKey = @ForeignKey(name = "fk_slide_deck_details_version")
    )
    private MaterialVersion version;

    @Column(name = "slide_count", nullable = false)
    private Integer slideCount;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    protected SlideDeckDetails() {
        // JPA
    }

    public SlideDeckDetails(Integer slideCount, String fileUrl) {
        this.slideCount = slideCount;
        this.fileUrl = fileUrl;
    }

    public Long getVersionId() {
        return versionId;
    }

    public MaterialVersion getVersion() {
        return version;
    }

    public Integer getSlideCount() {
        return slideCount;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setVersion(MaterialVersion version) {
        this.version = version;
    }

    public void setSlideCount(Integer slideCount) {
        this.slideCount = slideCount;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
}