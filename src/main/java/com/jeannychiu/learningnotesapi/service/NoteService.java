package com.jeannychiu.learningnotesapi.service;

import com.jeannychiu.learningnotesapi.exception.NoteNotFoundException;
import com.jeannychiu.learningnotesapi.model.Note;
import com.jeannychiu.learningnotesapi.repository.NoteRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.StreamSupport;

@Service
public class NoteService {
    private final NoteRepository noteRepository;

    @Autowired
    public NoteService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    public Note createNote(Note note) {
        return noteRepository.save(note);
    }

    public Note readNoteById(Long id) {
        Note note = noteRepository.findById(id).orElse(null);
        if (note == null) {
            throw new NoteNotFoundException("找不到 ID 為 " + id + " 的筆記");
        }
        return note;
    }

    public Page<Note> getAllNotes(Pageable pageable) {
        return noteRepository.findAll(pageable);
    }

    public Note updateNote(Long id, Note updatedNote) {
        // 先找出資料
        Note existingNote = noteRepository.findById(id).orElse(null);

        // 判斷是否存在並更新欄位
        if (existingNote != null) {
            existingNote.setTitle(updatedNote.getTitle());
            existingNote.setContent(updatedNote.getContent());
            existingNote.setCreatedAt(updatedNote.getCreatedAt());

            // 存回資料庫
            return noteRepository.save(existingNote);
        } else {
            throw new NoteNotFoundException("找不到 ID 為 " + id + " 的筆記");
        }
    }

    public boolean deleteNote(Long id) {
        if (noteRepository.existsById(id)) {
            noteRepository.deleteById(id);
            return true;
        } else {
            throw new NoteNotFoundException("找不到 ID 為 " + id + " 的筆記");
        }
    }
}
