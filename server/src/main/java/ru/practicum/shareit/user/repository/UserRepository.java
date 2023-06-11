package ru.practicum.shareit.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Transactional
    @Modifying
    @Query("update User u set u.name = ?1 where u.id = ?2")
    void updateUserName(String name, Long userId);

    @Transactional
    @Modifying
    @Query("update User u set u.email = ?1 where u.id = ?2")
    void updateUserEmail(String email, Long userId);
}
