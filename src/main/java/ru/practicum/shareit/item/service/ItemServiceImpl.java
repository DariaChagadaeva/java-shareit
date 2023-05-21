package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.EntityNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemDto addItem(Long userId, ItemDto itemDto) {
        User user = getUserIfItExists(userId);
        Item item = ItemMapper.fromDtoToItem(itemDto);
        item.setOwner(user);
        itemRepository.addItem(item);
        log.info("New item added : {}", item);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        getUserIfItExists(userId);
        Item item = getItemIfItExists(userId, itemId);
        if (!item.getOwner().getId().equals(userId)) {
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
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto getItemById(Long userId, Long itemId) {
        return ItemMapper.toItemDto(getItemIfItExists(userId, itemId));
    }

    @Override
    public List<ItemDto> getUserItems(Long userId) {
        getUserIfItExists(userId);
        return itemRepository.getAllItems().stream().filter(item
                -> item.getOwner().getId().equals(userId))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text.isEmpty()) {
            return List.of();
        }
        String textSearch = text.toLowerCase();
        return itemRepository.getAllItems().stream()
                .filter(item -> item.getName().toLowerCase().contains(textSearch)
                || (item.getDescription().toLowerCase().contains(textSearch) && item.getAvailable()))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    private User getUserIfItExists(Long userId) {
        return userRepository.getUserById(userId)
                .orElseThrow(() -> new EntityNotFoundException("No user with id " + userId));
    }

    private Item getItemIfItExists(Long userId, Long itemId) {
        getUserIfItExists(userId);
        return itemRepository.getItemById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("No item with id " + itemId));
    }
}
