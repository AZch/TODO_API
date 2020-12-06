package com.wcreators.todo_api.todo.controllers.note;

import com.wcreators.todo_api.constants.Routes;
import com.wcreators.todo_api.exceptions.EntityNotFoundException;
import com.wcreators.todo_api.todo.controllers.note.dto.NoteDto;
import com.wcreators.todo_api.todo.entities.Note;
import com.wcreators.todo_api.todo.repositories.NoteRepository;
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

    private final ModelAssembler assembler;
    private final CollectionAssembler collectionAssembler;

    @GetMapping
    public CollectionModel<EntityModel<Note>> all() {
        List<EntityModel<Note>> notes = repository
                .findAllByDeletedFalse()
                .stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());
        return collectionAssembler.toModel(notes);
    }

    @GetMapping(Routes.Notes.GET_ONE)
    public EntityModel<Note> one(@PathVariable Long id) {
        Note note = repository
                .findByIdAndDeletedFalse(id)
                .orElseThrow(() -> noteNotFoundById(id));
        return assembler.toModel(note);
    }

    @PostMapping
    public ResponseEntity<EntityModel<Note>> create(@RequestBody NoteDto noteDto) {
        Note note = repository.save(
                Note.builder()
                        .title(noteDto.getTitle())
                        .content(noteDto.getContent())
                        .build()
        );

        EntityModel<Note> entityModel = assembler.toModel(note);

        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    @PutMapping(Routes.Notes.EDIT)
    public ResponseEntity<EntityModel<Note>> editNote(@RequestBody NoteDto noteDto, @PathVariable Long id) {
        Note updatedNote = repository
                .findByIdAndDeletedFalse(id)
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
        repository
                .findByIdAndDeletedFalse(id)
                .map(note -> {
                    note.setDeleted(true);
                    return repository.save(note);
                });
        return ResponseEntity.noContent().build();
    }

    public static RuntimeException noteNotFoundById(Long id) {
        return new EntityNotFoundException("note", "id", id);
    }

}
