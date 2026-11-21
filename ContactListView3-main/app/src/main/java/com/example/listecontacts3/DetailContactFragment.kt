package com.example.listecontacts3

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.example.listecontacts3.data.PreferencesManager
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class DetailContactFragment : Fragment(R.layout.fragment_detail_contact) {

    private val viewModel: ContactViewModel by activityViewModels()
    private var currentContact: Contact? = null
    private var tempPhotoUri: String? = null

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            lifecycleScope.launch(Dispatchers.IO) {
                val savedPath = copyUriToInternalStorage(it)
                withContext(Dispatchers.Main) {
                    if (savedPath != null) {
                        tempPhotoUri = savedPath
                        displayPhoto(savedPath)
                    } else {
                        Toast.makeText(requireContext(), "Erreur lors de la sauvegarde de la photo", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let {
            lifecycleScope.launch(Dispatchers.IO) {
                val savedPath = saveBitmapToInternalStorage(it)
                withContext(Dispatchers.Main) {
                    if (savedPath != null) {
                        tempPhotoUri = savedPath
                        displayPhoto(savedPath)
                    } else {
                        Toast.makeText(requireContext(), "Erreur lors de la sauvegarde de la photo", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(requireContext(), "Permission caméra refusée", Toast.LENGTH_SHORT).show()
        }
    }

    private fun copyUriToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return null
            val photosDir = File(requireContext().filesDir, "contact_photos")
            if (!photosDir.exists()) photosDir.mkdirs()
            val file = File(photosDir, "photo_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun saveBitmapToInternalStorage(bitmap: Bitmap): String? {
        return try {
            val photosDir = File(requireContext().filesDir, "contact_photos")
            if (!photosDir.exists()) photosDir.mkdirs()
            val file = File(photosDir, "photo_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.close()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun displayPhoto(photoPath: String?) {
        val ivPhoto = view?.findViewById<ImageView>(R.id.ivDetailPhoto) ?: return
        if (!photoPath.isNullOrEmpty()) {
            try {
                val file = File(photoPath)
                if (file.exists()) {
                    ivPhoto.setImageURI(Uri.fromFile(file))
                } else {
                    ivPhoto.setImageURI(Uri.parse(photoPath))
                }
            } catch (e: Exception) {
                ivPhoto.setImageResource(R.drawable.ic_person_placeholder)
            }
        } else {
            ivPhoto.setImageResource(R.drawable.ic_person_placeholder)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            title = "Détail du contact"
            setDisplayHomeAsUpEnabled(true)
        }

        val etFirstName = view.findViewById<TextInputEditText>(R.id.etFirstName)
        val etLastName = view.findViewById<TextInputEditText>(R.id.etLastName)
        val etPhone = view.findViewById<TextInputEditText>(R.id.etPhone)
        val etEmail = view.findViewById<TextInputEditText>(R.id.etEmail)
        val etLatitude = view.findViewById<TextInputEditText>(R.id.etLatitude)
        val etLongitude = view.findViewById<TextInputEditText>(R.id.etLongitude)

        // Apply font size and text color preferences
        val fontSizeSp = PreferencesManager.getTaillePoliceSp(requireContext())
        val textColorInt = PreferencesManager.getCouleurPrefereeInt(requireContext())

        etFirstName.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSizeSp)
        etLastName.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSizeSp)
        etPhone.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSizeSp)
        etFirstName.setTextColor(textColorInt)
        etLastName.setTextColor(textColorInt)

        val contactId = viewModel.selectedContactId.value
        val isExistingContact = contactId != null && contactId != 0L

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                if (isExistingContact) {
                    menu.add(Menu.NONE, 102, Menu.NONE, "Supprimer")
                        .setIcon(android.R.drawable.ic_menu_delete)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == 102) {
                    confirmAndDeleteContact()
                    return true
                } else if (menuItem.itemId == android.R.id.home) {
                    parentFragmentManager.popBackStack()
                    return true
                }
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        val btnGallery = view.findViewById<Button>(R.id.btnGallery)
        val btnCamera = view.findViewById<Button>(R.id.btnCamera)
        val btnCall = view.findViewById<Button>(R.id.btnCall)
        val btnEmail = view.findViewById<Button>(R.id.btnEmail)
        val btnShowMap = view.findViewById<Button>(R.id.btnShowMap)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val btnSave = view.findViewById<Button>(R.id.btnSave)

        if (isExistingContact && contactId != null) {
            viewModel.getContact(contactId) { contact ->
                currentContact = contact
                contact?.let { c ->
                    etFirstName.setText(c.firstName)
                    etLastName.setText(c.lastName)
                    etPhone.setText(c.phoneNumber)
                    etEmail.setText(c.email)
                    c.latitude?.let { etLatitude.setText(it.toString()) }
                    c.longitude?.let { etLongitude.setText(it.toString()) }
                    tempPhotoUri = c.photoUri
                    displayPhoto(c.photoUri)
                }
            }
        } else {
            // New contact mode
            currentContact = Contact(0L, "", "", "", "", null, null, null)
            tempPhotoUri = null
        }

        btnGallery.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        btnCamera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                cameraLauncher.launch(null)
            } else {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        btnCall.setOnClickListener {
            val phone = etPhone.text.toString().trim()
            if (phone.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Aucune application de téléphone disponible", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Numéro de téléphone vide", Toast.LENGTH_SHORT).show()
            }
        }

        btnEmail.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (email.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Aucune application de messagerie disponible", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Adresse e-mail vide", Toast.LENGTH_SHORT).show()
            }
        }

        btnShowMap.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, CarteContactFragment())
                .addToBackStack(null)
                .commit()
        }

        btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnSave.setOnClickListener {
            val firstName = etFirstName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val latStr = etLatitude.text.toString().trim()
            val lonStr = etLongitude.text.toString().trim()

            if (firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty()) {
                Toast.makeText(requireContext(), "Veuillez remplir les champs obligatoires (Prénom, Nom, Téléphone)", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            var latitude: Double? = null
            var longitude: Double? = null

            if (latStr.isNotEmpty()) {
                val lat = latStr.toDoubleOrNull()
                if (lat == null || lat < -90.0 || lat > 90.0) {
                    Toast.makeText(requireContext(), "Latitude invalide (-90 à 90)", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                latitude = lat
            }

            if (lonStr.isNotEmpty()) {
                val lon = lonStr.toDoubleOrNull()
                if (lon == null || lon < -180.0 || lon > 180.0) {
                    Toast.makeText(requireContext(), "Longitude invalide (-180 à 180)", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                longitude = lon
            }

            val contactToSave = currentContact ?: Contact(0L, "", "", "", "", null, null, null)
            contactToSave.firstName = firstName
            contactToSave.lastName = lastName
            contactToSave.phoneNumber = phone
            contactToSave.email = email
            contactToSave.photoUri = tempPhotoUri
            contactToSave.latitude = latitude
            contactToSave.longitude = longitude

            viewModel.saveContact(contactToSave) {
                Toast.makeText(requireContext(), "Contact enregistré avec succès", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun confirmAndDeleteContact() {
        val contactId = viewModel.selectedContactId.value ?: 0L
        if (contactId == 0L) return

        AlertDialog.Builder(requireContext())
            .setTitle("Confirmation")
            .setMessage("Voulez-vous vraiment supprimer ce contact ?")
            .setPositiveButton("Supprimer") { _, _ ->
                viewModel.deleteContact(contactId) {
                    Toast.makeText(requireContext(), "Contact supprimé", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
}
