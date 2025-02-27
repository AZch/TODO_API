package com.wcreators.todo_api.controller_tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wcreators.todo_api.configs.security.details.CustomUserDetailsService;
import com.wcreators.todo_api.configs.security.jwt.JwtFilter;
import com.wcreators.todo_api.configs.security.jwt.JwtProvider;
import com.wcreators.todo_api.constants.Roles;
import com.wcreators.todo_api.constants.Routes;
import com.wcreators.todo_api.todo.controllers.note.CollectionAssembler;
import com.wcreators.todo_api.todo.controllers.note.ModelAssembler;
import com.wcreators.todo_api.todo.controllers.note.NoteController;
import com.wcreators.todo_api.todo.controllers.note.dto.NoteDto;
import com.wcreators.todo_api.todo.entities.Note;
import com.wcreators.todo_api.todo.repositories.NoteRepository;
import com.wcreators.todo_api.user.entities.Role;
import com.wcreators.todo_api.user.entities.User;
import com.wcreators.todo_api.user.repositories.RoleRepository;
import com.wcreators.todo_api.user.repositories.UserRepository;
import com.wcreators.todo_api.user.services.UserService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.wcreators.todo_api.configs.security.jwt.JwtFilter.AUTHORIZATION;
import static com.wcreators.todo_api.configs.security.jwt.JwtFilter.TOKEN_START_WITH;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NoteController.class)
@Import({
        ModelAssembler.class,
        CollectionAssembler.class,
        JwtFilter.class,
        JwtProvider.class,
        CustomUserDetailsService.class,
        UserService.class
})
public class NoteTest {

    private String TOKEN;
    private final String USERNAME = "test";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtProvider jwtProvider;

    @MockBean
    private NoteRepository noteRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RoleRepository roleRepository;

    @PostConstruct
    public void init() {
        TOKEN = jwtProvider.generateToken(USERNAME);
    }

    @Nested
    class GetAll {
        @Test
        public void shouldFindNoNotesIfRepositoryIsEmpty() throws Exception {
            prepareUserToTesting();
            when(noteRepository.findAllByDeletedFalse()).thenReturn(new ArrayList<>());

            mockMvc
                    .perform(get(Routes.Notes.BASE).header(AUTHORIZATION, String.format("%s%s", TOKEN_START_WITH, TOKEN)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$._embedded").doesNotExist());
        }

        @Test
        public void shouldReturnTwoNote() throws Exception {
            prepareUserToTesting();
            List<Note> noteList = Arrays.asList(
                    Note.builder().id(1L).title("1T").content("1C").build(),
                    Note.builder().id(2L).title("2T").content("2C").build()
            );
            when(noteRepository.findAllByDeletedFalse()).thenReturn(noteList);

            mockMvc
                    .perform(get(Routes.Notes.BASE).header(AUTHORIZATION, String.format("%s%s", TOKEN_START_WITH, TOKEN)))
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
    }

    @Nested
    class GetOne {
        @Test
        public void shouldReturnNote() throws Exception {
            prepareUserToTesting();
            Note note = Note.builder()
                    .id(1L)
                    .title("title")
                    .content("content")
                    .build();
            when(noteRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(note));

            mockMvc
                    .perform(get(String.format("%s/1", Routes.Notes.BASE)).header(AUTHORIZATION, String.format("%s%s", TOKEN_START_WITH, TOKEN)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(note.getId()))
                    .andExpect(jsonPath("$.title").value(note.getTitle()))
                    .andExpect(jsonPath("$.content").value(note.getContent()));
        }
    }

    @Nested
    class Create {
        @Test
        public void shouldCreateNote() throws Exception {
            prepareUserToTesting();
            NoteDto actualNote = NoteDto.builder()
                    .title("title")
                    .content("content")
                    .build();

            Note expectedNote = Note.builder()
                    .id(1L)
                    .title(actualNote.getTitle())
                    .content(actualNote.getContent())
                    .build();

            when(noteRepository.save(
                    Note.builder()
                            .title(actualNote.getTitle())
                            .content(actualNote.getContent())
                            .build())
            ).thenReturn(expectedNote);

            mockMvc
                    .perform(
                            post(Routes.Notes.BASE)
                                    .header(AUTHORIZATION, String.format("%s%s", TOKEN_START_WITH, TOKEN))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(new ObjectMapper().writeValueAsString(actualNote))
                    )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(expectedNote.getId()))
                    .andExpect(jsonPath("$.title").value(expectedNote.getTitle()))
                    .andExpect(jsonPath("$.content").value(expectedNote.getContent()));
        }
    }

    @Nested
    class Edit {
        @Test
        public void shouldEditNote() throws Exception {
            prepareUserToTesting();
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

            when(noteRepository.findByIdAndDeletedFalse(note.getId())).thenReturn(Optional.of(note));
            when(noteRepository.save(note.withTitle("new title"))).thenReturn(expectedNote);

            mockMvc
                    .perform(
                            put(String.format("%s/1", Routes.Notes.BASE))
                                    .header(AUTHORIZATION, String.format("%s%s", TOKEN_START_WITH, TOKEN))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(new ObjectMapper().writeValueAsString(noteDto))
                    )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(expectedNote.getId()))
                    .andExpect(jsonPath("$.title").value(expectedNote.getTitle()))
                    .andExpect(jsonPath("$.content").value(expectedNote.getContent()));
        }
    }

    @Nested
    class Delete {
        @Test
        public void shouldDeleteNote() throws Exception {
            prepareUserToTesting();
            Note note = Note.builder().id(1L).title("").content("").build();

            when(noteRepository.findByIdAndDeletedFalse(note.getId())).thenReturn(Optional.of(note));
            Note deletedNote = note.withDeleted(true);
            when(noteRepository.save(deletedNote)).thenReturn(deletedNote);

            mockMvc
                    .perform(delete(String.format("%s/1", Routes.Notes.BASE)).header(AUTHORIZATION, String.format("%s%s", TOKEN_START_WITH, TOKEN)))
                    .andExpect(status().isNoContent());
        }
    }

    private void prepareUserToTesting() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(
                Optional.of(
                        User.builder()
                                .id(1L)
                                .username(USERNAME)
                                .password("")
                                .role(
                                        Role.builder()
                                                .id(1L)
                                                .name(Roles.USER.getName())
                                                .build()
                                )
                                .build()
                )
        );
    }
}

