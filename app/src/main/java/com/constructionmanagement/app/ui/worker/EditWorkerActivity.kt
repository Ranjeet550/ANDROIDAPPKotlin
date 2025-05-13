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
import android.view.View
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
import com.constructionmanagement.app.databinding.ActivityEditWorkerBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EditWorkerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditWorkerBinding
    private lateinit var viewModel: WorkerViewModel
    private lateinit var viewModelFactory: WorkerViewModelFactory
    private var workerId: Long = 0
    private var currentPhotoPath: String? = null
    private var worker: Worker? = null

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
        binding = ActivityEditWorkerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get worker ID from intent
        workerId = intent.getLongExtra("worker_id", 0)
        if (workerId == 0L) {
            Toast.makeText(this, "Invalid worker ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Enable back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.edit_worker)

        // Initialize ViewModel
        val application = application as ConstructionApp
        viewModelFactory = WorkerViewModelFactory(
            application.workerRepository,
            application.workerSiteAssignmentRepository
        )
        viewModel = ViewModelProvider(this, viewModelFactory)[WorkerViewModel::class.java]

        // Load worker details
        viewModel.getWorkerById(workerId).observe(this) { workerData ->
            if (workerData != null) {
                worker = workerData
                populateWorkerData(workerData)
            } else {
                Toast.makeText(this, "Worker not found", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        // Set up save button
        binding.buttonSave.setOnClickListener {
            if (validateInputs()) {
                updateWorker()
            }
        }

        // Set up image selection
        binding.imageWorker.setOnClickListener {
            checkPermissionsAndShowImageDialog()
        }

        // Set up status change
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

    private fun populateWorkerData(worker: Worker) {
        binding.editName.setText(worker.name)
        binding.editPhone.setText(worker.phoneNumber)
        binding.editAddress.setText(worker.address)
        binding.editRole.setText(worker.role)
        binding.editAadhar.setText(worker.aadharNumber)
        binding.switchActive.isChecked = worker.isActive
        binding.textStatusValue.text = if (worker.isActive) getString(R.string.active) else getString(R.string.inactive)
        binding.textStatusValue.setTextColor(
            ContextCompat.getColor(
                this,
                if (worker.isActive) R.color.success else R.color.secondary_text
            )
        )

        // Load profile image if available
        if (worker.profileImagePath != null) {
            currentPhotoPath = worker.profileImagePath
            loadImageFromPath(worker.profileImagePath)
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

        // Validate address
        if (binding.editAddress.text.toString().trim().isEmpty()) {
            binding.layoutAddress.error = "Address is required"
            isValid = false
        } else {
            binding.layoutAddress.error = null
        }

        // Validate role
        if (binding.editRole.text.toString().trim().isEmpty()) {
            binding.layoutRole.error = "Role is required"
            isValid = false
        } else {
            binding.layoutRole.error = null
        }

        // Validate Aadhar
        if (binding.editAadhar.text.toString().trim().isEmpty()) {
            binding.layoutAadhar.error = "Aadhar number is required"
            isValid = false
        } else {
            binding.layoutAadhar.error = null
        }

        return isValid
    }

    private fun updateWorker() {
        val name = binding.editName.text.toString().trim()
        val phone = binding.editPhone.text.toString().trim()
        val address = binding.editAddress.text.toString().trim()
        val role = binding.editRole.text.toString().trim()
        val aadhar = binding.editAadhar.text.toString().trim()
        val isActive = binding.switchActive.isChecked

        // Create updated worker object
        worker?.let {
            val updatedWorker = Worker(
                id = it.id,
                name = name,
                phoneNumber = phone,
                address = address,
                role = role,
                aadharNumber = aadhar,
                isActive = isActive,
                joinDate = it.joinDate,
                profileImagePath = currentPhotoPath
            )

            // Update worker in database
            viewModel.updateWorker(updatedWorker)
            Toast.makeText(this, "Worker updated successfully", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun checkPermissionsAndShowImageDialog() {
        val cameraPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        )
        val storagePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        val permissionsToRequest = mutableListOf<String>()
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CAMERA)
        }
        if (storagePermission != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            showImageSourceDialog()
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
        AlertDialog.Builder(this)
            .setTitle("Select Profile Picture")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> takePhoto()
                    1 -> pickImageFromGallery()
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun takePhoto() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    Toast.makeText(
                        this,
                        "Error occurred while creating the file",
                        Toast.LENGTH_SHORT
                    ).show()
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

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
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
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
            val file = File.createTempFile(
                "JPEG_${timeStamp}_",
                ".jpg",
                storageDir
            )

            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            inputStream.close()
            return file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    private fun loadImageFromPath(path: String) {
        val file = File(path)
        if (file.exists()) {
            binding.imageUploadPrompt.visibility = View.GONE
            Glide.with(this)
                .load(file)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .centerCrop()
                .into(binding.imageWorker)
        }
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
