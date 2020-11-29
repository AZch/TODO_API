package com.wcreators.todo_api.todo.repositories;

import com.wcreators.todo_api.todo.entities.Note;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepository extends JpaRepository<Note, Long> {}
