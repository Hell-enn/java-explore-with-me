package ru.practicum.explorewithme.events.dto.annotations;


import jakarta.validation.Payload;

public @interface ValidatedStateEnum {
    String[] acceptedValues();

    String message() default "{ru.practicum.explorewithme.events.dto.annotations.ValidatedStateEnum.message}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}