package com.chatbot.service;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class OpenAIService {

    private static final String API_KEY =
            System.getenv("GROQ_API_KEY");

    private static final String URL =
            "https://api.groq.com/openai/v1/chat/completions";

    private final OkHttpClient client =
            new OkHttpClient();

    public String preguntar(String prompt) {

        try {

            JSONObject json = new JSONObject();

            json.put(
                    "model",
                    "llama3-8b-8192"
            );

            JSONArray messages =
                    new JSONArray();

            JSONObject system =
                    new JSONObject();

            system.put("role", "system");

            system.put(
                    "content",
                    """
                    Eres un vendedor experto de Marketplace.
                    
                    Tu personalidad es:
                    - amigable
                    - natural
                    - profesional
                    - humano
                    - conversacional
                    
                    Debes:
                    - recomendar productos
                    - responder naturalmente
                    - hablar como vendedor real
                    - usar emojis moderadamente
                    - ser breve y útil
                    """
            );

            JSONObject user =
                    new JSONObject();

            user.put("role", "user");

            user.put("content", prompt);

            messages.put(system);
            messages.put(user);

            json.put("messages", messages);

            RequestBody body =
                    RequestBody.create(
                            json.toString(),
                            MediaType.parse(
                                    "application/json"
                            )
                    );

            Request request =
                    new Request.Builder()
                            .url(URL)
                            .addHeader(
                                    "Authorization",
                                    "Bearer " + API_KEY
                            )
                            .addHeader(
                                    "Content-Type",
                                    "application/json"
                            )
                            .post(body)
                            .build();

            Response response =
                    client.newCall(request)
                            .execute();

            String responseBody =
                    response.body().string();

            if (!response.isSuccessful()) {

                System.out.println(responseBody);

                return "Error GROQ: " + responseBody;
            }

            JSONObject obj =
                    new JSONObject(responseBody);

            return obj
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

        } catch (IOException e) {

            e.printStackTrace();

            return "Error conectando con GROQ";
        }
    }
}