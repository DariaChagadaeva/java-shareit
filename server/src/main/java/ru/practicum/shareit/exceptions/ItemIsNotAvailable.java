package ru.practicum.shareit.exceptions;

public class ItemIsNotAvailable extends RuntimeException {
    public ItemIsNotAvailable(String message) {
        super(message);
    }
}
