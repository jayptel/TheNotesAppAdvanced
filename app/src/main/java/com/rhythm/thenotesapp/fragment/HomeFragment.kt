package com.rhythm.thenotesapp.fragment

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.Spreadsheet
import com.google.api.services.sheets.v4.model.SpreadsheetProperties
import com.google.api.services.sheets.v4.model.ValueRange
import com.rhythm.thenotesapp.MainActivity
import com.rhythm.thenotesapp.R
import com.rhythm.thenotesapp.adapter.NoteAdapter
import com.rhythm.thenotesapp.auth.GoogleSignInHelper
import com.rhythm.thenotesapp.databinding.FragmentHomeBinding
import com.rhythm.thenotesapp.model.Note
import com.rhythm.thenotesapp.viewmodel.NoteViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.OutputStream

class HomeFragment : Fragment(R.layout.fragment_home), SearchView.OnQueryTextListener, MenuProvider {

    private var homeBinding: FragmentHomeBinding? = null
    private val binding get() = homeBinding!!
    private lateinit var notesViewModel: NoteViewModel
    private lateinit var noteAdapter: NoteAdapter
    private var searchNotesObserver: Observer<List<Note>>? = null
    private var searchView: SearchView? = null
    private lateinit var googleSignInHelper: GoogleSignInHelper
    companion object {
         const val REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 1002
         const val REQUEST_CODE_SIGN_IN = 1001
    }

    private val createFile = registerForActivityResult(ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) { uri: Uri? ->
        uri?.let { exportNotesToExcel(it) }
    }

    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri -> importNotesFromExcel(uri) }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        homeBinding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenu()
        notesViewModel = (activity as MainActivity).noteViewModel
        setupRecyclerView()
        Log.d("HomeFragment", "Executed setupRecyclerView on onViewCreated()")
        setupFab()
        observeNotes()
        Log.d("HomeFragment", "Executed observeNotes on onViewCreated()")
        googleSignInHelper = GoogleSignInHelper(requireContext())
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupRecyclerView() {
        Log.d("HomeFragment", "function setupRecyclerView()")
        noteAdapter = NoteAdapter(::showDeleteNoteDialog)
       // noteAdapter = NoteAdapter()
        // if enable then you able to long press note delete  in homerecyclerView also  enable in code which Comment NoteAdapter
        //noteAdapter = NoteAdapter(::showDeleteNoteDialog)
        binding.homeRecyclerView.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            setHasFixedSize(true)
            adapter = noteAdapter
        }
    }

    private fun setupFab() {
        binding.addNoteFab.setOnClickListener {
            it.findNavController().navigate(R.id.action_homeFragment_to_addNoteFragment)
        }
    }

    private fun observeNotes() {
        Log.d("HomeFragment", "function Observed()")
        notesViewModel.allNotes.observe(viewLifecycleOwner) { notes ->
           // Log.d("HomeFragment", "function Observed notes: ${notes.size}")
            noteAdapter.differ.submitList(notes) {
                //Log.d("HomeFragment", "Notes submitted to adapter: ${notes.size}")
               // binding.homeRecyclerView.scrollToPosition(0)
                scrollToTop()
                //Log.d("HomeFragment", "RecyclerView scrolled to top")
                // for enable below working fine only issue inside delete then scroll to down
                updateUI(notes)
                //Log.d("HomeFragment", "Notes updated: ${notes.size} notes")
            }
            // for enable below working fine only issue inside delete then scroll to down
            /*updateUI(notes)
            Log.d("HomeFragment", "Notes updated: ${notes.size} notes")*/
        }
        //refreshRecyclerView()  // when enable then not able to go to top when add new note
        //Log.d("HomeFragment", "Executed refreshRecyclerView on observeNotes()")

    }

    private fun updateUI(notes: List<Note>?) {
        Log.d("HomeFragment", "function updateUI()")
        if (notes != null) {
            if (notes.isNotEmpty()) {
                Log.d("HomeFragment", "Notes are not empty, hiding empty state")
                binding.emptyNotesImage.visibility = View.GONE
                binding.homeRecyclerView.visibility = View.VISIBLE
            } else {
                Log.d("HomeFragment", "Notes are empty, showing empty state")
                binding.emptyNotesImage.visibility = View.VISIBLE
                binding.homeRecyclerView.visibility = View.GONE
            }
        } else {
            Log.d("HomeFragment", "Notes are null")
        }
    }

    private fun showDeleteNoteDialog(note: Note) {
        AlertDialog.Builder(requireContext()).apply {
            setTitle("Delete Note")
            setMessage("Do you want to delete this note?")
            setPositiveButton("Delete") { _, _ ->
                notesViewModel.deleteNote(note)
                Toast.makeText(context, "Note Deleted", Toast.LENGTH_SHORT).show()
                refreshRecyclerView() // if disable then delete then bottom location scroll
                //Log.d("HomeFragment", "Executed refreshRecyclerView on showDeleteNoteDialog()")
            }
            setNegativeButton("Cancel", null)
        }.create().show()
    }

    fun refreshRecyclerView() {
        Log.d("HomeFragment", "Executed refreshRecyclerView()")
        // this work delete long press first note then delete properly  but not editnotefragment
        notesViewModel.allNotes.observe(viewLifecycleOwner) { notes ->
            noteAdapter.differ.submitList(notes)

            //binding.homeRecyclerView.scrollToPosition(0)
            scrollToTop()
            //Log.d("HomeFragment", "refreshRecyclerView RecyclerView scrollToTop scrolled to top")
            updateUI(notes)
            //Log.d("HomeFragment", "Executed updateUI on refreshRecyclerView()")

            // when enable below botton delte after scroll down issue
            /*noteAdapter.differ.submitList(notes) {
                Log.d("HomeFragment", "Notes submitted to adapter: ${notes.size}")
                scrollToTop()
                Log.d("HomeFragment", "RecyclerView scrolled to top")
            }
            updateUI(notes)
            Log.d("HomeFragment", "Notes updated: ${notes.size} notes")*/

        }
        // this work delete long press first note then delete properly  but not editnotefragment

        // below does not work for long press delete as well as editnotefragment though delete hide all notes
       /* notesViewModel.allNotes.observe(viewLifecycleOwner) { notes ->
            noteAdapter.differ.submitList(notes) {
                scrollToTop()
                //binding.homeRecyclerView.scrollToPosition(0)
                Log.d("HomeFragment", "RecyclerView scrolled to top")
            }
            updateUI(notes)
            *//*noteAdapter.differ.submitList(notes) {
                Log.d("HomeFragment", "Notes submitted to adapter: ${notes.size}")
                binding.homeRecyclerView.scrollToPosition(0)
                Log.d("HomeFragment", "RecyclerView scrolled to top")
                updateUI(notes)
            }*//*
        }*/
    }
    private fun scrollToTop() {
        /*val layoutManager = binding.homeRecyclerView.layoutManager as StaggeredGridLayoutManager
        layoutManager.scrollToPositionWithOffset(0, 0)*/
       /* Log.d("HomeFragment", "Executed scrollToTop()")
        val layoutManager = binding.homeRecyclerView.layoutManager as StaggeredGridLayoutManager
        val smoothScroller = object : LinearSmoothScroller(binding.homeRecyclerView.context) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }
        }
        smoothScroller.targetPosition = 0
        layoutManager.startSmoothScroll(smoothScroller)*/
        //Log.d("HomeFragment", " scrollToTop to top")
        // Add a delay of 1 second before scrolling to the top
        Handler(Looper.getMainLooper()).postDelayed({
            Log.d("HomeFragment", "Executed scrollToTop()")
            val layoutManager = binding.homeRecyclerView.layoutManager as StaggeredGridLayoutManager
            val smoothScroller = object : LinearSmoothScroller(binding.homeRecyclerView.context) {
                override fun getVerticalSnapPreference(): Int {
                    return SNAP_TO_START
                }
            }
            smoothScroller.targetPosition = 0
            layoutManager.startSmoothScroll(smoothScroller)
            Log.d("HomeFragment", "Executed scrollToTop() with delay")
        }, 1000) // 1 second delay
    }
    private fun exportNotesToGoogleSheets(account: GoogleSignInAccount) {
        val credential = googleSignInHelper.getCredential(account)
        val sheetsService = Sheets.Builder(
            NetHttpTransport(),
            JacksonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName(getString(R.string.app_name))
            .build()

        val notesList = notesViewModel.allNotes.value ?: emptyList()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val spreadsheet = sheetsService.spreadsheets().create(
                    Spreadsheet().setProperties(
                        SpreadsheetProperties().setTitle("Notes_${System.currentTimeMillis()}")
                    )
                ).execute()

                val values = mutableListOf<List<Any>>()
                values.add(listOf("ID", "Title", "Description"))
                notesList.forEach { note ->
                    values.add(listOf(note.id, note.noteTitle, note.noteDesc))
                }

                val body = ValueRange().setValues(values)
                sheetsService.spreadsheets().values().update(
                    spreadsheet.spreadsheetId,
                    "Sheet1",
                    body
                ).setValueInputOption("RAW").execute()

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Notes exported successfully", Toast.LENGTH_LONG).show()
                }
            } catch (e: UserRecoverableAuthIOException) {
                startActivityForResult(e.intent, REQUEST_CODE_SIGN_IN)
                Log.e("HomeFragment", "User recoverable auth error", e)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error exporting notes: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                Log.e("HomeFragment", "Error exporting notes to Google Sheets", e)
            }
        }
    }
    private fun exportNotesToExcel(uri: Uri) {
        val notesList = notesViewModel.allNotes.value ?: emptyList()
        if (notesList.isEmpty()) {
            Toast.makeText(context, "No notes to export", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Notes")

            val headerRow = sheet.createRow(0)
            headerRow.createCell(0).setCellValue("ID")
            headerRow.createCell(1).setCellValue("Title")
            headerRow.createCell(2).setCellValue("Description")

            notesList.forEachIndexed { index, note ->
                val row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue(note.id.toDouble())
                row.createCell(1).setCellValue(note.noteTitle)
                row.createCell(2).setCellValue(note.noteDesc)
            }

            val outputStream: OutputStream? = context?.contentResolver?.openOutputStream(uri)
            workbook.write(outputStream)
            outputStream?.close()
            workbook.close()

            Toast.makeText(context, "Notes exported successfully", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error exporting notes: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun importNotesFromExcel(uri: Uri) {
        try {
            val inputStream = context?.contentResolver?.openInputStream(uri)
            val workbook = XSSFWorkbook(inputStream)
            val sheet = workbook.getSheetAt(0)
            val notesList = mutableListOf<Note>()

            for (rowIndex in 1..sheet.lastRowNum) {
                val row = sheet.getRow(rowIndex)
                val id = row.getCell(0).numericCellValue.toInt()
                val title = row.getCell(1).stringCellValue
                val description = row.getCell(2).stringCellValue

                val note = Note(id, title, description)
                notesList.add(note)
            }
           /* notesViewModel.deleteAllNotes()
            notesViewModel.insertNotes(notesList)
            inputStream?.close()
            workbook.close()*/
            inputStream?.close()
            workbook.close()

            // Update the ViewModel with the new notes list and refresh the UI
            notesViewModel.deleteAllNotes()
            notesViewModel.insertNotes(notesList)
            // Refresh the RecyclerView and scroll to top
            //refreshRecyclerView()
            Toast.makeText(context, "Notes imported successfully", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error importing notes: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun searchNote(query: String?) {
        val searchWords = query?.split(" ")?.joinToString("%", prefix = "%", postfix = "%") ?: "%"
        val titlePattern = searchWords
        val descPattern = searchWords

        if (view != null) {
            searchNotesObserver?.let { notesViewModel.searchNote("", "").removeObserver(it) }
            searchNotesObserver = Observer { list ->
                /*noteAdapter.differ.submitList(list) {
                    binding.homeRecyclerView.scrollToPosition(0)
                    Log.d("HomeFragment", "RecyclerView scrolled to top")
                }
                updateUI(list)
                Log.d("HomeFragment", "Notes updated: ${list.size} notes")*/
                noteAdapter.differ.submitList(list) {
                    Log.d("HomeFragment", "Notes submitted to adapter: ${list.size}")
                    //binding.homeRecyclerView.scrollToPosition(0)
                    scrollToTop()
                    Log.d("HomeFragment", "RecyclerView scrolled to top")
                    updateUI(list)
                }
            }

            notesViewModel.searchNote(titlePattern, descPattern)
                .observe(viewLifecycleOwner, searchNotesObserver!!)
        }
    }
    override fun onQueryTextSubmit(query: String?): Boolean {
       // return false
        searchNote(query)
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText != null) {
            searchNote(newText)
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //searchNotesObserver?.let { notesViewModel.searchNote("", "").removeObserver(it) }
        homeBinding = null
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.home_menu, menu)
       /* val searchItem = menu.findItem(R.id.searchMenu)
        searchView = searchItem.actionView as SearchView
        searchView?.setOnQueryTextListener(this)*/
        val search = menu.findItem(R.id.searchMenu).actionView as SearchView
        search.apply {
            isSubmitButtonEnabled = true
            setOnQueryTextListener(this@HomeFragment)
        }
    }
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            googleSignInHelper.handleSignInResult(data, onSuccess = { account ->
                Log.d("HomeFragment", "Google sign-in successful: ${account.email}")
                exportNotesToGoogleSheets(account)
            }, onFailure = { e ->
                Log.e("HomeFragment", "Google sign-in failed", e)
                Toast.makeText(requireContext(), "Sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            })
        }
    }
    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.exportMenu -> {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE_WRITE_EXTERNAL_STORAGE)
                } else {
                    createFile.launch("Notes_${System.currentTimeMillis()}.xlsx")
                }
                true
            }
            R.id.importMenu -> {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                pickFileLauncher.launch(intent)
                true
            }
            R.id.cloudMenu -> {
                /*val signInIntent = googleSignInHelper.getSignInIntent()
                startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN)
                Log.d("HomeFragment", "Google sign-in initiated")
                true*/
                AlertDialog.Builder(requireContext()).apply {
                    setTitle("Upload Notes")
                    setMessage("Do you want to upload your notes to Google Sheets?")
                    setPositiveButton("Yes") { _, _ ->
                        val signInIntent = googleSignInHelper.getSignInIntent()
                        startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN)
                        Log.d("HomeFragment", "Google sign-in initiated")
                    }
                    setNegativeButton("No", null)
                }.create().show()
                true
            }
            else -> false
        }
    }
}
