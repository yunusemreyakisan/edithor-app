package com.app.edithormobile.service;

import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class APIService {
    //URL -> https://api.openai.com/v1/chat/completions
    private final String BASE_URL = "https://api.openai.com/v1/chat/completions";

    //Response (GPT-3.5-Turbo)
    public void getResponse(String question, TextView text, Context context) throws JSONException {
        RequestQueue queue = Volley.newRequestQueue(context);

        /** @author yunusemreyakisan
        {
        "model": "gpt-3.5-turbo",
        "messages": [{"role": "user", "content": "Hello!"}]
        }
         */

        //Parametrelere gore objelerin olusturulması
        //messageObject içerisine role ve content verilerini yerleştirdik.
        //Bu nesneyi daha sonra messageArray içerisine yerleştirdik.
        //Bu array'i genel jsonObject'e yerleştirdik.
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("model", "gpt-3.5-turbo");
        JSONArray messagesArray = new JSONArray();
        JSONObject messageObject = new JSONObject();
        messageObject.put("role", "user");
        messageObject.put("content", question);
        messagesArray.put(messageObject);
        jsonObject.put("messages", messagesArray);


        //Post istegi
        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, BASE_URL, jsonObject, response -> {
            try {
                String resMessage = response.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content").trim();
                text.setText(resMessage);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }, error -> {
            Toast.makeText(context, "Failed response", Toast.LENGTH_SHORT)
                    .show();
        }) {
            @NotNull
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json");
                params.put("Authorization", "Bearer YOUR_API_KEY");
                return params;
            }
        };
        postRequest.setRetryPolicy((RetryPolicy) (new RetryPolicy() {
            public int getCurrentTimeout() {
                return 50000;
            }

            public int getCurrentRetryCount() {
                return 50000;
            }

            public void retry(@Nullable VolleyError error) {
                Toast toast = Toast.makeText(context, (CharSequence) "API Hatası", Toast.LENGTH_SHORT);
                toast.show();
            }
        }));
        queue.add(postRequest);
    }

}
