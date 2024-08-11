package ru.practicum.explorewithme.exceptions;

/**
 * Класс-исключение, объекты которого выбрасываются в случае, если
 * объект не был найден в базе данных.
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
