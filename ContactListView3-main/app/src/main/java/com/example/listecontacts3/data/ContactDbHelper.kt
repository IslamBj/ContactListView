package com.example.listecontacts3.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ContactDbHelper private constructor(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "contacts.db"
        private const val DATABASE_VERSION = 1

        @Volatile
        private var instance: ContactDbHelper? = null

        fun getInstance(context: Context): ContactDbHelper {
            return instance ?: synchronized(this) {
                instance ?: ContactDbHelper(context.applicationContext).also { instance = it }
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableSQL = """
            CREATE TABLE ${ContactContract.TABLE_NAME} (
                ${ContactContract.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${ContactContract.COLUMN_NOM} TEXT NOT NULL,
                ${ContactContract.COLUMN_PRENOM} TEXT NOT NULL,
                ${ContactContract.COLUMN_TELEPHONE} TEXT NOT NULL,
                ${ContactContract.COLUMN_EMAIL} TEXT,
                ${ContactContract.COLUMN_PHOTO_URI} TEXT,
                ${ContactContract.COLUMN_LATITUDE} REAL,
                ${ContactContract.COLUMN_LONGITUDE} REAL
            );
        """.trimIndent()
        db.execSQL(createTableSQL)

        // Seed 5 demonstration contacts (at least 2 without GPS coordinates)
        insertSeedContact(db, "Dupont", "Jean", "0601020304", "jean.dupont@email.com", null, 48.8566, 2.3522)
        insertSeedContact(db, "Curie", "Marie", "0605060708", "marie.curie@email.com", null, 43.6047, 1.4442)
        insertSeedContact(db, "Martin", "Pierre", "0699887766", "pierre.martin@email.com", null, 45.7640, 4.8357)
        insertSeedContact(db, "Bernard", "Sophie", "0612345678", "sophie.bernard@email.com", null, null, null)
        insertSeedContact(db, "Durand", "Luc", "0655443322", "luc.durand@email.com", null, null, null)
    }

    private fun insertSeedContact(
        db: SQLiteDatabase,
        nom: String,
        prenom: String,
        telephone: String,
        email: String?,
        photoUri: String?,
        latitude: Double?,
        longitude: Double?
    ) {
        val values = ContentValues().apply {
            put(ContactContract.COLUMN_NOM, nom)
            put(ContactContract.COLUMN_PRENOM, prenom)
            put(ContactContract.COLUMN_TELEPHONE, telephone)
            put(ContactContract.COLUMN_EMAIL, email)
            put(ContactContract.COLUMN_PHOTO_URI, photoUri)
            put(ContactContract.COLUMN_LATITUDE, latitude)
            put(ContactContract.COLUMN_LONGITUDE, longitude)
        }
        db.insert(ContactContract.TABLE_NAME, null, values)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ${ContactContract.TABLE_NAME}")
        onCreate(db)
    }
}
