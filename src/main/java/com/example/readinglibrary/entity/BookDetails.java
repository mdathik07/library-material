package com.example.readinglibrary.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.MapsId;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "book_details")
public class BookDetails {

    @Id
    @Column(name = "version_id")
    private Long versionId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(
            name = "version_id",
            foreignKey = @ForeignKey(name = "fk_book_details_version")
    )
    private MaterialVersion version;

    @Column(nullable = false, length = 255)
    private String author;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private JsonNode chapters;

    protected BookDetails() {
        // Required by JPA
    }

    public BookDetails(String author, JsonNode chapters) {
        this.author = author;
        this.chapters = chapters;
    }

    public Long getVersionId() {
        return versionId;
    }

    public MaterialVersion getVersion() {
        return version;
    }

    public String getAuthor() {
        return author;
    }

    public JsonNode getChapters() {
        return chapters;
    }

    public void setVersion(MaterialVersion version) {
        this.version = version;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setChapters(JsonNode chapters) {
        this.chapters = chapters;
    }
}