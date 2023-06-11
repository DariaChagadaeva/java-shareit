package ru.practicum.shareit.user;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.EntityNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@FieldDefaults(level = AccessLevel.PRIVATE)
class UserServiceImplTest {
    @Autowired
    UserService userService;
    UserDto userDto;
    UserDto userDto2;

    @BeforeEach
    void start() {
        UserDto newUser = UserDto.builder()
                .name("user")
                .email("user1@user.com")
                .build();
        userDto = userService.addUser(newUser);

        UserDto newUser2 = UserDto.builder()
                .name("user2")
                .email("user2@user.com")
                .build();
        userDto2 = userService.addUser(newUser2);
    }

    @Test
    void addUser() {
        assertThat(userService.getAllUsers(), hasSize(2));
    }

    @Test
    void addUser_whenUserHasDuplicateEmail_thenReturnDataIntegrityViolationException() {
        UserDto newUser = UserDto.builder()
                .name("user3")
                .email("user1@user.com")
                .build();

        assertThrows(DataIntegrityViolationException.class,
                () -> userService.addUser(newUser));
    }

    @Test
    void addUser_whenUserHasNoEmail_thenReturnException() {
        UserDto newUser = UserDto.builder()
                .name("user2").build();

        assertThrows(DataIntegrityViolationException.class,
                () -> userService.addUser(newUser));
    }

    @Test
    void updateUser() {
        UserDto newUser = UserDto.builder()
                .name("updatedName")
                .email("updatedUser@user.com")
                .build();
        UserDto updatedUser = userService.updateUser(userDto.getId(), newUser);

        assertThat(updatedUser.getName(), is("updatedName"));
        assertThat(updatedUser.getEmail(), is("updatedUser@user.com"));
    }

    @Test
    void updateUser_whenUserDoesNotExist_thenReturnException() {
        UserDto newUser = UserDto.builder()
                .name("updatedName")
                .email("updatedUser@user.com")
                .build();

        assertThrows(EntityNotFoundException.class,
                () -> userService.updateUser(0L, newUser));
    }

    @Test
    void getUserById() {
        assertThat(userDto.getName(), equalTo(userService.getUserById(1L).getName()));
        assertThat(userDto.getEmail(), equalTo(userService.getUserById(1L).getEmail()));
    }

    @Test
    void getAllUsers() {
        assertThat(userService.getAllUsers(), hasSize(2));
    }

    @Test
    void deleteUserById() {
        userService.deleteUserById(1L);
        assertThat(userService.getAllUsers(), hasSize(1));
        assertThat(userService.getAllUsers().get(0).getId(), is(userDto2.getId()));
    }
}