package ru.practicum.shareit.user.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.EntityAlreadyExistsException;
import ru.practicum.shareit.exceptions.EntityNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserServiceImpl implements UserService {
    final UserRepository userRepository;

    @Transactional
    @Override
    public UserDto addUser(UserDto userDto) {
        if (checkEmail(userDto)) {
            throw new EntityAlreadyExistsException("User with email " + userDto.getEmail() + " already exists");
        }
        User user = userRepository.save(UserMapper.fromDtoToUser(userDto));
        log.info("New user added : {}", user);
        return UserMapper.toUserDto(user);
    }

    @Transactional
    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        User user = getUserIfItExists(userId);
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
        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Override
    public UserDto getUserById(Long userId) {
        return UserMapper.toUserDto(getUserIfItExists(userId));
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream().map(UserMapper::toUserDto).collect(Collectors.toList());
    }

    @Override
    public void deleteUserById(Long userId) {
        getUserIfItExists(userId);
        userRepository.deleteById(userId);
    }

    private User getUserIfItExists(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("No user with id " + userId));
    }

    private boolean checkEmail(UserDto userDto) {
        return userRepository.findAll().stream().anyMatch(user -> user.getEmail().equals(userDto.getEmail()));
    }
}
