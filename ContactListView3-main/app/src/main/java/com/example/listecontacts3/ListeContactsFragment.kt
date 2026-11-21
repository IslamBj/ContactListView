package com.example.listecontacts3

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.listecontacts3.data.PreferencesManager
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ListeContactsFragment : Fragment(R.layout.fragment_liste_contacts) {

    private val viewModel: ContactViewModel by activityViewModels()
    private lateinit var adapter: ContactAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            title = "Mes contacts"
            setDisplayHomeAsUpEnabled(false)
        }

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.add(Menu.NONE, 101, Menu.NONE, "Ajouter")
                    .setIcon(android.R.drawable.ic_menu_add)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)

                menu.add(Menu.NONE, 103, Menu.NONE, "Paramètres")
                    .setIcon(android.R.drawable.ic_menu_preferences)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    101 -> {
                        openCreateContact()
                        return true
                    }
                    103 -> {
                        openSettings()
                        return true
                    }
                }
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        val etSearch = view.findViewById<EditText>(R.id.etSearch)
        val rvContacts = view.findViewById<RecyclerView>(R.id.rvContacts)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmpty)
        val fabAddContact = view.findViewById<FloatingActionButton>(R.id.fabAddContact)

        fabAddContact.setOnClickListener {
            openCreateContact()
        }

        adapter = ContactAdapter(
            contacts = emptyList(),
            fontSizeSp = PreferencesManager.getTaillePoliceSp(requireContext()),
            textColorInt = PreferencesManager.getCouleurPrefereeInt(requireContext()),
            showCallButton = PreferencesManager.isAppelDepuisListeActive(requireContext()),
            onClick = { contactId ->
                viewModel.selectContact(contactId)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container_view, DetailContactFragment())
                    .addToBackStack(null)
                    .commit()
            },
            onCallClick = { phoneNumber ->
                if (phoneNumber.isNotBlank()) {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
                    try {
                        startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Aucune application de téléphone disponible", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Numéro de téléphone vide", Toast.LENGTH_SHORT).show()
                }
            }
        )

        rvContacts.layoutManager = LinearLayoutManager(requireContext())
        rvContacts.adapter = adapter

        viewModel.filteredContacts.observe(viewLifecycleOwner) { contacts ->
            val fontSizeSp = PreferencesManager.getTaillePoliceSp(requireContext())
            val textColorInt = PreferencesManager.getCouleurPrefereeInt(requireContext())
            val showCallButton = PreferencesManager.isAppelDepuisListeActive(requireContext())
            adapter.updateData(contacts, fontSizeSp, textColorInt, showCallButton)

            if (contacts.isEmpty()) {
                tvEmpty.visibility = View.VISIBLE
                rvContacts.visibility = View.GONE
            } else {
                tvEmpty.visibility = View.GONE
                rvContacts.visibility = View.VISIBLE
            }
        }

        viewModel.searchQuery.observe(viewLifecycleOwner) { query ->
            if (etSearch.text.toString() != query) {
                etSearch.setText(query)
            }
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.filter(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onResume() {
        super.onResume()
        // Reload contacts from DB and refresh adapter preferences when returning to this fragment
        viewModel.loadContacts()
        val fontSizeSp = PreferencesManager.getTaillePoliceSp(requireContext())
        val textColorInt = PreferencesManager.getCouleurPrefereeInt(requireContext())
        val showCallButton = PreferencesManager.isAppelDepuisListeActive(requireContext())
        val currentList = viewModel.filteredContacts.value ?: emptyList()
        adapter.updateData(currentList, fontSizeSp, textColorInt, showCallButton)
    }

    private fun openCreateContact() {
        viewModel.selectContact(0L)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_view, DetailContactFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun openSettings() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_view, ParametresFragment())
            .addToBackStack(null)
            .commit()
    }
}
