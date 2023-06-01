package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequest;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;
    private static final String USER_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public BookingDto addBooking(@RequestHeader(USER_HEADER) Long userId,
                                 @RequestBody BookingRequest bookingRequest) {
        return bookingService.addBooking(userId, bookingRequest);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto setBookingStatus(@RequestHeader(USER_HEADER) Long userId,
                                       @PathVariable("bookingId") Long bookingId,
                                       @RequestParam Boolean approved) {
        return bookingService.setBookingStatus(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBookingById(@RequestHeader(USER_HEADER) Long userId,
                                     @PathVariable("bookingId") Long bookingId) {
        return bookingService.getBookingById(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> getAllUserBookings(@RequestHeader(USER_HEADER) Long userId,
                                               @RequestParam(defaultValue = "ALL") String state) {
        return bookingService.getAllBookerBookings(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingDto> getAllUserItemsBooking(@RequestHeader(USER_HEADER) Long ownerId,
                                                   @RequestParam(defaultValue = "ALL") String state) {
        return bookingService.getAllBookerItemsBooking(ownerId, state);
    }
}
