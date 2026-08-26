package com.example.readinglibrary.service;

import com.example.readinglibrary.enums.MaterialKind;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class MaterialDetailsCreatorFactory {

    private final Map<MaterialKind, MaterialDetailsCreator> creators;

    public MaterialDetailsCreatorFactory(
            List<MaterialDetailsCreator> creatorList
    ) {
        this.creators = new EnumMap<>(MaterialKind.class);

        for (MaterialDetailsCreator creator : creatorList) {
            MaterialDetailsCreator previous =
                    creators.put(
                            creator.supportedKind(),
                            creator
                    );

            if (previous != null) {
                throw new IllegalStateException(
                        "Multiple detail creators registered for kind: "
                                + creator.supportedKind()
                );
            }
        }
    }

    public MaterialDetailsCreator getCreator(
            MaterialKind kind
    ) {
        MaterialDetailsCreator creator = creators.get(kind);

        if (creator == null) {
            throw new IllegalArgumentException(
                    "No details creator configured for material kind: "
                            + kind
            );
        }

        return creator;
    }
}