package com.rhythm.thenotesapp.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.rhythm.thenotesapp.databinding.NoteLayoutBinding
import com.rhythm.thenotesapp.fragment.HomeFragmentDirections
import com.rhythm.thenotesapp.model.Note
class NoteAdapter(private val onLongPressListener: (Note) -> Unit) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {
//class NoteAdapter : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {
    // class NoteAdapter(private val onLongPressListener: (Note) -> Unit) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {
    class NoteViewHolder(val itemBinding: NoteLayoutBinding): RecyclerView.ViewHolder(itemBinding.root)

    private val differCallback = object : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = NoteLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun getItemCount(): Int = differ.currentList.size

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val currentNote = differ.currentList[position]
        Log.d("NoteAdapter", "Executed Binding note at position $position: $currentNote")
        holder.bind(currentNote)
    }
    fun deleteNoteAtPosition(position: Int) {
        val currentList = differ.currentList.toMutableList()
        if (position >= 0 && position < currentList.size) {
            currentList.removeAt(position)
            differ.submitList(currentList)
        }
    }
    private fun NoteViewHolder.bind(note: Note) {
        itemBinding.noteTitle.text = note.noteTitle
        itemBinding.noteDesc.text = note.noteDesc

        itemView.setOnClickListener {
            val direction = HomeFragmentDirections.actionHomeFragmentToEditNoteFragment(note)
            it.findNavController().navigate(direction)
        }
//  if enable for homerecycle long press  click to  delete note
        itemView.setOnLongClickListener {
            Log.d("NoteAdapter", "Executed setOnLongClickListener on NoteAdapter NoteViewHolder for delete")
            onLongPressListener(note)
            true
        }
    }
}
