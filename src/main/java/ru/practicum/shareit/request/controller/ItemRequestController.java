package ru.practicum.shareit.request.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Validated
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemRequestController {
    final ItemRequestService itemRequestService;
    static final String USER_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ItemRequestDto addItemRequest(@RequestHeader(USER_HEADER) Long userId,
                                     @Valid @RequestBody ItemRequestDto itemRequestDto) {
        return itemRequestService.addItemRequest(userId, itemRequestDto);
    }

    @GetMapping
    public List<ItemRequestDto> getAllUserRequests(@RequestHeader(USER_HEADER) Long userId) {
        return itemRequestService.getAllUserRequests(userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getRequestById(@RequestHeader(USER_HEADER) Long userId,
                                         @PathVariable("requestId") Long requestId) {
        return itemRequestService.getRequestById(userId, requestId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllRequests(@RequestHeader(USER_HEADER) Long userId,
                                               @RequestParam(value = "from", defaultValue = "0") @Min(0) int from,
                                               @RequestParam(value = "size", defaultValue = "10") @Min(1) int size) {
        return itemRequestService.getAllRequests(userId, from, size);
    }
}
