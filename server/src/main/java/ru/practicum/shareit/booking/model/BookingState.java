package ru.practicum.shareit.booking.model;

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
            throw new IllegalArgumentException("Unknown state: " + state);
        }
    }
}
