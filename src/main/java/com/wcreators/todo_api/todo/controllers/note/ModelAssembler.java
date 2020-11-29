package com.wcreators.todo_api.todo.controllers.note;

import com.wcreators.todo_api.todo.entities.Note;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ModelAssembler implements RepresentationModelAssembler<Note, EntityModel<Note>> {

    @Override
    public EntityModel<Note> toModel(Note entity) {
        return EntityModel.of(
                entity,
                linkTo(methodOn(NoteController.class).one(entity.getId())).withSelfRel(),
                linkTo(methodOn(NoteController.class).all()).withRel("notes")
        );
    }
}
