package com.example.readinglibrary.service;

import com.example.readinglibrary.enums.MaterialKind;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class MaterialDetailsUpdaterFactory {

    private final Map<MaterialKind, MaterialDetailsUpdater> updaters;

    public MaterialDetailsUpdaterFactory(
            List<MaterialDetailsUpdater> updaterList
    ) {
        this.updaters = new EnumMap<>(MaterialKind.class);

        for (MaterialDetailsUpdater updater : updaterList) {
            MaterialDetailsUpdater previous =
                    updaters.put(
                            updater.supportedKind(),
                            updater
                    );

            if (previous != null) {
                throw new IllegalStateException(
                        "Multiple detail updaters registered for kind: "
                                + updater.supportedKind()
                );
            }
        }
    }

    public MaterialDetailsUpdater getUpdater(
            MaterialKind kind
    ) {
        MaterialDetailsUpdater updater =
                updaters.get(kind);

        if (updater == null) {
            throw new IllegalArgumentException(
                    "No details updater configured for kind: "
                            + kind
            );
        }

        return updater;
    }
}