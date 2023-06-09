package ru.practicum.shareit.booking.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequest;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.EntityNotFoundException;
import ru.practicum.shareit.exceptions.ItemIsNotAvailable;
import ru.practicum.shareit.exceptions.OwnerException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingServiceImpl implements BookingService {
    final BookingRepository bookingRepository;
    final UserRepository userRepository;
    final ItemRepository itemRepository;

    @Transactional
    @Override
    public BookingDto addBooking(Long userId, BookingRequest bookingRequest) {
        checkUser(userId);
        checkItemIsAvailable(bookingRequest.getItemId());
        checkIfBookerIsNotOwner(userId, bookingRequest.getItemId());
        if (checkDates(bookingRequest.getStart(), bookingRequest.getEnd())) {
            throw new ValidationException("Wrong booking time");
        }
        Booking booking = BookingMapper.toBookingModel(bookingRequest);
        booking.setItem(itemRepository.findById(bookingRequest.getItemId())
                .orElseThrow(() -> new EntityNotFoundException("No item with id " + bookingRequest.getItemId())));
        booking.setBooker(userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("No user with id " + userId)));
        booking.setStatus(BookingStatus.WAITING);
        bookingRepository.save(booking);
        log.info("New booking added : {}", booking);
        return BookingMapper.toBookingDto(booking);
    }

    @Transactional
    @Override
    public BookingDto setBookingStatus(Long userId, Long bookingId, Boolean approved) {
        Booking booking = getBookingIfExists(bookingId);
        checkOwner(userId, booking);
        if (BookingStatus.APPROVED == booking.getStatus()) {
            throw new ValidationException("Booking is already confirmed");
        }
        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
            log.info("Set status to Approved for booking : {}", booking);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
            log.info("Set status to Rejected for booking : {}", booking);
        }
        bookingRepository.save(booking);
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public BookingDto getBookingById(Long userId, Long bookingId) {
        checkUser(userId);
        Booking booking = getBookingIfExists(bookingId);
        checkBookerOrItemOwner(booking, userId);
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getAllBookerBookings(Long userId, String state, int from, int size) {
        checkUser(userId);
        BookingState newState = BookingState.parseState(state);
        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);
        switch (newState) {
            case ALL:
                return bookingRepository.findAllByBookerIdOrderByStartDesc(userId, page).getContent()
                        .stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
            case PAST:
                return bookingRepository.findAllByBookerIdAndEndIsBeforeOrderByStartDesc(userId, LocalDateTime.now(), page)
                        .getContent().stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
            case FUTURE:
                return bookingRepository.findAllByBookerIdAndStartIsAfterOrderByStartDesc(userId, LocalDateTime.now(), page)
                        .getContent().stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
            case CURRENT:
                return bookingRepository.findAllByBookerIdCurrentBookings(userId, LocalDateTime.now(), page)
                        .getContent().stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
            default:
                BookingStatus status = BookingStatus.valueOf(String.valueOf(state));
                return bookingRepository.findALLByBookerIdAndStatusOrderByStartDesc(userId, status, page)
                        .getContent().stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
        }
    }

    @Override
    public List<BookingDto> getAllBookerItemsBooking(Long ownerId, String state, int from, int size) {
        checkUser(ownerId);
        BookingState newState = BookingState.parseState(state);
        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);
        switch (newState) {
            case ALL:
                return bookingRepository.findAllByOwnerIdOrderByStartDesc(ownerId, page).stream()
                        .map(BookingMapper::toBookingDto).collect(Collectors.toList());
            case PAST:
                return bookingRepository.findAllByOwnerIdAndEndIsBeforeOrderByStartDesc(ownerId, LocalDateTime.now(), page)
                        .getContent().stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
            case FUTURE:
                return bookingRepository.findAllByOwnerIdAndStartAfterOrderByStartDesc(ownerId, LocalDateTime.now(), page)
                        .getContent().stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
            case CURRENT:
                return bookingRepository.findAllByOwnerIdCurrentBookings(ownerId, LocalDateTime.now(), page)
                        .getContent().stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
            default:
                BookingStatus status = BookingStatus.valueOf(String.valueOf(state));
                return bookingRepository.findAllByOwnerIdAndStatusOrderByStartDesc(ownerId, status, page)
                        .getContent().stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
        }
    }

    private boolean checkUser(Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new EntityNotFoundException("No user with id " + userId);
        } else {
            return true;
        }
    }

    private Boolean checkOwner(Long userId, Booking booking) {
        Long ownerId = booking.getItem().getOwnerId();
        if (ownerId.equals(userId)) {
            return true;
        } else {
            throw new OwnerException("User is not the owner of the item");
        }
    }

    private boolean checkItemIsAvailable(Long itemId) {
        if (itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("No item with id " + itemId))
                .getAvailable()) {
            return true;
        } else {
            throw new ItemIsNotAvailable("Item is not available for booking");
        }
    }

    private boolean checkDates(LocalDateTime start, LocalDateTime end) {
        LocalDateTime now = LocalDateTime.now();
        return (start.isBefore(now) || end.isBefore(now) || start.equals(end) || start.isAfter(end));
    }

    private boolean checkIfBookerIsNotOwner(Long userId, Long itemId) {
        if (userId.equals(itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("No item with id " + itemId)).getOwnerId())) {
            throw new OwnerException("User is the owner of the item");
        } else {
            return true;
        }
    }

    private Booking getBookingIfExists(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("No booking with id " + bookingId));
    }

    private Boolean checkBookerOrItemOwner(Booking booking, Long userId) {
        Long ownerId = booking.getItem().getOwnerId();
        Long bookerId = booking.getBooker().getId();
        if (ownerId.equals(userId) || bookerId.equals(userId)) {
            return true;
        } else {
            throw new EntityNotFoundException("Booking is not available for viewing");
        }
    }
}
