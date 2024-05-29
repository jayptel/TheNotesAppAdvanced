package com.rhythm.thenotesapp.fragment

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import com.rhythm.thenotesapp.MainActivity
import com.rhythm.thenotesapp.R
import com.rhythm.thenotesapp.databinding.FragmentAddNoteBinding
import com.rhythm.thenotesapp.model.Note
import com.rhythm.thenotesapp.viewmodel.NoteViewModel

class AddNoteFragment : Fragment(R.layout.fragment_add_note), MenuProvider {

    private var addNoteBinding: FragmentAddNoteBinding? = null
    private val binding get() = addNoteBinding!!
    private lateinit var notesViewModel: NoteViewModel
    private lateinit var addNoteView: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        addNoteBinding = FragmentAddNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenu()
        initializeViewModel()
        addNoteView = view
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun initializeViewModel() {
        notesViewModel = (activity as MainActivity).noteViewModel
    }

    private fun saveNote() {
        val noteTitle = binding.addNoteTitle.text.toString().trim()
        val noteDesc = binding.addNoteDesc.text.toString().trim()

        if (noteTitle.isNotEmpty()) {
            val note = Note(
                id = 0,
                noteTitle = noteTitle,
                noteDesc = noteDesc
            )
            notesViewModel.addNote(note)
            Log.d("AddNoteFragment", "Note saved: $note")
            Toast.makeText(addNoteView.context, "Note Saved", Toast.LENGTH_SHORT).show()

            addNoteView.findNavController().popBackStack(R.id.homeFragment, false)
            Log.d("AddNoteFragment", "Executed popBackStack on findNavController() addnote to home")
        } else {
            Toast.makeText(addNoteView.context, "Please enter note title.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.menu_add_note, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.saveMenu -> {
                saveNote()
                true
            }
            else -> false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        addNoteBinding = null
    }
}
