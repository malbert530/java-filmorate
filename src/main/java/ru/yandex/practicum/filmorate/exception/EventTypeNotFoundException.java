package ru.yandex.practicum.filmorate.exception;

public class EventTypeNotFoundException extends RuntimeException {
    public EventTypeNotFoundException(String message) {
        super(message);
    }
}
