package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingForItem;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithDates;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    ItemService itemService;

    ItemDto itemDto;
    ItemWithDates itemWithDates;
    CommentDto comment;
    LocalDateTime createdTime = LocalDateTime.now();

    @BeforeEach
    void start() {
        itemDto = new ItemDto(1L, "item", "item desc", true, 1L);
        comment = new CommentDto(1L, "comment", "user", createdTime);
        itemWithDates = new ItemWithDates(
                1L, "item1", "item1 desc", true,
                new BookingForItem(1L, 1L), new BookingForItem(2L, 1L), List.of(comment));
    }

    @SneakyThrows
    @Test
    void addItem_whenItemIsValid_thenReturnSavedItem() {
        when(itemService.addItem(anyLong(), any())).thenReturn(itemDto);

        String result = mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(itemDto), result);
        verify(itemService).addItem(1L, itemDto);
    }

    @SneakyThrows
    @Test
    void addItem_whenItemIsNotValid_thenReturnValidationException() {
        itemDto.setName("");
        when(itemService.addItem(anyLong(), any())).thenThrow(new ValidationException());

        mockMvc.perform(post("/items")
                .header("X-Sharer-User-Id", 1)
                .content(objectMapper.writeValueAsString(itemDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(itemService, never()).addItem(1L, itemDto);
    }

    @SneakyThrows
    @Test
    void updateItem() {
        itemDto.setName("updateName");
        when(itemService.updateItem(anyLong(), anyLong(), any())).thenReturn(itemDto);

        mockMvc.perform(patch("/items/{itemId}", 1)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$.requestId", is(itemDto.getRequestId()), Long.class));
        verify(itemService).updateItem(1L, 1L, itemDto);
    }

    @SneakyThrows
    @Test
    void getItemById() {
        when(itemService.getItemById(anyLong(), anyLong())).thenReturn(itemWithDates);

        String result = mockMvc.perform(get("/items/{itemId}", 1)
                .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(itemWithDates), result);
        verify(itemService).getItemById(1L, 1L);
    }

    @SneakyThrows
    @Test
    void getUserItems() {
        when(itemService.getUserItems(anyLong(), anyInt(), anyInt())).thenReturn(List.of(itemWithDates));

        mockMvc.perform(get("/items")
                .header("X-Sharer-User-Id", 1)
                .param("from", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(itemWithDates.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemWithDates.getName())))
                .andExpect(jsonPath("$[0].description", is(itemWithDates.getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemWithDates.getAvailable())))
                .andExpect(jsonPath("$[0].lastBooking.id", is(itemWithDates.getLastBooking().getId()), Long.class))
                .andExpect(jsonPath("$[0].lastBooking.bookerId", is(itemWithDates.getLastBooking().getBookerId()), Long.class))
                .andExpect(jsonPath("$[0].nextBooking.id", is(itemWithDates.getNextBooking().getId()), Long.class))
                .andExpect(jsonPath("$[0].nextBooking.bookerId", is(itemWithDates.getNextBooking().getBookerId()), Long.class))
                .andExpect(jsonPath("$[0].comments", hasSize(1)))
                .andExpect(jsonPath("$[0].comments[0].id", is(comment.getId()), Long.class))
                .andExpect(jsonPath("$[0].comments[0].text", is(comment.getText())))
                .andExpect(jsonPath("$[0].comments[0].authorName", is(comment.getAuthorName())));
    }

    @SneakyThrows
    @Test
    void searchItems() {
        when(itemService.searchItems(anyString(), anyInt(), anyInt())).thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items/search")
                        .param("text", "item")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is(itemDto.getName())));
    }

    @SneakyThrows
    @Test
    void addComment() {
        when(itemService.addComment(anyLong(), anyLong(), any())).thenReturn(comment);

        String result = mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(comment))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(comment), result);
        verify(itemService).addComment(anyLong(), anyLong(), any());
    }
}