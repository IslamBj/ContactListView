package com.example.listecontacts3

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.listecontacts3.data.ContactDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContactViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = ContactDao(application)

    private val _contacts = MutableLiveData<List<Contact>>()
    val contacts: LiveData<List<Contact>> = _contacts

    private val _filteredContacts = MutableLiveData<List<Contact>>()
    val filteredContacts: LiveData<List<Contact>> = _filteredContacts

    private val _selectedContactId = MutableLiveData<Long?>()
    val selectedContactId: LiveData<Long?> = _selectedContactId

    private val _searchQuery = MutableLiveData<String>("")
    val searchQuery: LiveData<String> = _searchQuery

    init {
        loadContacts()
    }

    fun loadContacts() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = dao.getAll()
            withContext(Dispatchers.Main) {
                _contacts.value = list
                filter(_searchQuery.value ?: "")
            }
        }
    }

    fun selectContact(id: Long?) {
        _selectedContactId.value = id
    }

    fun getContact(id: Long, callback: (Contact?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val contact = dao.getById(id)
            withContext(Dispatchers.Main) {
                callback(contact)
            }
        }
    }

    fun saveContact(contact: Contact, onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            if (contact.id == 0L) {
                dao.insert(contact)
            } else {
                dao.update(contact)
            }
            val list = dao.getAll()
            withContext(Dispatchers.Main) {
                _contacts.value = list
                filter(_searchQuery.value ?: "")
                onComplete()
            }
        }
    }

    fun deleteContact(id: Long, onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.delete(id)
            val list = dao.getAll()
            withContext(Dispatchers.Main) {
                _contacts.value = list
                filter(_searchQuery.value ?: "")
                onComplete()
            }
        }
    }

    fun filter(query: String) {
        _searchQuery.value = query
        viewModelScope.launch(Dispatchers.IO) {
            val list = if (query.isBlank()) {
                dao.getAll()
            } else {
                dao.search(query)
            }
            withContext(Dispatchers.Main) {
                _filteredContacts.value = list
            }
        }
    }
}
