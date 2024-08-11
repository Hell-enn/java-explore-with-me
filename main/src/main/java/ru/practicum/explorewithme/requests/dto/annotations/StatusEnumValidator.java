package ru.practicum.explorewithme.requests.dto.annotations;

import ru.practicum.explorewithme.requests.dto.enums.Status;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StatusEnumValidator implements ConstraintValidator<ValidatedStatusEnum, Status> {

    private List<Status> valueList;

    @Override
    public void initialize(ValidatedStatusEnum constraintAnnotation) {
        valueList = new ArrayList<>();
        Arrays.stream(constraintAnnotation.acceptedValues())
                .forEach(val -> valueList.add(Status.valueOf(val.toUpperCase())));
    }

    @Override
    public boolean isValid(Status value, ConstraintValidatorContext context) {
        return valueList.contains(value);
    }

}