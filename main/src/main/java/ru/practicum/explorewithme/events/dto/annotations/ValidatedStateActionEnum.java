package ru.practicum.explorewithme.events.dto.annotations;

import jakarta.validation.Payload;

public @interface ValidatedStateActionEnum {
    String[] acceptedValues();

    String message() default "{ru.practicum.explorewithme.events.dto.annotations.ValidatedStateActionEnum.message}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
