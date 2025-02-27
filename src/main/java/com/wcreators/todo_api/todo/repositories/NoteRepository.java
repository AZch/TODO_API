package com.wcreators.todo_api.todo.repositories;

import com.wcreators.todo_api.todo.entities.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByUsersUsernameAndDeletedFalse(String username);

    List<Note> findAllByDeletedFalse();

    Optional<Note> findByIdAndUsersUsernameAndDeletedFalse(Long id, String username);

    Optional<Note> findByIdAndDeletedFalse(Long id);
}
