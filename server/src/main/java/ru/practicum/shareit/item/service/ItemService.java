package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentRequest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithDates;

import java.util.List;

public interface ItemService {
    ItemDto addItem(Long userId, ItemDto itemDto);

    ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto);

    ItemWithDates getItemById(Long userId, Long itemId);

    List<ItemWithDates> getUserItems(Long userId, int from, int size);

    List<ItemDto> searchItems(String text, int from, int size);

    CommentDto addComment(Long userId, Long itemId, CommentRequest commentRequest);

}
