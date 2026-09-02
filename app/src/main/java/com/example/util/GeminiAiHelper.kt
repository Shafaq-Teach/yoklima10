package com.example.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class ParsedMemberAi(
    val name: String,
    val contactAddress: String = "",
    val telegramContact: String = "",
    val whatsappContact: String = "",
    val subGroup: Int = 1,
    val notes: String = ""
)

object GeminiAiHelper {

    private const val GEMINI_MODEL = "gemini-2.5-flash"
    private const val API_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    suspend fun generateCustomReport(
        apiKey: String,
        userPrompt: String,
        systemContext: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val trimmedKey = apiKey.trim()
        if (trimmedKey.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Gemini API Key is empty"))
        }

        try {
            val urlString = "$API_BASE_URL/$GEMINI_MODEL:generateContent?key=$trimmedKey"
            val url = URL(urlString)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                doOutput = true
                doInput = true
                connectTimeout = 15000
                readTimeout = 25000
            }

            val fullPrompt = buildString {
                append("سىستېما سانلىق مەلۇماتلىرى ۋە ئەھۋالى:\n")
                append(systemContext)
                append("\n\nئىشلەتكۈچىنىڭ دوكلات چىقىرىش تەلىپى (User Request):\n")
                append(userPrompt.ifBlank { "بارلىق گۇرۇپپىلارنىڭ يوقلىما، نۆۋەتچىلىك، كەلمىگەنلەر ۋە قورال-ياراغ ئەھۋالىنى رەتلىك تېلېگرام / ۋاتسئاپ ئۈچۈن خۇلاسە دوكلات قىلىپ چىقىرىپ بەر." })
                append("\n\nتەلەپ: جاۋابنى چىرايلىق، رەتلىك بەلگىلەر (بۇرجەكلەر، ئېموجىلار، قۇرلار) بىلەن، ئۇيغۇرچە تەييارلاپ بەر.")
            }

            val jsonBody = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            val partObj = JSONObject().apply {
                                put("text", fullPrompt)
                            }
                            put(partObj)
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)
            }

            OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                writer.write(jsonBody.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                val responseText = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8")).use { reader ->
                    reader.readText()
                }
                val jsonResponse = JSONObject(responseText)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val text = parts.getJSONObject(0).optString("text", "")
                        if (text.isNotBlank()) {
                            return@withContext Result.success(text)
                        }
                    }
                }
                Result.failure(Exception("Gemini API returned empty content"))
            } else {
                val errorStream = connection.errorStream
                val errorText = errorStream?.let {
                    BufferedReader(InputStreamReader(it, "UTF-8")).use { r -> r.readText() }
                } ?: "HTTP $responseCode"
                Result.failure(Exception("Gemini API Error ($responseCode): $errorText"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun analyzeMemberAndExecuteTask(
        apiKey: String,
        userPrompt: String,
        textInput: String,
        imageBase64: String? = null,
        mimeType: String = "image/jpeg"
    ): Result<String> = withContext(Dispatchers.IO) {
        val trimmedKey = apiKey.trim()
        if (trimmedKey.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Gemini API Key is empty"))
        }

        try {
            val urlString = "$API_BASE_URL/$GEMINI_MODEL:generateContent?key=$trimmedKey"
            val url = URL(urlString)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                doOutput = true
                doInput = true
                connectTimeout = 20000
                readTimeout = 30000
            }

            val instruction = buildString {
                append("سىز ئەزا تىزىملاش ۋە خادىملار باشقۇرۇش سىستېمىسىنىڭ كۈچلۈك AI ياردەمچىسى.\n")
                append("ئىشلەتكۈچى تەمىنلىگەن تېكىست ياكى رەسىم ماتېرىيالىدىن ئەزالارنىڭ ئىسمى، تېلېفون نومۇرى، تېلېگرام، ۋاتسئاپ، گۇرۇپپا ياكى باشقا ئۇچۇرلىرىنى ئېنىق ئانالىز قىلىپ، ئىشلەتكۈچىنىڭ تەلىپىنى ئىجرا قىلىپ بەر.\n\n")
                if (userPrompt.isNotBlank()) {
                    append("ئىشلەتكۈچىنىڭ ئالاھىدە تەلىپى (User Prompt / Instruction):\n$userPrompt\n\n")
                }
                if (textInput.isNotBlank()) {
                    append("كىرگۈزۈلگەن تېكىست ياكى تىزىملىك:\n$textInput\n\n")
                }
                append("جاۋابىڭىزنى ئېنىق، رەتلىك، چۈشىنىشلىك ئۇيغۇرچە يېزىپ بەر. ئەگەر ئەزالار بار بولسا ئىسىم ۋە ئالاقىسىنى رەتلىك تىزىپ بەر.")
            }

            val jsonBody = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            val textPart = JSONObject().apply {
                                put("text", instruction)
                            }
                            put(textPart)

                            if (!imageBase64.isNullOrBlank()) {
                                val inlineData = JSONObject().apply {
                                    put("mime_type", mimeType)
                                    put("data", imageBase64)
                                }
                                val imagePart = JSONObject().apply {
                                    put("inline_data", inlineData)
                                }
                                put(imagePart)
                            }
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)
            }

            OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                writer.write(jsonBody.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                val responseText = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8")).use { reader ->
                    reader.readText()
                }
                val jsonResponse = JSONObject(responseText)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val text = parts.getJSONObject(0).optString("text", "")
                        if (text.isNotBlank()) {
                            return@withContext Result.success(text)
                        }
                    }
                }
                Result.failure(Exception("Gemini API returned empty content"))
            } else {
                val errorStream = connection.errorStream
                val errorText = errorStream?.let {
                    BufferedReader(InputStreamReader(it, "UTF-8")).use { r -> r.readText() }
                } ?: "HTTP $responseCode"
                Result.failure(Exception("Gemini API Error ($responseCode): $errorText"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun parseMembersWithAi(
        apiKey: String,
        userPrompt: String,
        rawText: String,
        fileBase64: String? = null,
        mimeType: String = "image/jpeg",
        defaultSubGroup: Int = 1
    ): Result<List<ParsedMemberAi>> = withContext(Dispatchers.IO) {
        val trimmedKey = apiKey.trim()
        if (trimmedKey.isBlank()) {
            // Fallback heuristic parsing if no API key provided
            val fallback = fallbackHeuristicParse(rawText, defaultSubGroup)
            return@withContext Result.success(fallback)
        }

        try {
            val urlString = "$API_BASE_URL/$GEMINI_MODEL:generateContent?key=$trimmedKey"
            val url = URL(urlString)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                doOutput = true
                doInput = true
                connectTimeout = 25000
                readTimeout = 35000
            }

            val prompt = buildString {
                append("You are an expert personnel and roster extraction AI assistant.\n")
                append("Your task is to extract all members/persons from the provided text, document, or image based on the user's instructions and divide them into sub-groups (SubGroup 1 or SubGroup 2).\n\n")
                if (userPrompt.isNotBlank()) {
                    append("USER INSTRUCTIONS / REQUIREMENTS:\n$userPrompt\n\n")
                }
                if (rawText.isNotBlank()) {
                    append("RAW TEXT / DATA INPUT:\n$rawText\n\n")
                }
                append("DEFAULT SUBGROUP: $defaultSubGroup\n\n")
                append("RULES:\n")
                append("1. Extract every individual member/person.\n")
                append("2. Divide members into subGroup 1 or 2 according to the user instructions, or default to $defaultSubGroup.\n")
                append("3. Output ONLY a valid JSON Array with no outer markdown fences or markdown commentary.\n")
                append("4. Schema for each object:\n")
                append("   {\n")
                append("     \"name\": \"Full Name (String, required)\",\n")
                append("     \"contactAddress\": \"Phone number or address (String)\",\n")
                append("     \"telegramContact\": \"Telegram username (@...) or phone (String)\",\n")
                append("     \"whatsappContact\": \"WhatsApp number (String)\",\n")
                append("     \"subGroup\": 1 or 2 (Integer),\n")
                append("     \"notes\": \"Any note/role or additional info (String)\"\n")
                append("   }\n")
            }

            val jsonBody = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            val textPart = JSONObject().apply {
                                put("text", prompt)
                            }
                            put(textPart)

                            if (!fileBase64.isNullOrBlank()) {
                                val inlineData = JSONObject().apply {
                                    put("mime_type", mimeType)
                                    put("data", fileBase64)
                                }
                                val filePart = JSONObject().apply {
                                    put("inline_data", inlineData)
                                }
                                put(filePart)
                            }
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)
            }

            OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                writer.write(jsonBody.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                val responseText = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8")).use { reader ->
                    reader.readText()
                }
                val jsonResponse = JSONObject(responseText)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        var text = parts.getJSONObject(0).optString("text", "").trim()
                        // Clean markdown fences if any
                        if (text.startsWith("```json")) {
                            text = text.removePrefix("```json").trim()
                        } else if (text.startsWith("```")) {
                            text = text.removePrefix("```").trim()
                        }
                        if (text.endsWith("```")) {
                            text = text.removeSuffix("```").trim()
                        }

                        val parsedList = mutableListOf<ParsedMemberAi>()
                        val jsonArray = JSONArray(text)
                        for (i in 0 until jsonArray.length()) {
                            val obj = jsonArray.getJSONObject(i)
                            val name = obj.optString("name", "").trim()
                            if (name.isNotBlank()) {
                                parsedList.add(
                                    ParsedMemberAi(
                                        name = name,
                                        contactAddress = obj.optString("contactAddress", "").trim(),
                                        telegramContact = obj.optString("telegramContact", "").trim(),
                                        whatsappContact = obj.optString("whatsappContact", "").trim(),
                                        subGroup = obj.optInt("subGroup", defaultSubGroup).coerceIn(1, 2),
                                        notes = obj.optString("notes", "").trim()
                                    )
                                )
                            }
                        }

                        if (parsedList.isNotEmpty()) {
                            return@withContext Result.success(parsedList)
                        }
                    }
                }
                // If API didn't return members, fallback to heuristic
                val fallback = fallbackHeuristicParse(rawText, defaultSubGroup)
                Result.success(fallback)
            } else {
                // Fallback to heuristic parser on API error
                val fallback = fallbackHeuristicParse(rawText, defaultSubGroup)
                Result.success(fallback)
            }
        } catch (e: Exception) {
            val fallback = fallbackHeuristicParse(rawText, defaultSubGroup)
            if (fallback.isNotEmpty()) {
                Result.success(fallback)
            } else {
                Result.failure(e)
            }
        }
    }

    private fun fallbackHeuristicParse(text: String, defaultSubGroup: Int): List<ParsedMemberAi> {
        val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
        val result = mutableListOf<ParsedMemberAi>()
        var currentSub = defaultSubGroup

        for (line in lines) {
            if (line.contains("1-گۇرۇپ") || line.contains("1-بۆلەك") || line.contains("گۇرۇپپا 1") || line.contains("1-گۇرۇپچا")) {
                currentSub = 1
                continue
            }
            if (line.contains("2-گۇرۇپ") || line.contains("2-بۆلەك") || line.contains("گۇرۇپپا 2") || line.contains("2-گۇرۇپچا")) {
                currentSub = 2
                continue
            }

            // Extract phone number if present
            val phoneRegex = Regex("""(\+?\d[\d\s\-]{6,}\d)""")
            val phoneMatch = phoneRegex.find(line)
            val phone = phoneMatch?.value?.replace(" ", "") ?: ""

            // Extract telegram if present (@username)
            val tgRegex = Regex("""@([a-zA-Z0-9_]+)""")
            val tgMatch = tgRegex.find(line)
            val tg = tgMatch?.value ?: ""

            // Clean name by removing phone, tg, leading numbers/bullet
            var cleanName = line
                .replace(phone, "")
                .replace(tg, "")
                .replace(Regex("""^\d+[\.\-\s\)\/、]+"""), "")
                .replace(Regex("""^[•\-\*]\s*"""), "")
                .replace(Regex("""[\(\)\[\],;]"""), " ")
                .trim()

            if (cleanName.isNotBlank() && cleanName.length <= 40) {
                result.add(
                    ParsedMemberAi(
                        name = cleanName,
                        contactAddress = phone,
                        telegramContact = tg,
                        whatsappContact = if (phone.startsWith("+")) phone else "",
                        subGroup = currentSub,
                        notes = ""
                    )
                )
            }
        }
        return result
    }
}

