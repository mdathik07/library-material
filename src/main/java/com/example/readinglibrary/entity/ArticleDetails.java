package com.example.readinglibrary.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "article_details")
public class ArticleDetails {

    @Id
    @Column(name = "version_id")
    private Long versionId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(
            name = "version_id",
            foreignKey = @ForeignKey(name = "fk_article_details_version")
    )
    private MaterialVersion version;

    @Column(name = "word_count", nullable = false)
    private Integer wordCount;

    protected ArticleDetails() {
        // JPA
    }

    public ArticleDetails(Integer wordCount) {
        this.wordCount = wordCount;
    }

    public Long getVersionId() {
        return versionId;
    }

    public MaterialVersion getVersion() {
        return version;
    }

    public Integer getWordCount() {
        return wordCount;
    }

    public void setVersion(MaterialVersion version) {
        this.version = version;
    }

    public void setWordCount(Integer wordCount) {
        this.wordCount = wordCount;
    }
}