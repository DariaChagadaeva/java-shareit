package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.dto.BookingForItem;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Page<Booking> findAllByBookerIdOrderByStartDesc(Long userId, Pageable page);

    Page<Booking> findAllByBookerIdAndEndIsBeforeOrderByStartDesc(Long userId, LocalDateTime now, Pageable page);

    Page<Booking> findAllByBookerIdAndStartIsAfterOrderByStartDesc(Long userId, LocalDateTime now, Pageable page);

    @Query("select new ru.practicum.shareit.booking.model.Booking" +
            "(b.id, b.start, b.end, b.item, b.booker, b.status) " +
            "from Booking as b " +
            "where b.booker.id = ?1 " +
            "and (?2 between b.start and b.end) " +
            "order by b.start desc")
    Page<Booking> findAllByBookerIdCurrentBookings(Long userId, LocalDateTime now, Pageable page);

    Page<Booking> findALLByBookerIdAndStatusOrderByStartDesc(Long userId, BookingStatus status, Pageable page);

    @Query("select new ru.practicum.shareit.booking.model.Booking" +
            "(b.id, b.start, b.end, b.item, b.booker, b.status) " +
            "from Booking as b " +
            "where b.item.ownerId = ?1 " +
            "order by b.start desc")
    Page<Booking> findAllByOwnerIdOrderByStartDesc(Long ownerId, Pageable page);

    @Query("select new ru.practicum.shareit.booking.model.Booking" +
            "(b.id, b.start, b.end, b.item, b.booker, b.status) " +
            "from Booking as b " +
            "where b.item.ownerId = ?1 " +
            "and b.end < ?2 " +
            "order by b.start desc")
    Page<Booking> findAllByOwnerIdAndEndIsBeforeOrderByStartDesc(Long ownerId, LocalDateTime now, Pageable page);

    @Query("select new ru.practicum.shareit.booking.model.Booking" +
            "(b.id, b.start, b.end, b.item, b.booker, b.status) " +
            "from Booking as b " +
            "where b.item.ownerId = ?1 " +
            "and b.start > ?2 " +
            "order by b.start desc")
    Page<Booking> findAllByOwnerIdAndStartAfterOrderByStartDesc(Long ownerId, LocalDateTime now, Pageable page);

    @Query("select new ru.practicum.shareit.booking.model.Booking" +
            "(b.id, b.start, b.end, b.item, b.booker, b.status) " +
            "from Booking as b " +
            "where b.item.ownerId = ?1 " +
            "and (?2 between b.start and b.end) " +
            "order by b.start desc")
    Page<Booking> findAllByOwnerIdCurrentBookings(Long ownerId, LocalDateTime now, Pageable page);

    @Query("select new ru.practicum.shareit.booking.model.Booking" +
            "(b.id, b.start, b.end, b.item, b.booker, b.status) " +
            "from Booking as b " +
            "where b.item.ownerId = ?1 " +
            "and b.status = ?2 " +
            "order by b.start desc")
    Page<Booking> findAllByOwnerIdAndStatusOrderByStartDesc(Long ownerId, BookingStatus status, Pageable page);

    @Query("select new ru.practicum.shareit.booking.dto.BookingForItem(b.id, b.booker.id) " +
            "from Booking as b " +
            "where b.item.id = ?1 " +
            "and b.start > ?2 " +
            "and b.status = ?3 " +
            "order by b.start asc")
    List<BookingForItem> findNextBookingForItem(Long itemId, LocalDateTime currentTime, BookingStatus status);

    @Query("select new ru.practicum.shareit.booking.dto.BookingForItem(b.id, b.booker.id) " +
            "from Booking as b " +
            "where b.item.id = ?1 " +
            "and b.start < ?2 " +
            "and b.status = ?3 " +
            "order by b.start desc")
    List<BookingForItem> findLastBookingForItem(Long itemId, LocalDateTime currentTime, BookingStatus status);

    List<Booking> findByBookerIdAndItemId(Long bookerId, Long itemId);
}
