package com.rhythm.thenotesapp.fragment

import android.app.AlertDialog
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.rhythm.thenotesapp.R
import com.rhythm.thenotesapp.adapter.NoteAdapter
import com.rhythm.thenotesapp.databinding.FragmentEditNoteBinding
import com.rhythm.thenotesapp.model.Note
import com.rhythm.thenotesapp.viewmodel.NoteViewModel

class EditNoteFragment : Fragment(R.layout.fragment_edit_note), MenuProvider {

    private var _binding: FragmentEditNoteBinding? = null
    private val binding get() = _binding!!
    private lateinit var noteViewModel: NoteViewModel
    private val args: EditNoteFragmentArgs by navArgs()
    private lateinit var currentNote: Note
    //private lateinit var noteAdapter: NoteAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenu()
        initializeViewModel()
        getCurrentNote()
        Log.d("EditNoteFragment", "Executed getCurrentNote on onViewCreated()")
        bindDataToViews()
        Log.d("EditNoteFragment", "Executed bindDataToViews on onViewCreated()")
        adjustFabForKeyboard(binding.editNoteFab)
        Log.d("EditNoteFragment", "Executed adjustFabForKeyboard on onViewCreated()")
        //noteAdapter = NoteAdapter()

    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun initializeViewModel() {
        noteViewModel = ViewModelProvider(requireActivity()).get(NoteViewModel::class.java)
    }

    private fun getCurrentNote() {
        currentNote = args.note!!
        Log.d("EditNoteFragment", "Current Note: $currentNote")
    }

    private fun bindDataToViews() {
        binding.apply {
            editNoteTitle.setText(currentNote.noteTitle)
            editNoteDesc.setText(currentNote.noteDesc)
            editNoteFab.setOnClickListener { updateNote() }
        }
    }

    private fun updateNote() {
        val noteTitle = binding.editNoteTitle.text.toString().trim()
        val noteDesc = binding.editNoteDesc.text.toString().trim()

        if (noteTitle.isNotEmpty()) {
            val updatedNote = currentNote.copy(noteTitle = noteTitle, noteDesc = noteDesc)
            noteViewModel.updateNote(updatedNote)
            Log.d("EditNoteFragment", "Updated Note: $updatedNote")
            Toast.makeText(context, "Edited Note Saved", Toast.LENGTH_SHORT).show()
            navigateToHomeFragment()
            Log.d("EditNoteFragment", "Executed navigateToHomeFragment on updateNote editnote to home")
        } else {
            Toast.makeText(context, "Please enter Note Title", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteNote() {
        AlertDialog.Builder(requireContext()).apply {
            setTitle("Delete Note")
            setMessage("Do you want to delete this note?")
            setPositiveButton("Delete") { _, _ ->
                Log.d("EditNoteFragment", "Deleting note: $currentNote")
                // Delete the note from the ViewModel
                noteViewModel.deleteNote(currentNote)
                Log.d("EditNoteFragment", "Deleted Note: $currentNote")
                // Notify the adapter that the item has been removed
                //val position = noteAdapter.differ.currentList.indexOf(currentNote)
                //noteAdapter.deleteNoteAtPosition(position)
                Toast.makeText(context, "Note Deleted", Toast.LENGTH_SHORT).show()
                Log.d("EditNoteFragment", "Note Deleted Successfully: $currentNote")
                navigateToHomeFragment()
                Log.d("EditNoteFragment", "Executed navigateToHomeFragment on deleteNote editnote to home")
                //findNavController().navigate(R.id.action_editNoteFragment_to_homeFragment)
            }
            setNegativeButton("Cancel", null)
        }.create().show()
    }

    private fun navigateToHomeFragment() {
        findNavController().popBackStack(R.id.homeFragment, false)
        // Assuming HomeFragment is the immediate parent fragment
        (parentFragment as? HomeFragment)?.refreshRecyclerView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun adjustFabForKeyboard(fab: FloatingActionButton) {
        val rootView = binding.root
        rootView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val rect = Rect()
                rootView.getWindowVisibleDisplayFrame(rect)
                val screenHeight = rootView.rootView.height
                val keypadHeight = screenHeight - rect.bottom

                val translationY = if (keypadHeight > screenHeight * 0.15) {
                    -keypadHeight.toFloat() + 120f // Adjust the 120f to tweak the spacing
                } else {
                    0f
                }
                fab.translationY = translationY
            }
        })
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.menu_edit_note, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.deleteMenu -> {
                deleteNote()
                true
            }
            else -> false
        }
    }
}
