package com.rhythm.thenotesapp.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.rhythm.thenotesapp.database.NoteDatabase
import com.rhythm.thenotesapp.model.Note

class NoteRepository(private val db: NoteDatabase) {

    //suspend fun insertNote(note: Note) = db.getNoteDao().insertNote(note)
    suspend fun insertNote(note: Note) {
        Log.d("NoteRepository", "Executed Inserting note: $note")
        db.getNoteDao().insertNote(note)
    }

    //suspend fun insertNotes(notes: List<Note>) = db.getNoteDao().insertNotes(notes)
    suspend fun insertNotes(notes: List<Note>) {
        Log.d("NoteRepository", "Executed Inserting notes: ${notes.size}")
        db.getNoteDao().insertNotes(notes)
    }
    //suspend fun deleteNote(note: Note) = db.getNoteDao().deleteNote(note)
    suspend fun deleteNote(note: Note) {
        Log.d("NoteRepository", "Executed Deleting note: $note")
        db.getNoteDao().deleteNote(note)
    }
    //suspend fun deleteAllNotes() = db.getNoteDao().deleteAllNotes()
    suspend fun deleteAllNotes() {
        Log.d("NoteRepository", "Executed Deleting all notes")
        db.getNoteDao().deleteAllNotes()
    }
    //suspend fun updateNote(note: Note) = db.getNoteDao().updateNote(note)
    suspend fun updateNote(note: Note) {
        Log.d("NoteRepository", "Executed Updating note: $note")
        db.getNoteDao().updateNote(note)
    }
    //fun getAllNotes(): LiveData<List<Note>> = db.getNoteDao().getAllNotes()
    fun getAllNotes(): LiveData<List<Note>> {
        Log.d("NoteRepository", "Executed Fetching all notes")
        return db.getNoteDao().getAllNotes()
    }
    /*fun searchNote(titlePattern: String, descPattern: String): LiveData<List<Note>> =
        db.getNoteDao().searchNote(titlePattern, descPattern)*/
    fun searchNote(titlePattern: String, descPattern: String): LiveData<List<Note>> {
        Log.d("NoteRepository", "Executed Searching notes with title pattern: $titlePattern, desc pattern: $descPattern")
        return db.getNoteDao().searchNote(titlePattern, descPattern)
    }
}
