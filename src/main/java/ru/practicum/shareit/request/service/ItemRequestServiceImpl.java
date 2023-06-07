package ru.practicum.shareit.request.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.EntityNotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemRequestServiceImpl implements ItemRequestService {
    final ItemRequestRepository itemRequestRepository;
    final UserRepository userRepository;

    @Override
    public ItemRequestDto addItemRequest(Long userId, ItemRequestDto itemRequestDto) {
        ItemRequest itemRequest = ItemRequestMapper.fromDtoToModel(itemRequestDto);
        itemRequest.setUser(getUserIfItExists(userId));
        itemRequest.setItems(new ArrayList<>());
        ItemRequest newItemRequest = itemRequestRepository.save(itemRequest);
        log.info("New item request added : {}", newItemRequest);
        return ItemRequestMapper.fromModelToDto(newItemRequest);
    }

    @Override
    public List<ItemRequestDto> getAllUserRequests(Long userId) {
        checkUser(userId);
        return itemRequestRepository.findAllByUserId(userId).stream()
                .map(ItemRequestMapper::fromModelToDto).collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getRequestById(Long userId, Long requestId) {
        checkUser(userId);
        checkRequest(requestId);
        return ItemRequestMapper.fromModelToDto(itemRequestRepository.findAllById(requestId));
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId, int from, int size) {
        checkUser(userId);
        Sort sort = Sort.by("created");
        PageRequest page = PageRequest.of(from, size, sort);
        return itemRequestRepository.findAllByUserIdIsNot(userId, page)
                .getContent().stream().map(ItemRequestMapper::fromModelToDto)
                .collect(Collectors.toList());
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

    private boolean checkRequest(Long requestId) {
        if (itemRequestRepository.findById(requestId).isEmpty()) {
            throw new EntityNotFoundException("No request with id " + requestId);
        } else {
            return true;
        }
    }
}
