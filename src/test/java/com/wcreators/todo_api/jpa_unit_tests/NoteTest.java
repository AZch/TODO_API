package com.wcreators.todo_api.jpa_unit_tests;

import com.wcreators.todo_api.todo.entities.Note;
import com.wcreators.todo_api.todo.repositories.NoteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class NoteTest {

    @Autowired
    private NoteRepository repository;

    @Test
    public void shouldFindNoNotesIfRepositoryIsEmpty() {
        Iterable<Note> actualNotes = repository.findAllByDeletedFalse();
        assertThat(actualNotes).isEmpty();
    }

    @Test
    public void shouldCreateNote() {
        Note note = Note.builder()
                .title("expected title")
                .content("expected content")
                .build();

        Note expected = repository.save(note);
        Note actual = repository.findByIdAndDeletedFalse(expected.getId()).orElseThrow();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldFindTwoNotes() {
        repository.saveAll(Arrays.asList(
                Note.builder().title("1").content("1").build(),
                Note.builder().title("2").content("2").build()
        ));

        Iterable<Note> actualNotes = repository.findAllByDeletedFalse();
        assertThat(actualNotes).hasSize(2);
    }

    @Test
    public void shouldEditNote() {
        Note baseNote = repository.save(
                Note.builder()
                    .title("title")
                    .content("content")
                    .build()
        );
        repository.save(baseNote.withTitle("new title"));

        Note expectedNote = Note.builder()
                .title("new title")
                .content("content")
                .build();
        Note actualNote = repository.findByIdAndDeletedFalse(baseNote.getId()).orElseThrow();
        assertThat(actualNote).isEqualToIgnoringGivenFields(expectedNote, "id");
    }

    @Test
    public void shouldDeleteNote() {
        Note note = repository.save(Note.builder().title("").content("").deleted(true).build());

        Optional<Note> actualThisNote = repository.findById(note.getId());
        assertThat(actualThisNote).isEqualTo(Optional.of(note));

        Optional<Note> actualNonIdNote = repository.findByIdAndDeletedFalse(note.getId());
        assertThat(actualNonIdNote).isEmpty();
    }
}
