package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object ContactUtils {

    fun openTelegram(context: Context, contact: String) {
        val trimmed = contact.trim()
        if (trimmed.isEmpty()) return

        try {
            val uri = when {
                trimmed.startsWith("https://t.me/") || trimmed.startsWith("http://t.me/") -> {
                    Uri.parse(trimmed)
                }
                trimmed.startsWith("t.me/") -> {
                    Uri.parse("https://$trimmed")
                }
                trimmed.startsWith("@") -> {
                    val handle = trimmed.removePrefix("@")
                    Uri.parse("https://t.me/$handle")
                }
                trimmed.matches(Regex("^[0-9+]+$")) -> {
                    val cleanPhone = trimmed.replace(Regex("[^0-9+]"), "")
                    Uri.parse("https://t.me/+$cleanPhone")
                }
                else -> {
                    Uri.parse("https://t.me/$trimmed")
                }
            }

            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Telegram: $trimmed", Toast.LENGTH_SHORT).show()
        }
    }

    fun openWhatsApp(context: Context, contact: String) {
        val trimmed = contact.trim()
        if (trimmed.isEmpty()) return

        try {
            val uri = when {
                trimmed.startsWith("https://wa.me/") || trimmed.startsWith("http://wa.me/") -> {
                    Uri.parse(trimmed)
                }
                trimmed.startsWith("wa.me/") -> {
                    Uri.parse("https://$trimmed")
                }
                else -> {
                    val cleanPhone = trimmed.replace(Regex("[^0-9]"), "")
                    Uri.parse("https://wa.me/$cleanPhone")
                }
            }

            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp: $trimmed", Toast.LENGTH_SHORT).show()
        }
    }

    fun openPhoneCall(context: Context, contact: String) {
        val trimmed = contact.trim()
        if (trimmed.isEmpty()) return

        try {
            val cleanPhone = trimmed.replace(Regex("[^0-9+]"), "")
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanPhone"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, contact, Toast.LENGTH_SHORT).show()
        }
    }
}
