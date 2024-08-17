package ru.practicum.explorewithme.requests.dto.annotations;

import jakarta.validation.Payload;

public @interface ValidatedStatusEnum {
    String[] acceptedValues();

    String message() default "{ru.practicum.explorewithme.annotations.ValidateStatusEnum.message}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
