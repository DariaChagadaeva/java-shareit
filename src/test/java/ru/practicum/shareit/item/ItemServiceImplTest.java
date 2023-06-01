package ru.practicum.shareit.item;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequest;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exceptions.EntityNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentRequest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@FieldDefaults(level = AccessLevel.PRIVATE)
class ItemServiceImplTest {

    @Autowired
    ItemService itemService;

    @Autowired
    UserService userService;

    @Autowired
    ItemRequestService itemRequestService;

    @Autowired
    BookingService bookingService;

    UserDto userDto;

    UserDto owner;

    ItemDto itemDto;

    ItemRequestDto itemRequestDto;

    @BeforeEach
    void start() {
        UserDto newUser = UserDto.builder()
                .name("user")
                .email("user1@user.com")
                .build();
        userDto = userService.addUser(newUser);

        UserDto newOwner = UserDto.builder()
                .name("owner")
                .email("owner@user.com")
                .build();
        owner = userService.addUser(newOwner);

        itemRequestDto = itemRequestService.addItemRequest(userDto.getId(), new ItemRequestDto("item request"));

        ItemDto item = ItemDto.builder()
                .name("item")
                .description("desc")
                .available(true)
                .requestId(itemRequestDto.getId())
                .build();
        itemDto = itemService.addItem(owner.getId(), item);
    }

    @Test
    void addItem_whenUserFound_thenReturnItem() {
        assertThat(itemService.getItemById(1L, 1L).getName(), equalTo(itemDto.getName()));
    }

    @Test
    void addItem_whenUserNotFound_thenReturnEntityNotFoundException() {
        assertThrows(EntityNotFoundException.class,
                () -> itemService.addItem(0L, itemDto));
    }

    @Test
    void updateItem_whenUserAndItemFound_thenReturnUpdatedItem() {
        ItemDto newItem = itemDto;
        newItem.setName("newItem");

        assertThat(itemService.updateItem(owner.getId(), newItem.getId(), newItem), equalTo(itemDto));
    }

    @Test
    void updateItem_whenUserIsNotTheOwner_thenReturnEntityNotFoundException() {
        assertThrows(EntityNotFoundException.class,
                () -> itemService.updateItem(1L, 1L, itemDto));
    }

    @Test
    void getItemById() {
        assertThat(itemDto.getName(), equalTo(itemService.getItemById(1L, 1L).getName()));
        assertThat(itemDto.getDescription(), equalTo(itemService.getItemById(1L, 1L).getDescription()));

    }

    @Test
    void getUserItems() {
        assertThat(itemService.getUserItems(owner.getId(), 0, 10), hasSize(1));
        assertThat(itemService.getUserItems(owner.getId(), 0, 10).get(0).getName(), equalTo(itemDto.getName()));
        assertThat(itemService.getUserItems(owner.getId(), 0, 10).get(0).getLastBooking(), is(nullValue()));
        assertThat(itemService.getUserItems(owner.getId(), 0, 10).get(0).getNextBooking(), is(nullValue()));
        assertThat(itemService.getUserItems(owner.getId(), 0, 10).get(0).getComments(), is(empty()));
    }

    @Test
    void searchItems_whenTextIsNotEmpty_ReturnListOfItems() {
        assertThat(itemService.searchItems("item", 0, 10), hasSize(1));
    }

    @Test
    void searchItems_whenTextIsEmpty_ReturnEmptyListOfItems() {
        assertThat(itemService.searchItems("", 0, 10), hasSize(0));
    }

    @Test
    void addComment() throws InterruptedException {
        LocalDateTime start = LocalDateTime.now().plusSeconds(1);
        LocalDateTime end = start.plusSeconds(5);
        BookingDto bookingDto = bookingService.addBooking(userDto.getId(), new BookingRequest(itemDto.getId(), start, end));
        bookingService.setBookingStatus(owner.getId(), bookingDto.getId(), true);
        TimeUnit.SECONDS.sleep(10);
        CommentDto commentDto = itemService.addComment(userDto.getId(), itemDto.getId(), new CommentRequest("comment"));

        assertThat(itemService.getItemById(owner.getId(), itemDto.getId()).getComments(), hasSize(1));
        assertThat(itemService.getItemById(owner.getId(), itemDto.getId()).getComments().get(0).getText(), is(commentDto.getText()));
        assertThat(itemService.getItemById(owner.getId(), itemDto.getId()).getComments().get(0).getAuthorName(), is(userDto.getName()));
    }

    @Test
    void addComment_whenBookingNotAvailableToComment_thenReturnValidationException() throws InterruptedException {
        assertThrows(ValidationException.class,
                () -> itemService.addComment(1L, 1L, new CommentRequest("comment")));
    }
}