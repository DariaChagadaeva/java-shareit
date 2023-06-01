package ru.practicum.shareit.booking;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequest;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exceptions.EntityNotFoundException;
import ru.practicum.shareit.exceptions.ItemIsNotAvailable;
import ru.practicum.shareit.exceptions.OwnerException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@FieldDefaults(level = AccessLevel.PRIVATE)
class BookingServiceImplIT {
    @Autowired
    BookingService bookingService;

    @Autowired
    ItemRequestService itemRequestService;

    @Autowired
    ItemService itemService;

    @Autowired
    UserService userService;

    UserDto userDto;
    UserDto owner;
    ItemDto itemDto;
    ItemRequestDto itemRequestDto;
    BookingDto bookingDto;
    BookingRequest bookingRequest;
    LocalDateTime start = LocalDateTime.now().plusHours(1);
    LocalDateTime end = start.plusHours(10);

    @BeforeEach
    void start() {
        userDto = userService.addUser(UserDto.builder()
                .name("user").email("user@user.com").build());
        owner = userService.addUser(UserDto.builder()
                .name("owner").email("owner@user.com").build());
        itemRequestDto = itemRequestService.addItemRequest(userDto.getId(),
                new ItemRequestDto("item request"));
        itemDto = itemService.addItem(owner.getId(), ItemDto.builder()
                .name("item").description("desc").available(true).requestId(itemRequestDto.getId()).build());
        bookingRequest = new BookingRequest(itemDto.getId(), start, end);
        bookingDto = bookingService.addBooking(userDto.getId(), bookingRequest);
    }

    @Test
    void addBooking() {
        assertThat(bookingDto, equalTo(bookingService.getBookingById(userDto.getId(), bookingDto.getId())));
    }

    @Test
    void addBooking_whenDateIsWrong_thenReturnValidationException() {
        bookingRequest.setStart(LocalDateTime.now());
        bookingRequest.setEnd(LocalDateTime.now());

        assertThrows(ValidationException.class,
                () -> bookingService.addBooking(userDto.getId(), bookingRequest));
    }

    @Test
    void addBooking_whenUserIsOwner_thenReturnOwnerException() {
        assertThrows(OwnerException.class,
                () -> bookingService.addBooking(owner.getId(), bookingRequest));
    }

    @Test
    void addBooking_whenUserIsNotFound_thenReturnEntityNotFoundException() {
        assertThrows(EntityNotFoundException.class,
                () -> bookingService.addBooking(0L, bookingRequest));
    }

    @Test
    void addBooking_whenItemIsNotAvailable_ItemIsNotAvailableException() {
        itemDto.setAvailable(false);
        itemService.updateItem(owner.getId(), itemDto.getId(), itemDto);

        assertThrows(ItemIsNotAvailable.class,
                () -> bookingService.addBooking(userDto.getId(), bookingRequest));
    }

    @Test
    void setBookingStatus_whenOwnerApprovedBooking_thenReturnBookingWithStatusApproved() {
        bookingService.setBookingStatus(owner.getId(), bookingDto.getId(), true);

        assertThat(BookingStatus.APPROVED, equalTo(bookingService.getBookingById(owner.getId(), itemDto.getId()).getStatus()));
    }

    @Test
    void setBookingStatus_whenOwnerRejectedBooking_thenReturnBookingWithStatusRejected() {
        bookingService.setBookingStatus(owner.getId(), bookingDto.getId(), false);

        assertThat(BookingStatus.REJECTED, equalTo(bookingService.getBookingById(owner.getId(), itemDto.getId()).getStatus()));
    }

    @Test
    void setBookingStatus_whenStatusIsAlreadyApproved_thenReturnValidationException() {
        bookingService.setBookingStatus(owner.getId(), bookingDto.getId(), true);

        assertThrows(ValidationException.class,
                () -> bookingService.setBookingStatus(owner.getId(), bookingDto.getId(), true));
    }

    @Test
    void setBookingStatus_whenUserIsNotTheOwner_thenReturnOwnerException() {
        assertThrows(OwnerException.class,
                () -> bookingService.setBookingStatus(userDto.getId(), bookingDto.getId(), true));
    }

    @Test
    void getBookingById() {
        assertThat(bookingService.getBookingById(owner.getId(), bookingDto.getId()), equalTo(bookingDto));
    }

    @Test
    void getBookingById_whenBookingNotFound_thenReturnEntityNotFoundException() {
        assertThrows(EntityNotFoundException.class,
                () -> bookingService.getBookingById(userDto.getId(), 5L));
    }

    @Test
    void getBookingById_whenUserIsNeitherTheOwnerNorTheBooker_thenReturnEntityNotFoundException() {
        UserDto newUser = userService.addUser(UserDto.builder()
                .name("new user").email("newuser@user.com").build());

        assertThrows(EntityNotFoundException.class,
                () -> bookingService.getBookingById(newUser.getId(), bookingDto.getId()));
    }
}