package ru.practicum.explorewithme.events.dto.annotations;

import ru.practicum.explorewithme.events.dto.enums.State;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StateEnumValidator implements ConstraintValidator<ValidatedStateEnum, State> {

    private List<State> valueList;

    @Override
    public void initialize(ValidatedStateEnum constraintAnnotation) {
        valueList = new ArrayList<>();
        Arrays.stream(constraintAnnotation.acceptedValues())
                .forEach(val -> valueList.add(State.valueOf(val.toUpperCase())));
    }

    @Override
    public boolean isValid(State value, ConstraintValidatorContext context) {
        return valueList.contains(value);
    }

}