package com.app.edithormobile.layouts.chat_gpt;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.edithormobile.R;
import com.app.edithormobile.adapters.MessageAdapter;
import com.app.edithormobile.databinding.ActivityChatGptBinding;
import com.app.edithormobile.models.GPTModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class AskGPT extends AppCompatActivity {
    private ActivityChatGptBinding binding;
    private MessageAdapter message_adapter;
    private ArrayList<GPTModel> messages;
    private DatabaseReference mDatabase;
    FirebaseAuth mAuth;
    private FirebaseUser mUser;
    String url = "https://api.openai.com/v1/completions";

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatGptBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary, this.getTheme()));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        }

        //init rv
        messages = new ArrayList<>();
        message_adapter = new MessageAdapter(messages);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this.getApplicationContext()));
        binding.recyclerView.setAdapter(message_adapter);
        binding.recyclerView.smoothScrollToPosition(message_adapter.getItemCount());


        //onClickListener
        binding.animationView.setOnClickListener(it -> {
            if (binding.txtChat.getText().length() > 0) {
                binding.animationView.playAnimation();
                hideKeyboardFrom(getApplicationContext(), it);
                messages.add(new GPTModel(binding.txtChat.getText().toString(), "user"));
                message_adapter.notifyDataSetChanged();
                messages.add(new GPTModel("Yazıyor...", "gpt"));
                try {
                    getResponse(binding.txtChat.getText().toString().trim());
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            } else {
                runOnUiThread((Runnable) (new Runnable() {
                    public void run() {
                        Toast toast = Toast.makeText(getApplicationContext(), (CharSequence) "Mesaj giriniz", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }));
            }
        });
    } //eof oncreate

    @Override
    protected void onStart() {
        super.onStart();
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    //Response
    private void getResponse(String question) throws JSONException {
        binding.txtChat.setText("");
        RequestQueue queue = Volley.newRequestQueue(this.getApplicationContext());
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("model", "text-davinci-003");
        jsonObject.put("prompt", question);
        jsonObject.put("max_tokens", 4000);
        jsonObject.put("temperature", 0);


        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    messages.remove(messages.size() - 1);
                    String resMessage = response.getJSONArray("choices").getJSONObject(0).getString("text").trim();
                    messages.add(new GPTModel(resMessage, "gpt"));
                    message_adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }, error -> {
            Toast.makeText(getApplicationContext(), "Failed response", Toast.LENGTH_SHORT)
                    .show();
        }) {
            @NotNull
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json");
                params.put("Authorization", "Bearer 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");
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
                runOnUiThread((Runnable) (new Runnable() {
                    public final void run() {
                        Toast toast = Toast.makeText(getApplicationContext(), (CharSequence) "API Hatası", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }));
            }
        }));
        queue.add(postRequest);
    }

    //Hide Keyboard
    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}