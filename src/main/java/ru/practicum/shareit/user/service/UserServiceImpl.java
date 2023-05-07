package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.EntityAlreadyExistsException;
import ru.practicum.shareit.exceptions.EntityNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto addUser(UserDto userDto) {
        if (userDto.getEmail() == null) {
            throw new ValidationException("Email is empty");
        }
        if (checkEmail(userDto)) {
            throw new EntityAlreadyExistsException("User with email " + userDto.getEmail() + " already exists");
        }
        User user = userRepository.addUser(UserMapper.fromDtoToUser(userDto));
        log.info("New user added : {}", user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        User user = checkUserExists(userId);
        if (userDto.getName() != null) {
                user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            if (checkEmail(userDto)) {
                if (!user.getEmail().equals(userDto.getEmail())) {
                    throw new EntityAlreadyExistsException("User with email " + user.getEmail() + " already exists");
                }
            }
            user.setEmail(userDto.getEmail());
        }
        log.info("User updated : {}", user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto getUserById(Long userId) {
        return UserMapper.toUserDto(checkUserExists(userId));
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.getAllUsers().stream().map(UserMapper::toUserDto).collect(Collectors.toList());
    }

    @Override
    public void deleteUserById(Long userId) {
        checkUserExists(userId);
        userRepository.deleteUserById(userId);
    }

    private User checkUserExists(Long userId) {
        return userRepository.getUserById(userId)
                .orElseThrow(() -> new EntityNotFoundException("No user with id " + userId));
    }

    private boolean checkEmail(UserDto userDto) {
        return userRepository.getAllUsers().stream().anyMatch(user -> user.getEmail().equals(userDto.getEmail()));
    }

}
