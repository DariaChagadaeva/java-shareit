package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
class ItemRequestControllerTest {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MockMvc mockMvc;
    @MockBean
    ItemRequestService itemRequestService;
    LocalDateTime createdTime;
    ItemRequestDto itemRequestDto;

    @BeforeEach
    private void addRequests() {
        createdTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        List<ItemDto> items = List.of(new ItemDto(1L, "name1", "desc1", true, 1L));
        itemRequestDto = new ItemRequestDto(1L, "desc1", createdTime, items);
    }

    @SneakyThrows
    @Test
    void addItemRequest() {
        when(itemRequestService.addItemRequest(anyLong(), any())).thenReturn(itemRequestDto);

        String result = mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequestDto)))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(itemRequestDto), result);

        verify(itemRequestService).addItemRequest(1L, itemRequestDto);
    }

    @SneakyThrows
    @Test
    void getAllUserRequests() {
        when(itemRequestService.getAllUserRequests(anyLong())).thenReturn(List.of(itemRequestDto));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].description", is("desc1")))
                .andExpect(jsonPath("$[0].created", is(createdTime.toString())))
                .andExpect(jsonPath("$[0].items", hasSize(1)))
                .andExpect(jsonPath("$[0].items[0].id", is(1)))
                .andExpect(jsonPath("$[0].items[0].name", is("name1")))
                .andExpect(jsonPath("$[0].items[0].description", is("desc1")))
                .andExpect(jsonPath("$[0].items[0].requestId", is(1)));
    }

    @SneakyThrows
    @Test
    void getRequestById() {
        Mockito.when(itemRequestService.getRequestById(anyLong(), anyLong()))
                        .thenReturn(itemRequestDto);

        mockMvc.perform(get("/requests/{requestId}", 1L)
                        .header("X-Sharer-User-Id", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$.created", is(createdTime.toString())))
                .andExpect(jsonPath("$.items", hasSize(1)));

        verify(itemRequestService).getRequestById(1L, 1L);
    }

    @SneakyThrows
    @Test
    void getAllRequests() {
        when(itemRequestService.getAllRequests(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(itemRequestDto));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", "1")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].description", is("desc1")))
                .andExpect(jsonPath("$[0].created", is(createdTime.toString())))
                .andExpect(jsonPath("$[0].items", hasSize(1)))
                .andExpect(jsonPath("$[0].items[0].id", is(1)))
                .andExpect(jsonPath("$[0].items[0].name", is("name1")))
                .andExpect(jsonPath("$[0].items[0].description", is("desc1")))
                .andExpect(jsonPath("$[0].items[0].requestId", is(1)));
    }
}