package com.example.readinglibrary.service;

import com.example.readinglibrary.entity.MaterialVersion;
import com.example.readinglibrary.enums.MaterialKind;

public interface MaterialDetailsCopier {

    MaterialKind supportedKind();

    void copyDetails(
            MaterialVersion sourceVersion,
            MaterialVersion targetVersion
    );
}