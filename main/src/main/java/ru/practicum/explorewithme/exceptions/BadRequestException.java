package ru.practicum.explorewithme.exceptions;

/**
 * Класс-исключение, объекты которого выбрасываются в случае, если
 * была допущена ошибка при формировании запроса на клиенте.
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}