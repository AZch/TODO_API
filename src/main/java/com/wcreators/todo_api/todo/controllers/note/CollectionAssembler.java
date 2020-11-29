package com.wcreators.todo_api.todo.controllers.note;

import com.wcreators.todo_api.todo.entities.Note;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
@RequiredArgsConstructor
public class CollectionAssembler implements RepresentationModelAssembler<List<EntityModel<Note>>, CollectionModel<EntityModel<Note>>> {

    @Override
    public CollectionModel<EntityModel<Note>> toModel(List<EntityModel<Note>> entity) {
        return CollectionModel.of(
                entity,
                linkTo(methodOn(NoteController.class).all()).withSelfRel()
        );
    }
}
