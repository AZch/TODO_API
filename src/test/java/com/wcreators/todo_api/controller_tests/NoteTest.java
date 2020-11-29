package com.wcreators.todo_api.controller_tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wcreators.todo_api.todo.controllers.note.CollectionAssembler;
import com.wcreators.todo_api.todo.controllers.note.ModelAssembler;
import com.wcreators.todo_api.todo.controllers.note.NoteController;
import com.wcreators.todo_api.todo.controllers.note.dto.NoteDto;
import com.wcreators.todo_api.todo.entities.Note;
import com.wcreators.todo_api.todo.repositories.NoteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NoteController.class)
@Import({ ModelAssembler.class, CollectionAssembler.class })
public class NoteTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NoteRepository repository;

    @Test
    public void shouldFindNoNotesIfRepositoryIsEmpty() throws Exception {
        when(repository.findAll()).thenReturn(new ArrayList<>());

        mockMvc
                .perform(get("/notes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist());
    }

    @Test
    public void shouldReturnNote() throws Exception {
        Note note = Note.builder()
                .id(1L)
                .title("title")
                .content("content")
                .build();
        when(repository.findById(1L)).thenReturn(Optional.of(note));

        mockMvc
                .perform(get("/notes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(note.getId()))
                .andExpect(jsonPath("$.title").value(note.getTitle()))
                .andExpect(jsonPath("$.content").value(note.getContent()));
    }

    @Test
    public void shouldReturnTwoNote() throws Exception {
        List<Note> noteList = Arrays.asList(
                Note.builder().id(1L).title("1T").content("1C").build(),
                Note.builder().id(2L).title("2T").content("2C").build()
        );
        when(repository.findAll()).thenReturn(noteList);

        mockMvc
                .perform(get("/notes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.notes[*]").isArray())
                .andExpect(jsonPath("$._embedded.notes[*]", hasSize(2)))
                .andExpect(jsonPath("$._embedded.notes[0].id").value(noteList.get(0).getId()))
                .andExpect(jsonPath("$._embedded.notes[0].title").value(noteList.get(0).getTitle()))
                .andExpect(jsonPath("$._embedded.notes[0].content").value(noteList.get(0).getContent()))
                .andExpect(jsonPath("$._embedded.notes[1].id").value(noteList.get(1).getId()))
                .andExpect(jsonPath("$._embedded.notes[1].title").value(noteList.get(1).getTitle()))
                .andExpect(jsonPath("$._embedded.notes[1].content").value(noteList.get(1).getContent()));
    }

    @Test
    public void shouldCreateNote() throws Exception {
        NoteDto actualNote = NoteDto.builder()
                .title("title")
                .content("content")
                .build();

        Note expectedNote = Note.builder()
                .id(1L)
                .title(actualNote.getTitle())
                .content(actualNote.getContent())
                .build();

        when(repository.save(
                Note.builder()
                        .title(actualNote.getTitle())
                        .content(actualNote.getContent())
                        .build())
        ).thenReturn(expectedNote);

        mockMvc
                .perform(
                        post("/notes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(new ObjectMapper().writeValueAsString(actualNote))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(expectedNote.getId()))
                .andExpect(jsonPath("$.title").value(expectedNote.getTitle()))
                .andExpect(jsonPath("$.content").value(expectedNote.getContent()));
    }

    @Test
    public void shouldEditNote() throws Exception {
        Note note = Note.builder()
                .id(1L)
                .title("title")
                .content("content")
                .build();
        NoteDto noteDto = NoteDto.builder()
                .title("new title")
                .content(note.getContent())
                .build();

        Note expectedNote = Note.builder()
                .id(note.getId())
                .title(noteDto.getTitle())
                .content(note.getContent())
                .build();

        when(repository.findById(note.getId())).thenReturn(Optional.of(note));
        when(repository.save(note.withTitle("new title"))).thenReturn(expectedNote);

        mockMvc
                .perform(
                        put("/notes/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(noteDto))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(expectedNote.getId()))
                .andExpect(jsonPath("$.title").value(expectedNote.getTitle()))
                .andExpect(jsonPath("$.content").value(expectedNote.getContent()));
    }

    @Test
    public void shouldDeleteNote() throws Exception {
        Note note = Note.builder().id(1L).title("").content("").build();

        doNothing().when(repository).deleteById(note.getId());

        mockMvc
                .perform(delete("/notes/1"))
                .andExpect(status().isNoContent());
    }
}

