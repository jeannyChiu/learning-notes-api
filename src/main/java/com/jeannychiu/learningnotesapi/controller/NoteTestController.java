package com.jeannychiu.learningnotesapi.controller;

import com.jeannychiu.learningnotesapi.model.Note;
import com.jeannychiu.learningnotesapi.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NoteTestController {
    private final NoteRepository noteRepository;

    @Autowired
    public NoteTestController(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    @GetMapping("/test-notes")
    public Iterable<Note> testNotes() {
        return noteRepository.findAll();
    }


}
