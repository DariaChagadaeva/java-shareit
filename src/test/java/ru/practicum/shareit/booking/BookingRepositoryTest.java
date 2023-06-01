package ru.practicum.shareit.booking;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.dto.BookingForItem;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@FieldDefaults(level = AccessLevel.PRIVATE)
class BookingRepositoryTest {

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ItemRepository itemRepository;

    User user;
    User owner;
    Item item;
    Booking bookingCurrent;
    Booking bookingFuture;
    Booking bookingPast;
    LocalDateTime currentTime = LocalDateTime.now();
    Pageable page = PageRequest.of(0, 10);
    @BeforeEach
    void start() {
        LocalDateTime pastTime = currentTime.minusDays(10);
        LocalDateTime futureTime = currentTime.plusDays(10);

        User newUser = new User("user", "user@user.com");
        user = userRepository.save(newUser);

        User newOwner = new User("owner", "owner@user.com");
        owner = userRepository.save(newOwner);

        Item newItem = Item.builder().name("item").description("desc").available(true).ownerId(owner.getId()).requestId(1L).build();
        item = itemRepository.save(newItem);

        Item newItem2 = Item.builder().name("item2").description("desc2").available(true).ownerId(user.getId()).requestId(1L).build();
        Item item2 = itemRepository.save(newItem2);

        Booking newBookingCurrent = Booking.builder().start(currentTime).end(currentTime.plusHours(1)).item(item).booker(user).status(BookingStatus.WAITING).build();
        bookingCurrent = bookingRepository.save(newBookingCurrent);

        Booking newBookingFuture = Booking.builder().start(futureTime).end(futureTime.plusHours(1)).item(item).booker(user).status(BookingStatus.WAITING).build();
        bookingFuture = bookingRepository.save(newBookingFuture);

        Booking newBookingPast = Booking.builder().start(pastTime).end(pastTime.plusHours(1)).item(item2).booker(user).status(BookingStatus.WAITING).build();
        bookingPast = bookingRepository.save(newBookingPast);
    }

    @Test
    void findAllByBookerIdOrderByStartDesc() {
        Page<Booking> bookings = bookingRepository.findAllByBookerIdOrderByStartDesc(user.getId(), page);

        assertTrue(bookings.hasContent());
        assertEquals(3, bookings.getContent().size());
    }

    @Test
    void findAllByBookerIdAndEndIsBeforeOrderByStartDesc() {
        Page<Booking> bookings = bookingRepository.findAllByBookerIdAndEndIsBeforeOrderByStartDesc(user.getId(), currentTime, page);

        assertTrue(bookings.hasContent());
        assertEquals(1, bookings.getContent().size());
        assertEquals(3, bookings.getContent().get(0).getId());
    }

    @Test
    void findAllByBookerIdAndStartIsAfterOrderByStartDesc() {
        Page<Booking> bookings = bookingRepository.findAllByBookerIdAndStartIsAfterOrderByStartDesc(user.getId(), currentTime, page);

        assertTrue(bookings.hasContent());
        assertEquals(1, bookings.getContent().size());
        assertEquals(2, bookings.getContent().get(0).getId());
    }

    @Test
    void findAllByBookerIdCurrentBookings() {
        Page<Booking> bookings = bookingRepository.findAllByBookerIdCurrentBookings(user.getId(), currentTime, page);

        assertTrue(bookings.hasContent());
        assertEquals(1, bookings.getContent().size());
        assertEquals(1, bookings.getContent().get(0).getId());
    }

    @Test
    void findALLByBookerIdAndStatusOrderByStartDesc() {
        Page<Booking> bookings = bookingRepository.findALLByBookerIdAndStatusOrderByStartDesc(user.getId(), BookingStatus.APPROVED, page);

        assertFalse(bookings.hasContent());
        assertEquals(0, bookings.getContent().size());
    }

    @Test
    void findAllByOwnerIdOrderByStartDesc() {
        Page<Booking> bookings = bookingRepository.findAllByOwnerIdOrderByStartDesc(user.getId(), page);

        assertTrue(bookings.hasContent());
        assertEquals(1, bookings.getContent().size());
    }

    @Test
    void findAllByOwnerIdAndEndIsBeforeOrderByStartDesc() {
        Page<Booking> bookings = bookingRepository.findAllByOwnerIdAndEndIsBeforeOrderByStartDesc(user.getId(), currentTime, page);

        assertTrue(bookings.hasContent());
        assertEquals(1, bookings.getContent().size());
    }

    @Test
    void findAllByOwnerIdAndStartAfterOrderByStartDesc() {
        Page<Booking> bookings = bookingRepository.findAllByOwnerIdAndStartAfterOrderByStartDesc(user.getId(), currentTime, page);

        assertFalse(bookings.hasContent());
        assertEquals(0, bookings.getContent().size());
    }

    @Test
    void findAllByOwnerIdCurrentBookings() {
        Page<Booking> bookings = bookingRepository.findAllByOwnerIdCurrentBookings(user.getId(), currentTime, page);

        assertFalse(bookings.hasContent());
        assertEquals(0, bookings.getContent().size());
    }

    @Test
    void findAllByOwnerIdAndStatusOrderByStartDesc() {
        Page<Booking> bookings = bookingRepository.findAllByOwnerIdAndStatusOrderByStartDesc(user.getId(), BookingStatus.WAITING, page);

        assertTrue(bookings.hasContent());
        assertEquals(1, bookings.getContent().size());
    }

    @Test
    void findNextBookingForItem() {
        bookingCurrent.setStatus(BookingStatus.APPROVED);
        bookingFuture.setStatus(BookingStatus.APPROVED);

        List<BookingForItem> bookings = bookingRepository.findNextBookingForItem(item.getId(), currentTime, BookingStatus.APPROVED);

        assertFalse(bookings.isEmpty());
        assertEquals(1, bookings.size());
        assertEquals(2, bookings.get(0).getId());
    }

    @Test
    void findLastBookingForItem() {
        bookingCurrent.setStatus(BookingStatus.APPROVED);
        bookingFuture.setStatus(BookingStatus.APPROVED);

        List<BookingForItem> bookings = bookingRepository.findLastBookingForItem(item.getId(), currentTime, BookingStatus.APPROVED);

        assertTrue(bookings.isEmpty());
    }

    @Test
    void findByBookerIdAndItemId() {
        List<Booking> bookings = bookingRepository.findByBookerIdAndItemId(user.getId(), item.getId());

        assertFalse(bookings.isEmpty());
        assertEquals(2, bookings.size());
        assertEquals(1, bookings.get(0).getId());
        assertEquals(2, bookings.get(1).getId());
    }
}