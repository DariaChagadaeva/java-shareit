package ru.practicum.shareit.booking.model;

import ru.practicum.shareit.exceptions.ValidationException;

public enum BookingState {
    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    REJECTED;

    public static BookingState parseState(String state) {
        try {
            return BookingState.valueOf(state);
        } catch (RuntimeException e) {
            throw new ValidationException("Unknown state: UNSUPPORTED_STATUS");
        }
    }
}
