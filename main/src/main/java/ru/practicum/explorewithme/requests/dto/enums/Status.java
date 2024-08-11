package ru.practicum.explorewithme.requests.dto.enums;

import lombok.Getter;

/**
 * Новый статус запроса на участие в событии текущего пользователя
 */
@Getter
public enum Status {
    CONFIRMED,
    REJECTED,
    CANCELED,
    PENDING
}