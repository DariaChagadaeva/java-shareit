package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequest;

import java.util.List;

public interface BookingService {
    BookingDto addBooking(Long userId, BookingRequest bookingRequest);

    BookingDto setBookingStatus(Long userId, Long bookingId, Boolean approved);

    BookingDto getBookingById(Long userId, Long bookingId);

    List<BookingDto> getAllBookerBookings(Long userId, String state, int from, int size);

    List<BookingDto> getAllBookerItemsBooking(Long ownerId, String state, int from, int size);
}
