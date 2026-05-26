package com.chatbot.service;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class OpenAIService {

    private static final String API_KEY =
            System.getenv("OPENAI_API_KEY");

    private static final String URL =
            "https://api.openai.com/v1/chat/completions";

    private final OkHttpClient client = new OkHttpClient();

    public String preguntar(String prompt) {

        try {

            JSONObject json = new JSONObject();

            json.put("model", "gpt-4.1-mini");

            JSONArray messages = new JSONArray();

            JSONObject system = new JSONObject();
            system.put("role", "system");
            system.put("content",
                    """
                    Eres un chatbot de ventas para Marketplace.
                    Debes identificar:
                    - intención del usuario
                    - producto solicitado
                    - responder amigablemente.
                    """
            );

            JSONObject user = new JSONObject();
            user.put("role", "user");
            user.put("content", prompt);

            messages.put(system);
            messages.put(user);

            json.put("messages", messages);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(URL)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                return "Error consultando OpenAI";
            }

            String responseBody = response.body().string();

            JSONObject obj = new JSONObject(responseBody);

            return obj
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

        } catch (IOException e) {
            e.printStackTrace();
            return "Error conectando con OpenAI";
        }
    }
}