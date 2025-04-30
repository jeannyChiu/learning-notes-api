package com.jeannychiu.learningnotesapi.repository;

import com.jeannychiu.learningnotesapi.model.Note;
import org.springframework.data.repository.CrudRepository;

public interface NoteRepository extends CrudRepository<Note, Long> {
}
