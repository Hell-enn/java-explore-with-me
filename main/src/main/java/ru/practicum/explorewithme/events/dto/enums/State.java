package ru.practicum.explorewithme.events.dto.enums;

import lombok.Getter;

/**
 * Список состояний жизненного цикла события
 */
@Getter
public enum State {
    PENDING,
    PUBLISHED,
    CANCELED
}