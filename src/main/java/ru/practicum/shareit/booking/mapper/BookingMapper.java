package ru.practicum.shareit.booking.mapper;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequest;
import ru.practicum.shareit.booking.model.Booking;

public class BookingMapper {
    public static Booking toBookingModel(BookingRequest bookingRequest) {
        Booking booking = new Booking();
        booking.setStart(bookingRequest.getStart());
        booking.setEnd(bookingRequest.getEnd());
        return booking;
    }

    public static BookingDto toBookingDto(Booking booking) {
        return new BookingDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getItem(),
                booking.getBooker(),
                booking.getStatus()
        );
    }
}
