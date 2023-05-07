package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    Item addItem(Item item);

    Item updateItem(Long itemId, Item item);

    Optional<Item> getItemById(Long itemId);

    List<Item> getAllItems();

    void deleteItemById(Long itemId);
}
