package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import ru.practicum.shareit.exceptions.EntityAlreadyExistsException;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.ValidationException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
class UserControllerTest {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserService userService;

    UserDto userDto;

    @BeforeEach
    private void addUsers() {
        userDto = new UserDto(1L, "user1", "user1@user.com");
    }

    @SneakyThrows
    @Test
    void addUser_whenUserIsValid_thenReturnSavedUser() {
        when(userService.addUser(any())).thenReturn(userDto);

        String result = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(userDto), result);
        verify(userService).addUser(userDto);
    }

    @SneakyThrows
    @Test
    void addUser_whenUserIsNotValid_thenReturnBadRequest() {
        userDto.setEmail("wrongEmail");
        when(userService.addUser(userDto)).thenThrow(new ValidationException());

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).addUser(userDto);
    }

    @SneakyThrows
    @Test
    void addUser_whenUserHasDuplicateEmail_thenReturnEntityAlreadyExistsException() {
        UserDto newUser = UserDto.builder()
                .name("user2")
                .email("user123@user.com")
                .build();
        when(userService.addUser(newUser))
                .thenThrow(new EntityAlreadyExistsException("User with email " + userDto.getEmail() + " already exists"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser))
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isConflict());

        verify(userService, never()).addUser(userDto);
    }

    @SneakyThrows
    @Test
    void updateUser() {
        userDto.setName("updateName");
        when(userService.updateUser(anyLong(), any())).thenReturn(userDto);

        mockMvc.perform(patch("/users/{userId}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));
        verify(userService).updateUser(1L, userDto);
    }

    @SneakyThrows
    @Test
    void getUserById() {
        when(userService.getUserById(anyLong())).thenReturn(userDto);

        String result = mockMvc.perform(get("/users/{userId}", 1))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(userDto), result);
        verify(userService).getUserById(1L);
    }

    @SneakyThrows
    @Test
    void getAllUsers() {
        when(userService.getAllUsers()).thenReturn(List.of(userDto));

        String result = mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(List.of(userDto)), result);
        verify(userService).getAllUsers();
    }

    @SneakyThrows
    @Test
    void deleteUser() {
        doNothing().when(userService).deleteUserById(anyLong());

        mockMvc.perform(delete("/users/{userId}", 1))
                .andExpect(status().isOk());

        verify(userService).deleteUserById(1L);
    }
}