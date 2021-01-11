package com.wcreators.todo_api.todo.controllers.note;

import com.wcreators.todo_api.common.servicies.user_from_auth.UserFromAuth;
import com.wcreators.todo_api.constants.Routes;
import com.wcreators.todo_api.exceptions.EntityNotFoundException;
import com.wcreators.todo_api.todo.controllers.note.dto.NoteDto;
import com.wcreators.todo_api.todo.entities.Note;
import com.wcreators.todo_api.todo.repositories.NoteRepository;
import com.wcreators.todo_api.user.controllers.auth.AuthController;
import com.wcreators.todo_api.user.entities.User;
import com.wcreators.todo_api.user.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(Routes.Notes.BASE)
@RequiredArgsConstructor
public class NoteController {

    private final NoteRepository repository;
    private final UserRepository userRepository;

    private final ModelAssembler assembler;
    private final CollectionAssembler collectionAssembler;
    private final UserFromAuth userFromAuth;

    @GetMapping
    public CollectionModel<EntityModel<Note>> all() {
        String username = userFromAuth.getAuthentication().getName();
        System.out.println(username);
        List<EntityModel<Note>> notes = repository
                .findByUsersUsernameAndDeletedFalse(username)
                .stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());
        return collectionAssembler.toModel(notes);
    }

    @GetMapping(Routes.Notes.GET_ONE)
    public EntityModel<Note> one(@PathVariable Long id) {
        String username = userFromAuth.getAuthentication().getName();
        Note note = repository
                .findByIdAndUsersUsernameAndDeletedFalse(id, username)
                .orElseThrow(() -> noteNotFoundById(id));
        return assembler.toModel(note);
    }

    @PostMapping
    public ResponseEntity<EntityModel<Note>> create(@RequestBody NoteDto noteDto) {
        String username = userFromAuth.getAuthentication().getName();
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> AuthController.userNotFoundByUsername(username));
        Note note = repository.save(
                Note.builder()
                        .title(noteDto.getTitle())
                        .content(noteDto.getContent())
                        .build()
        );
        user.getNotes().add(note);
        userRepository.save(user);

        EntityModel<Note> entityModel = assembler.toModel(note);

        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    @PutMapping(Routes.Notes.EDIT)
    public ResponseEntity<EntityModel<Note>> editNote(@RequestBody NoteDto noteDto, @PathVariable Long id) {
        String username = userFromAuth.getAuthentication().getName();
        Note updatedNote = repository
                .findByIdAndUsersUsernameAndDeletedFalse(id, username)
                .map(note -> {
                    note.setTitle(noteDto.getTitle());
                    note.setContent(noteDto.getContent());
                    return repository.save(note);
                }).orElseThrow(() -> noteNotFoundById(id));

        EntityModel<Note> entityModel = assembler.toModel(updatedNote);

        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    @DeleteMapping(Routes.Notes.DELETE)
    public ResponseEntity<?> deleteNote(@PathVariable Long id) {
        String username = userFromAuth.getAuthentication().getName();
        repository
                .findByIdAndUsersUsernameAndDeletedFalse(id, username)
                .map(note -> {
                    note.setDeleted(true);
                    return repository.save(note);
                }).orElseThrow(() -> noteNotFoundById(id));
        return ResponseEntity.noContent().build();
    }

    public static RuntimeException noteNotFoundById(Long id) {
        return new EntityNotFoundException("note", "id", id);
    }

}
