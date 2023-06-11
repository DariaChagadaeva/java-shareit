package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("select new ru.practicum.shareit.item.dto.CommentDto" +
            "(c.id, c.text, c.author.name, c.created) " +
            "from Comment as c " +
            "where c.item.id = ?1")
    List<CommentDto> findAllByItem(Long itemId);
}
