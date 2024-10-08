package ru.practicum.explorewithme.events.dto.annotations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.practicum.explorewithme.events.dto.enums.StateAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StateActionEnumValidator implements ConstraintValidator<ValidatedStateActionEnum, StateAction> {

    private List<StateAction> valueList;

    @Override
    public void initialize(ValidatedStateActionEnum constraintAnnotation) {
        valueList = new ArrayList<>();
        Arrays.stream(constraintAnnotation.acceptedValues())
                .forEach(val -> valueList.add(StateAction.valueOf(val.toUpperCase())));
    }

    @Override
    public boolean isValid(StateAction value, ConstraintValidatorContext context) {
        return valueList.contains(value);
    }

}