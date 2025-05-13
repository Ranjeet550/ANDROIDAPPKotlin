package com.constructionmanagement.app.ui.worker

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.constructionmanagement.app.ConstructionApp
import com.constructionmanagement.app.R
import com.constructionmanagement.app.data.model.Worker
import com.constructionmanagement.app.databinding.ActivityAddWorkerBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddWorkerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddWorkerBinding
    private lateinit var viewModel: WorkerViewModel
    private lateinit var viewModelFactory: WorkerViewModelFactory
    private var currentPhotoPath: String? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            showImageSourceDialog()
        } else {
            Toast.makeText(
                this,
                "Camera and storage permissions are required to take photos",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            currentPhotoPath?.let { path ->
                loadImageFromPath(path)
            }
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                val path = saveImageFromUri(uri)
                if (path != null) {
                    currentPhotoPath = path
                    loadImageFromPath(path)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddWorkerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Enable back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.add_worker)
        // Hide action bar since we have our own title
        supportActionBar?.hide()

        // Initialize ViewModel
        val application = application as ConstructionApp
        viewModelFactory = WorkerViewModelFactory(
            application.workerRepository,
            application.workerSiteAssignmentRepository
        )
        viewModel = ViewModelProvider(this, viewModelFactory)[WorkerViewModel::class.java]

        // Set up image selection
        setupImageSelection()

        // Set up status change
        setupStatusChange()

        // Set up save button
        binding.buttonSave.setOnClickListener {
            if (validateInputs()) {
                saveWorker()
            }
        }
    }

    private fun setupImageSelection() {
        binding.imageWorker.setOnClickListener {
            checkPermissionsAndShowImageDialog()
        }

        binding.imageAddPhoto.setOnClickListener {
            checkPermissionsAndShowImageDialog()
        }

        binding.textAddPhoto.setOnClickListener {
            checkPermissionsAndShowImageDialog()
        }
    }

    private fun setupStatusChange() {
        binding.switchActive.setOnCheckedChangeListener { _, isChecked ->
            binding.textStatusValue.text = if (isChecked) getString(R.string.active) else getString(R.string.inactive)
            binding.textStatusValue.setTextColor(
                ContextCompat.getColor(
                    this,
                    if (isChecked) R.color.success else R.color.secondary_text
                )
            )
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        // Validate name
        if (binding.editName.text.toString().trim().isEmpty()) {
            binding.layoutName.error = "Name is required"
            isValid = false
        } else {
            binding.layoutName.error = null
        }

        // Validate phone
        if (binding.editPhone.text.toString().trim().isEmpty()) {
            binding.layoutPhone.error = "Phone number is required"
            isValid = false
        } else {
            binding.layoutPhone.error = null
        }

        // Validate Aadhar
        if (binding.editAadhar.text.toString().trim().isEmpty()) {
            binding.layoutAadhar.error = "ID number is required"
            isValid = false
        } else {
            binding.layoutAadhar.error = null
        }

        return isValid
    }

    private fun saveWorker() {
        val name = binding.editName.text.toString().trim()
        val phone = binding.editPhone.text.toString().trim()
        val address = binding.editAddress.text.toString().trim()
        val role = binding.editRole.text.toString().trim()
        val aadhar = binding.editAadhar.text.toString().trim()
        val isActive = binding.switchActive.isChecked

        // Get current date as join date
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val joinDate = dateFormat.format(Date())

        // Create worker object
        val worker = Worker(
            id = 0, // Room will auto-generate the ID
            name = name,
            phoneNumber = phone,
            address = address,
            role = role,
            aadharNumber = aadhar,
            isActive = isActive,
            joinDate = joinDate,
            profileImagePath = currentPhotoPath
        )

        // Save worker to database
        viewModel.insertWorker(worker)
        Toast.makeText(this, "Worker added successfully", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun checkPermissionsAndShowImageDialog() {
        val cameraPermission = Manifest.permission.CAMERA
        val storagePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE

        val cameraPermissionGranted = ContextCompat.checkSelfPermission(
            this, cameraPermission
        ) == PackageManager.PERMISSION_GRANTED

        val storagePermissionGranted = ContextCompat.checkSelfPermission(
            this, storagePermission
        ) == PackageManager.PERMISSION_GRANTED

        if (cameraPermissionGranted && storagePermissionGranted) {
            showImageSourceDialog()
        } else {
            requestPermissionLauncher.launch(arrayOf(cameraPermission, storagePermission))
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")

        AlertDialog.Builder(this)
            .setTitle("Select Profile Picture")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> dispatchTakePictureIntent()
                    1 -> dispatchPickImageIntent()
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show()
                    null
                }

                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.constructionmanagement.app.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    takePictureLauncher.launch(takePictureIntent)
                }
            }
        }
    }

    private fun dispatchPickImageIntent() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun saveImageFromUri(uri: Uri): String? {
        try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val file = createImageFile()
            inputStream.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            return file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    private fun loadImageFromPath(path: String) {
        Glide.with(this)
            .load(File(path))
            .circleCrop()
            .placeholder(R.drawable.circle_background)
            .into(binding.imageWorker)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
