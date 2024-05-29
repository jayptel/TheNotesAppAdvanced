package com.rhythm.thenotesapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.rhythm.thenotesapp.model.Note
import com.rhythm.thenotesapp.repository.NoteRepository
import kotlinx.coroutines.launch

class NoteViewModel(app: Application, private val noteRepository: NoteRepository) : AndroidViewModel(app) {

    //val allNotes: LiveData<List<Note>> = noteRepository.getAllNotes()
    val allNotes: LiveData<List<Note>> = noteRepository.getAllNotes().also {
        it.observeForever { notes ->
            Log.d("NoteViewModel", "Executed All notes updated: ${notes.size}")
        }
    }
    /*fun addNote(note: Note) = viewModelScope.launch {
        noteRepository.insertNote(note)
    }*/
    fun addNote(note: Note) = viewModelScope.launch {
        Log.d("NoteViewModel", "Executed Adding note: $note")
        noteRepository.insertNote(note)
    }
    /*fun deleteNote(note: Note) = viewModelScope.launch {
        noteRepository.deleteNote(note)
    }*/
    fun deleteNote(note: Note) = viewModelScope.launch {
        Log.d("NoteViewModel", "Executed Deleting note: $note")
        noteRepository.deleteNote(note)
    }
    /*fun updateNote(note: Note) = viewModelScope.launch {
        noteRepository.updateNote(note)
    }*/
    fun updateNote(note: Note) = viewModelScope.launch {
        Log.d("NoteViewModel", "Executed Updating note: $note")
        noteRepository.updateNote(note)
    }
    /*fun deleteAllNotes() = viewModelScope.launch {
        noteRepository.deleteAllNotes()
    }*/
    fun deleteAllNotes() = viewModelScope.launch {
        Log.d("NoteViewModel", "Executed Deleting all notes")
        noteRepository.deleteAllNotes()
    }
    /*fun insertNotes(notes: List<Note>) = viewModelScope.launch {
        noteRepository.insertNotes(notes)
    }*/
    fun insertNotes(notes: List<Note>) = viewModelScope.launch {
        Log.d("NoteViewModel", "Inserting notes: ${notes.size}")
        noteRepository.insertNotes(notes)
    }
    /*fun searchNote(titlePattern: String, descPattern: String): LiveData<List<Note>> {
        return noteRepository.searchNote(titlePattern, descPattern)
    }*/
    fun searchNote(titlePattern: String, descPattern: String): LiveData<List<Note>> {
        Log.d("NoteViewModel", "Executed Searching notes with title pattern: $titlePattern, desc pattern: $descPattern")
        return noteRepository.searchNote(titlePattern, descPattern)
    }
}
