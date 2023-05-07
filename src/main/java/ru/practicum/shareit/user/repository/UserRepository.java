package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User addUser(User user);

    User updateUser(Long userId, User user);

    Optional<User> getUserById(Long userId);

    List<User> getAllUsers();

    void deleteUserById(Long userId);
}
