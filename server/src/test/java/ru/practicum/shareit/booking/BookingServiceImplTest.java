package ru.practicum.shareit.booking;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequest;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exceptions.EntityNotFoundException;
import ru.practicum.shareit.exceptions.ItemIsNotAvailable;
import ru.practicum.shareit.exceptions.OwnerException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingServiceImplTest {
    @Mock
    BookingRepository bookingRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    ItemRepository itemRepository;

    @InjectMocks
    BookingServiceImpl bookingService;
    User user;
    User owner;
    Item item;
    Booking bookingCurrent;
    Booking bookingFuture;
    Booking bookingPast;
    Page<Booking> page;

    @BeforeEach
    void start() {
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime pastTime = currentTime.minusDays(10);
        LocalDateTime futureTime = currentTime.plusDays(10);
        user = setUser(1L, "user", "user@user.com");
        owner = setUser(2L, "owner", "owner@user.com");
        lenient().when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        lenient().when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        item = new Item(1L, "item", "desc", true, 2L, 1L);
        bookingCurrent = new Booking(1L, currentTime, currentTime.plusHours(1), item, user, BookingStatus.APPROVED);
        bookingFuture = new Booking(2L, futureTime, futureTime.plusHours(1), item, user, BookingStatus.APPROVED);
        bookingPast = new Booking(3L, pastTime, pastTime.plusHours(1), item, user, BookingStatus.APPROVED);
    }

    @Test
    void getAllBookerBookings_WhenStateIsAll_thenReturnListOfAllBookerBookings() {
        page = new PageImpl<>(List.of(bookingCurrent, bookingFuture, bookingPast));
        when(bookingRepository.findAllByBookerIdOrderByStartDesc(anyLong(), any())).thenReturn(page);

        List<BookingDto> bookings = bookingService.getAllBookerBookings(1L, "ALL", 0, 10);

        assertEquals(3, bookings.size());
    }

    @Test
    void getAllBookerBookings_WhenStateIsPast_thenReturnListOfBookerPastBookings() {
        page = new PageImpl<>(List.of(bookingPast));
        when(bookingRepository.findAllByBookerIdAndEndIsBeforeOrderByStartDesc(anyLong(), any(), any())).thenReturn(page);

        List<BookingDto> bookings = bookingService.getAllBookerBookings(1L, "PAST", 0, 10);

        assertEquals(1, bookings.size());
        assertEquals(3, bookings.get(0).getId());
    }

    @Test
    void getAllBookerBookings_WhenStateIsFuture_thenReturnListOfBookerFutureBookings() {
        page = new PageImpl<>(List.of(bookingFuture));
        when(bookingRepository.findAllByBookerIdAndStartIsAfterOrderByStartDesc(anyLong(), any(), any())).thenReturn(page);

        List<BookingDto> bookings = bookingService.getAllBookerBookings(1L, "FUTURE", 0, 10);

        assertEquals(1, bookings.size());
        assertEquals(2, bookings.get(0).getId());
    }

    @Test
    void getAllBookerBookings_WhenStateIsCurrent_thenReturnListOfBookerCurrentBookings() {
        page = new PageImpl<>(List.of(bookingCurrent));
        when(bookingRepository.findAllByBookerIdCurrentBookings(anyLong(), any(), any())).thenReturn(page);

        List<BookingDto> bookings = bookingService.getAllBookerBookings(1L, "CURRENT", 0, 10);

        assertEquals(1, bookings.size());
        assertEquals(1, bookings.get(0).getId());
    }

    @Test
    void getAllBookerBookings_WhenStateIsWaiting_thenReturnListOfBookerWaitingBookings() {
        page = new PageImpl<>(List.of());
        when(bookingRepository.findALLByBookerIdAndStatusOrderByStartDesc(anyLong(), any(), any())).thenReturn(page);

        List<BookingDto> bookings = bookingService.getAllBookerBookings(1L, "WAITING", 0, 10);

        assertEquals(0, bookings.size());
    }

    @Test
    void getAllBookerBookings_WhenStateIsWrong_thenReturnIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                bookingService.getAllBookerBookings(1L, "WRONG_STATE", 0, 10));

    }

    @Test
    void getAllBookerItemsBooking_WhenStateIsAll_thenReturnListOfAllUserItemBookings() {
        page = new PageImpl<>(List.of(bookingCurrent, bookingFuture, bookingPast));
        when(bookingRepository.findAllByOwnerIdOrderByStartDesc(anyLong(), any())).thenReturn(page);

        List<BookingDto> bookings = bookingService.getAllBookerItemsBooking(2L, "ALL", 0, 10);

        assertEquals(3, bookings.size());
    }

    @Test
    void getAllBookerItemsBooking_WhenStateIsPast_thenReturnListOfUserItemPastBookings() {
        page = new PageImpl<>(List.of(bookingPast));
        when(bookingRepository.findAllByOwnerIdAndEndIsBeforeOrderByStartDesc(anyLong(), any(), any())).thenReturn(page);

        List<BookingDto> bookings = bookingService.getAllBookerItemsBooking(2L, "PAST", 0, 10);

        assertEquals(1, bookings.size());
        assertEquals(3, bookings.get(0).getId());
    }

    @Test
    void getAllBookerItemsBooking_WhenStateIsFuture_thenReturnListOfUserItemFutureBookings() {
        page = new PageImpl<>(List.of(bookingFuture));
        when(bookingRepository.findAllByOwnerIdAndStartAfterOrderByStartDesc(anyLong(), any(), any())).thenReturn(page);

        List<BookingDto> bookings = bookingService.getAllBookerItemsBooking(2L, "FUTURE", 0, 10);

        assertEquals(1, bookings.size());
        assertEquals(2, bookings.get(0).getId());
    }

    @Test
    void getAllBookerItemsBooking_WhenStateIsCurrent_thenReturnListOfUserItemCurrentBookings() {
        page = new PageImpl<>(List.of(bookingCurrent));
        when(bookingRepository.findAllByOwnerIdCurrentBookings(anyLong(), any(), any())).thenReturn(page);

        List<BookingDto> bookings = bookingService.getAllBookerItemsBooking(2L, "CURRENT", 0, 10);

        assertEquals(1, bookings.size());
        assertEquals(1, bookings.get(0).getId());
    }

    @Test
    void getAllBookerItemsBooking_WhenStateIsWaiting_thenReturnListOfUserItemWaitingBookings() {
        page = new PageImpl<>(List.of());
        when(bookingRepository.findAllByOwnerIdAndStatusOrderByStartDesc(anyLong(), any(), any())).thenReturn(page);

        List<BookingDto> bookings = bookingService.getAllBookerItemsBooking(2L, "WAITING", 0, 10);

        assertEquals(0, bookings.size());
    }

    @Test
    void getAllBookerItemsBooking_WhenStateIsWrong_thenReturnIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                bookingService.getAllBookerItemsBooking(2L, "WRONG_STATE", 0, 10));

    }

    @Test
    void addBooking_whenUserIsNotFound_thenReturnEntityNotFoundException() {
        LocalDateTime newStart = LocalDateTime.now();
        LocalDateTime newEnd = newStart.minusDays(1);
        BookingRequest newBookingRequest = new BookingRequest(item.getId(), newStart, newEnd);

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> bookingService.addBooking(0L, newBookingRequest));
        assertEquals("No user with id " + 0L, ex.getMessage());
    }

    @Test
    void addBooking_whenItemIsNotAvailable_ItemIsNotAvailableException() {
        LocalDateTime newStart = LocalDateTime.now();
        LocalDateTime newEnd = newStart.minusDays(1);
        BookingRequest newBookingRequest = new BookingRequest(item.getId(), newStart, newEnd);
        item.setAvailable(false);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        ItemIsNotAvailable ex = assertThrows(ItemIsNotAvailable.class,
                () -> bookingService.addBooking(user.getId(), newBookingRequest));
        assertEquals("Item is not available for booking", ex.getMessage());
    }

    @Test
    void addBooking_whenUserIsOwner_thenReturnOwnerException() {
        LocalDateTime newStart = LocalDateTime.now();
        LocalDateTime newEnd = newStart.minusDays(1);
        BookingRequest newBookingRequest = new BookingRequest(item.getId(), newStart, newEnd);

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        OwnerException ex = assertThrows(OwnerException.class,
                () -> bookingService.addBooking(owner.getId(), newBookingRequest));

        assertEquals("User is the owner of the item", ex.getMessage());
    }

    @Test
    void addBooking_whenDateIsWrong_thenReturnValidationException() {
        LocalDateTime newStart = LocalDateTime.now();
        LocalDateTime newEnd = newStart.minusDays(1);
        BookingRequest newBookingRequest = new BookingRequest(item.getId(), newStart, newEnd);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        ValidationException ex = assertThrows(ValidationException.class,
                () -> bookingService.addBooking(user.getId(), newBookingRequest));
        assertEquals("Wrong booking time", ex.getMessage());
    }

    @Test
    void setBookingStatus_whenOwnerApprovedBooking_thenReturnBookingWithStatusApproved() {
        bookingCurrent.setStatus(BookingStatus.WAITING);
        when(bookingRepository.findById(bookingCurrent.getId())).thenReturn(Optional.of(bookingCurrent));
        when(bookingRepository.save(bookingCurrent)).thenReturn(bookingCurrent);

        BookingDto actualBooking = bookingService.setBookingStatus(owner.getId(), bookingCurrent.getId(), true);

        assertEquals(bookingCurrent.getStatus(), actualBooking.getStatus());
    }

    @Test
    void setBookingStatus_whenOwnerRejectedBooking_thenReturnBookingWithStatusRejected() {
        bookingCurrent.setStatus(BookingStatus.WAITING);
        when(bookingRepository.findById(bookingCurrent.getId())).thenReturn(Optional.of(bookingCurrent));
        when(bookingRepository.save(bookingCurrent)).thenReturn(bookingCurrent);

        BookingDto actualBooking = bookingService.setBookingStatus(owner.getId(), bookingCurrent.getId(), false);

        assertEquals(bookingCurrent.getStatus(), actualBooking.getStatus());
    }

    @Test
    void setBookingStatus_whenStatusIsAlreadyApproved_thenReturnValidationException() {
        when(bookingRepository.findById(bookingCurrent.getId())).thenReturn(Optional.of(bookingCurrent));

        ValidationException ex = assertThrows(ValidationException.class,
                () -> bookingService.setBookingStatus(owner.getId(), bookingCurrent.getId(), true));
        assertEquals("Booking is already confirmed", ex.getMessage());
    }

    @Test
    void setBookingStatus_whenUserIsNotTheOwnerOfTheItem_thenReturnOwnerException() {
        when(bookingRepository.findById(bookingCurrent.getId())).thenReturn(Optional.of(bookingCurrent));

        OwnerException ex = assertThrows(OwnerException.class,
                () -> bookingService.setBookingStatus(user.getId(), bookingCurrent.getId(), true));
        assertEquals("User is not the owner of the item", ex.getMessage());
    }

    @Test
    void getBookingById() {
        when(bookingRepository.findById(bookingCurrent.getId())).thenReturn(Optional.of(bookingCurrent));

        BookingDto expectedBooking = BookingMapper.toBookingDto(bookingCurrent);
        BookingDto actualBooking = bookingService.getBookingById(user.getId(), bookingCurrent.getId());

        assertEquals(expectedBooking, actualBooking);
    }

    @Test
    void getBookingById_whenBookingNotFound_thenReturnEntityNotFoundException() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> bookingService.getBookingById(user.getId(), 0L));
        assertEquals("No booking with id " + 0L, ex.getMessage());
    }

    @Test
    void getBookingById_whenUserNotFound_thenReturnEntityNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> bookingService.getBookingById(0L, bookingCurrent.getId()));
        assertEquals("No user with id " + 0L, ex.getMessage());
    }

    @Test
    void getBookingById_whenUserIsNeitherTheOwnerNorTheBooker_thenReturnEntityNotFoundException() {
        User newUser = setUser(3L, "user3", "user3@user.com");

        when(userRepository.findById(newUser.getId())).thenReturn(Optional.of(newUser));
        when(bookingRepository.findById(bookingCurrent.getId())).thenReturn(Optional.of(bookingCurrent));

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> bookingService.getBookingById(3L, bookingCurrent.getId()));
        assertEquals("Booking is not available for viewing", ex.getMessage());
    }

    private User setUser(Long id, String name, String email) {
        User newUser = new User();
        newUser.setId(id);
        newUser.setName(name);
        newUser.setEmail(email);
        return newUser;
    }
}
