package ru.practicum.shareit.request;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.EntityNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.ValidationException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@FieldDefaults(level = AccessLevel.PRIVATE)
class ItemRequestServiceImplTest {
    @Autowired
    UserService userService;

    @Autowired
    ItemRequestService itemRequestService;

    @Autowired
    ItemService itemService;

    UserDto userDto;
    UserDto owner;
    ItemRequestDto itemRequestDto;

    @BeforeEach
    void start() {
        userDto = userService.addUser(UserDto.builder()
                .name("user")
                .email("user@user.com")
                .build());
        owner = userService.addUser(UserDto.builder()
                .name("owner")
                .email("owner@user.com")
                .build());
        itemRequestDto = itemRequestService.addItemRequest(userDto.getId(), new ItemRequestDto("item request desc"));
        ItemDto newItem = ItemDto.builder().name("item").description("item desc").available(true).requestId(itemRequestDto.getId()).build();
        itemService.addItem(owner.getId(), newItem);
    }

    @Test
    void addItemRequest_whenUserFoundAndDescriptionNotEmpty_thenSavedRequest() {
        assertThat(userDto.getId(), notNullValue());
        assertThat(itemRequestService.getAllUserRequests(userDto.getId()), hasSize(1));
        assertThat(itemRequestDto, equalTo(itemRequestService.getRequestById(userDto.getId(), itemRequestDto.getId())));
    }

    @Test
    void addItemRequest_whenUserNotFound_thenReturnEntityNotFoundException() {
        assertThrows(EntityNotFoundException.class,
                () -> itemRequestService.addItemRequest(0L, itemRequestDto));
    }

    @Test
    void addItemRequest_whenUserFoundAndDescriptionIsEmpty_thenReturnValidationException() {
        ItemRequestDto newItemRequestDto = new ItemRequestDto(null);

        assertThrows(ValidationException.class,
                () -> itemRequestService.addItemRequest(userDto.getId(), newItemRequestDto));
    }

    @Test
    void getAllUserRequests() {
        itemRequestService.addItemRequest(userDto.getId(), new ItemRequestDto("second request"));

        assertThat(itemRequestService.getAllUserRequests(userDto.getId()), hasSize(2));
    }

    @Test
    void getAllUserRequests_whenUserNotFound_thenReturnEntityNotFoundException() {
        assertThrows(EntityNotFoundException.class,
                () -> itemRequestService.getAllUserRequests(0L));
    }

    @Test
    void getRequestById_whenUserAndRequestFound_thenReturnedRequest() {
        assertThat(itemRequestService.getRequestById(userDto.getId(), itemRequestDto.getId()), equalTo(itemRequestDto));
    }

    @Test
    void getRequestById_whenRequestNotFound_thenReturnEntityNotFoundException() {
        assertThrows(EntityNotFoundException.class,
                () -> itemRequestService.getRequestById(userDto.getId(), 0L));
    }
    @Test
    void getAllRequests_whenUserFound_thenReturnRequestsList() {
        assertThat(itemRequestService.getAllRequests(owner.getId(), 0, 10), hasSize(1));
    }

    @Test
    void getAllRequests_whenUserNotFound_thenReturnEntityNotFoundException() {
        assertThrows(EntityNotFoundException.class,
                () -> itemRequestService.getAllRequests(0L, 0, 10));
    }
}