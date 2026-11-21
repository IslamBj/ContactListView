package com.example.listecontacts3.data

import android.content.Context
import android.graphics.Color
import androidx.preference.PreferenceManager

object PreferencesManager {
    const val KEY_TAILLE_POLICE = "taille_police"
    const val KEY_COULEUR_PREFEREE = "couleur_preferee"
    const val KEY_APPEL_DEPUIS_LISTE = "appel_depuis_liste"

    fun getTaillePoliceSp(context: Context): Float {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val strValue = prefs.getString(KEY_TAILLE_POLICE, "16") ?: "16"
        return strValue.toFloatOrNull() ?: 16f
    }

    fun getCouleurPrefereeInt(context: Context): Int {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val colorHex = prefs.getString(KEY_COULEUR_PREFEREE, "#000000") ?: "#000000"
        return try {
            Color.parseColor(colorHex)
        } catch (e: Exception) {
            Color.BLACK
        }
    }

    fun isAppelDepuisListeActive(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(KEY_APPEL_DEPUIS_LISTE, false)
    }
}
