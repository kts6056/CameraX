package com.example.camerax

import android.Manifest
import android.content.pm.PackageManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.camerax.ext.hasPermission

class PermissionsFragment : Fragment() {
    override fun onResume() {
        super.onResume()
        if (!requireContext().hasPermission(Manifest.permission.CAMERA)) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), PERMISSIONS_REQUEST_CODE)
        } else {
            navigateToCamera()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE &&
            grantResults.any { it == PackageManager.PERMISSION_GRANTED }
        ) navigateToCamera()
    }

    private fun navigateToCamera() = lifecycleScope.launchWhenCreated {
        findNavController().navigate(PermissionsFragmentDirections.actionPermissionToCamera())
    }

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 10
    }
}
