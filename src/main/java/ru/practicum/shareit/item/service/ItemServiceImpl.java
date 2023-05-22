package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingForItem;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.EntityNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentRequest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithDates;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Transactional
    @Override
    public ItemDto addItem(Long userId, ItemDto itemDto) {
        checkUser(userId);
        Item item = ItemMapper.fromDtoToItem(itemDto);
        item.setOwnerId(userId);
        itemRepository.save(item);
        log.info("New item added : {}", item);
        return ItemMapper.toItemDto(item);
    }

    @Transactional
    @Override
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        Item item = getItemIfItExists(userId, itemId);
        if (!item.getOwnerId().equals(userId)) {
            throw new EntityNotFoundException("No owner with id " + userId);
        }
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        log.info("Item updated : {}", item);
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemWithDates getItemById(Long userId, Long itemId) {
        Item item = getItemIfItExists(userId, itemId);
        ItemWithDates itemWithDates = ItemMapper.toItemWithDatesDto(item);
        LocalDateTime currentTime = LocalDateTime.now();
        if (item.getOwnerId().equals(userId)) {
            BookingForItem lastBooking = bookingRepository.findLastBookingForItem(itemId, currentTime, BookingStatus.APPROVED)
                    .stream().findFirst().orElse(null);
            BookingForItem nextBooking = bookingRepository.findNextBookingForItem(itemId, currentTime, BookingStatus.APPROVED)
                    .stream().findFirst().orElse(null);
            itemWithDates.setLastBooking(lastBooking);
            itemWithDates.setNextBooking(nextBooking);
        }
        List<CommentDto> comments = commentRepository.findAllByItem(itemId);
        itemWithDates.setComments(comments);
        return itemWithDates;
    }

    @Override
    public List<ItemWithDates> getUserItems(Long userId) {
        getUserIfItExists(userId);
        List<Item> items = itemRepository.findAllByOwnerId(userId);
        return items.stream().map(item -> getItemById(userId, item.getId()))
                .sorted(Comparator.comparing(ItemWithDates::getId)).collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text.isEmpty()) {
            return List.of();
        }
        String textSearch = text.toLowerCase();
        return itemRepository.findAll().stream()
                .filter(item -> item.getName().toLowerCase().contains(textSearch)
                || (item.getDescription().toLowerCase().contains(textSearch) && item.getAvailable()))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto addComment(Long userId, Long itemId, CommentRequest commentRequest) {
        checkUserToComment(userId, itemId);
        Comment comment = CommentMapper.toCommentModel(commentRequest);
        comment.setItem(getItemIfItExists(userId, itemId));
        comment.setAuthor(getUserIfItExists(userId));
        Comment newComment = commentRepository.save(comment);
        log.info("New comment added : {}", newComment);
        return CommentMapper.toCommentDto(newComment);
    }

    private User getUserIfItExists(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("No user with id " + userId));
    }

    private boolean checkUser(Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new EntityNotFoundException("No user with id " + userId);
        } else {
            return true;
        }
    }

    private Item getItemIfItExists(Long userId, Long itemId) {
        getUserIfItExists(userId);
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("No item with id " + itemId));
    }

    private boolean checkUserToComment(Long userId, Long itemId) {
        LocalDateTime currentTime = LocalDateTime.now();
        List<Booking> bookings = bookingRepository.findByBookerIdAndItemId(userId, itemId).stream()
                .filter(booking -> booking.getEnd().isBefore(currentTime)).collect(Collectors.toList());
        if (bookings.isEmpty()) {
            throw new ValidationException("No booking to comment");
        } else {
            return true;
        }
    }
}
