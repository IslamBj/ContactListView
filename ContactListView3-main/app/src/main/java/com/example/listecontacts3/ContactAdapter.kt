package com.example.listecontacts3

import android.net.Uri
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class ContactAdapter(
    private var contacts: List<Contact>,
    private var fontSizeSp: Float = 16f,
    private var textColorInt: Int = 0xFF000000.toInt(),
    private var showCallButton: Boolean = false,
    private val onClick: (Long) -> Unit,
    private val onCallClick: (String) -> Unit
) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    class ContactViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivPhoto: ImageView = view.findViewById(R.id.ivContactPhoto)
        val tvName: TextView = view.findViewById(R.id.tvContactName)
        val tvPhone: TextView = view.findViewById(R.id.tvContactPhone)
        val btnCallItem: ImageButton = view.findViewById(R.id.btnCallItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        holder.tvName.text = contact.fullName
        holder.tvPhone.text = contact.phoneNumber

        // Apply preferences for text size and color
        holder.tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSizeSp)
        holder.tvName.setTextColor(textColorInt)

        // Keep phone text size proportional (slightly smaller or equal)
        holder.tvPhone.setTextSize(TypedValue.COMPLEX_UNIT_SP, (fontSizeSp - 2f).coerceAtLeast(12f))

        if (!contact.photoUri.isNullOrEmpty()) {
            try {
                val uri = Uri.parse(contact.photoUri)
                if (uri.scheme == "content" || uri.scheme == "android.resource" || uri.scheme == "file") {
                    holder.ivPhoto.setImageURI(uri)
                } else {
                    val file = File(contact.photoUri!!)
                    if (file.exists()) {
                        holder.ivPhoto.setImageURI(Uri.fromFile(file))
                    } else {
                        holder.ivPhoto.setImageResource(R.drawable.ic_person_placeholder)
                    }
                }
            } catch (e: Exception) {
                holder.ivPhoto.setImageResource(R.drawable.ic_person_placeholder)
            }
        } else {
            holder.ivPhoto.setImageResource(R.drawable.ic_person_placeholder)
        }

        if (showCallButton && contact.phoneNumber.isNotBlank()) {
            holder.btnCallItem.visibility = View.VISIBLE
            holder.btnCallItem.setOnClickListener {
                onCallClick(contact.phoneNumber)
            }
        } else {
            holder.btnCallItem.visibility = View.GONE
            holder.btnCallItem.setOnClickListener(null)
        }

        holder.itemView.setOnClickListener {
            onClick(contact.id)
        }
    }

    override fun getItemCount(): Int = contacts.size

    fun updateData(
        newContacts: List<Contact>,
        newFontSizeSp: Float = fontSizeSp,
        newTextColorInt: Int = textColorInt,
        newShowCallButton: Boolean = showCallButton
    ) {
        contacts = newContacts
        fontSizeSp = newFontSizeSp
        textColorInt = newTextColorInt
        showCallButton = newShowShowCallButtonSafe(newShowCallButton)
        notifyDataSetChanged()
    }

    private fun newShowShowCallButtonSafe(value: Boolean): Boolean = value
}
