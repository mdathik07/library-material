package com.example.readinglibrary.validation;

import com.example.readinglibrary.enums.MaterialKind;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class MaterialDetailsValidatorFactory {

    private final Map<MaterialKind, MaterialDetailsValidator> validators;

    public MaterialDetailsValidatorFactory(
            List<MaterialDetailsValidator> validatorList
    ) {
        this.validators = new EnumMap<>(MaterialKind.class);

        for (MaterialDetailsValidator validator : validatorList) {
            MaterialDetailsValidator previous =
                    validators.put(
                            validator.supportedKind(),
                            validator
                    );

            if (previous != null) {
                throw new IllegalStateException(
                        "Multiple validators registered for material kind: "
                                + validator.supportedKind()
                );
            }
        }
    }

    public MaterialDetailsValidator getValidator(MaterialKind kind) {
        MaterialDetailsValidator validator = validators.get(kind);

        if (validator == null) {
            throw new IllegalArgumentException(
                    "No validator configured for material kind: " + kind
            );
        }

        return validator;
    }
}