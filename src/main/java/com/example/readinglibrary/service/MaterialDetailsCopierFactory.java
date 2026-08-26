package com.example.readinglibrary.service;

import com.example.readinglibrary.enums.MaterialKind;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class MaterialDetailsCopierFactory {

    private final Map<MaterialKind, MaterialDetailsCopier> copiers;

    public MaterialDetailsCopierFactory(
            List<MaterialDetailsCopier> copierList
    ) {
        this.copiers = new EnumMap<>(MaterialKind.class);

        for (MaterialDetailsCopier copier : copierList) {
            MaterialDetailsCopier previous =
                    copiers.put(
                            copier.supportedKind(),
                            copier
                    );

            if (previous != null) {
                throw new IllegalStateException(
                        "Multiple detail copiers registered for kind: "
                                + copier.supportedKind()
                );
            }
        }
    }

    public MaterialDetailsCopier getCopier(
            MaterialKind kind
    ) {
        MaterialDetailsCopier copier = copiers.get(kind);

        if (copier == null) {
            throw new IllegalArgumentException(
                    "No details copier configured for kind: "
                            + kind
            );
        }

        return copier;
    }
}