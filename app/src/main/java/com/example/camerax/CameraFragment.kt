package com.example.camerax

import android.Manifest
import android.content.ContentValues
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.camerax.databinding.FragmentCameraBinding
import com.example.camerax.ext.hasPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class CameraFragment : Fragment() {
    private var _binding: FragmentCameraBinding? = null
    private val binding
        get() = _binding
            ?: throw IllegalStateException("${this.javaClass.simpleName} - ViewDataBinding is released")

    private var imageCapture: ImageCapture? = null
    private val contentValues: ContentValues
        get() = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "${System.currentTimeMillis()}.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }
        }
    private val outputFileOptions
        get() = ImageCapture.OutputFileOptions.Builder(
            requireContext().contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()
    private val cameraExecutor by lazy { Executors.newSingleThreadExecutor() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentCameraBinding.inflate(inflater, container, false).also {
        _binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraSetup()
        binding.btnCapture.setOnClickListener { takePhoto() }
    }

    private fun cameraSetup() = with(binding) {
        ProcessCameraProvider.getInstance(requireContext()).apply {
            addListener({
                val cameraProvider: ProcessCameraProvider = get()
                val preview = Preview.Builder()
                    .build()
                    .also { it.setSurfaceProvider(preview.surfaceProvider) }
                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                runCatching {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        viewLifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                }
            }, ContextCompat.getMainExecutor(requireContext()))
        }
    }

    private fun takePhoto() = imageCapture?.run {
        takePicture(outputFileOptions, cameraExecutor, object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                lifecycleScope.launch(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        outputFileResults.savedUri.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onError(exception: ImageCaptureException) {
                lifecycleScope.launch(Dispatchers.Main) {
                    Toast.makeText(requireContext(), exception.toString(), Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (!requireContext().hasPermission(Manifest.permission.CAMERA))
            findNavController().navigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}