package com.wcreators.todo_api.todo.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wcreators.todo_api.user.entities.User;
import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@With
@Entity
@Table(name = "note")
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String content;

    private boolean deleted = false;

    @JsonIgnore
    @ManyToMany(mappedBy = "notes", fetch = FetchType.LAZY)
    private Set<User> users = new HashSet<>();
}
