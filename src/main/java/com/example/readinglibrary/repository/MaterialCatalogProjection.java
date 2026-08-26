package com.example.readinglibrary.repository;

public interface MaterialCatalogProjection {

    Long getMaterialId();

    String getTitle();

    String getKind();

    Long getVersionId();

    Integer getVersionNumber();

    Integer getPdfPageCount();
}