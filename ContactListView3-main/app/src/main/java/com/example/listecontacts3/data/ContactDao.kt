package com.example.listecontacts3.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.listecontacts3.Contact

class ContactDao(context: Context) {
    private val dbHelper = ContactDbHelper.getInstance(context)

    fun insert(contact: Contact): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(ContactContract.COLUMN_NOM, contact.lastName)
            put(ContactContract.COLUMN_PRENOM, contact.firstName)
            put(ContactContract.COLUMN_TELEPHONE, contact.phoneNumber)
            put(ContactContract.COLUMN_EMAIL, contact.email)
            put(ContactContract.COLUMN_PHOTO_URI, contact.photoUri)
            put(ContactContract.COLUMN_LATITUDE, contact.latitude)
            put(ContactContract.COLUMN_LONGITUDE, contact.longitude)
        }
        return db.insert(ContactContract.TABLE_NAME, null, values)
    }

    fun update(contact: Contact): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(ContactContract.COLUMN_NOM, contact.lastName)
            put(ContactContract.COLUMN_PRENOM, contact.firstName)
            put(ContactContract.COLUMN_TELEPHONE, contact.phoneNumber)
            put(ContactContract.COLUMN_EMAIL, contact.email)
            put(ContactContract.COLUMN_PHOTO_URI, contact.photoUri)
            put(ContactContract.COLUMN_LATITUDE, contact.latitude)
            put(ContactContract.COLUMN_LONGITUDE, contact.longitude)
        }
        return db.update(
            ContactContract.TABLE_NAME,
            values,
            "${ContactContract.COLUMN_ID} = ?",
            arrayOf(contact.id.toString())
        )
    }

    fun delete(id: Long): Int {
        val db = dbHelper.writableDatabase
        return db.delete(
            ContactContract.TABLE_NAME,
            "${ContactContract.COLUMN_ID} = ?",
            arrayOf(id.toString())
        )
    }

    fun getAll(): List<Contact> {
        val db = dbHelper.readableDatabase
        val contacts = mutableListOf<Contact>()
        val query = "SELECT * FROM ${ContactContract.TABLE_NAME} ORDER BY ${ContactContract.COLUMN_NOM} COLLATE NOCASE ASC, ${ContactContract.COLUMN_PRENOM} COLLATE NOCASE ASC"
        db.rawQuery(query, null).use { cursor ->
            while (cursor.moveToNext()) {
                contacts.add(cursorToContact(cursor))
            }
        }
        return contacts
    }

    fun getById(id: Long): Contact? {
        val db = dbHelper.readableDatabase
        val query = "SELECT * FROM ${ContactContract.TABLE_NAME} WHERE ${ContactContract.COLUMN_ID} = ?"
        db.rawQuery(query, arrayOf(id.toString())).use { cursor ->
            if (cursor.moveToFirst()) {
                return cursorToContact(cursor)
            }
        }
        return null
    }

    fun search(texte: String): List<Contact> {
        val db = dbHelper.readableDatabase
        val contacts = mutableListOf<Contact>()
        val pattern = "%$texte%"
        val selection = "${ContactContract.COLUMN_NOM} LIKE ? OR ${ContactContract.COLUMN_PRENOM} LIKE ? OR ${ContactContract.COLUMN_TELEPHONE} LIKE ?"
        val selectionArgs = arrayOf(pattern, pattern, pattern)
        val orderBy = "${ContactContract.COLUMN_NOM} COLLATE NOCASE ASC, ${ContactContract.COLUMN_PRENOM} COLLATE NOCASE ASC"

        db.query(
            ContactContract.TABLE_NAME,
            null,
            selection,
            selectionArgs,
            null,
            null,
            orderBy
        ).use { cursor ->
            while (cursor.moveToNext()) {
                contacts.add(cursorToContact(cursor))
            }
        }
        return contacts
    }

    private fun cursorToContact(cursor: Cursor): Contact {
        val id = cursor.getLong(cursor.getColumnIndexOrThrow(ContactContract.COLUMN_ID))
        val nom = cursor.getString(cursor.getColumnIndexOrThrow(ContactContract.COLUMN_NOM))
        val prenom = cursor.getString(cursor.getColumnIndexOrThrow(ContactContract.COLUMN_PRENOM))
        val telephone = cursor.getString(cursor.getColumnIndexOrThrow(ContactContract.COLUMN_TELEPHONE))
        val email = cursor.getString(cursor.getColumnIndexOrThrow(ContactContract.COLUMN_EMAIL))
        val photoUri = cursor.getString(cursor.getColumnIndexOrThrow(ContactContract.COLUMN_PHOTO_URI))

        val latIndex = cursor.getColumnIndexOrThrow(ContactContract.COLUMN_LATITUDE)
        val latitude = if (cursor.isNull(latIndex)) null else cursor.getDouble(latIndex)

        val lonIndex = cursor.getColumnIndexOrThrow(ContactContract.COLUMN_LONGITUDE)
        val longitude = if (cursor.isNull(lonIndex)) null else cursor.getDouble(lonIndex)

        return Contact(
            id = id,
            firstName = prenom,
            lastName = nom,
            phoneNumber = telephone,
            email = email ?: "",
            photoUri = photoUri,
            latitude = latitude,
            longitude = longitude
        )
    }
}
