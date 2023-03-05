package com.app.edithormobile.layouts.chat_gpt;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.app.edithormobile.databinding.ActivityChatGptBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

//TODO: ChatGPT Entegresi üzerinde çalışılacak.
public class GPT extends AppCompatActivity {
    ActivityChatGptBinding binding;
    String deger = null;

    //OkHTTP
    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatGptBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        //Send Button
        binding.chatSendButton.setOnClickListener(v -> {
            String q = binding.txtChat.getText().toString().trim();
            callAPI(q);
            binding.txtChat.setText("");
            hideKeyboardFrom(getApplicationContext(), v);
        });


    }

    void callAPI(String question)  {
        //API JSON Object
        JSONObject body = new JSONObject();
        try {
            body.put("model", "text-davinci-003");
            body.put("prompt", question);
            body.put("max_tokens", 4000);
            body.put("temperature", 0);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        //Request
        RequestBody req = RequestBody.create(JSON, body.toString());
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/completions")
                .header("Authorization", "Bearer YOUR_TOKEN")
                .post(req)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    final Toast toast = Toast.makeText(getApplicationContext(), "API Error", Toast.LENGTH_SHORT );
                    toast.show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()){
                    JSONObject jsonObject = null;
                    try {
                        assert response.body() != null;
                        jsonObject = new JSONObject(response.body().string());
                        JSONArray array = jsonObject.getJSONArray("choices");
                        String result = array.getJSONObject(0).getString("text");

                        runOnUiThread(() -> {
                            final Toast toast = Toast.makeText(getApplicationContext(), "Sonuc: " + result.trim(), Toast.LENGTH_LONG );
                            toast.show();
                            binding.chatResponse.setText(result.trim());
                        });
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }else{
                    runOnUiThread(() -> {
                        final Toast toast = Toast.makeText(getApplicationContext(), "Gonderilemedi", Toast.LENGTH_SHORT );
                        toast.show();
                    });
                }
            }
        });


    }

    //Hide Keyboard Func.
    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


}
