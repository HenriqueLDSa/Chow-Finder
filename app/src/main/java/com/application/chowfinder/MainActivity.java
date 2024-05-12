package com.application.chowfinder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private EditText mUserInput;
    private Button mSubmitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mUserInput = findViewById(R.id.input_text);
        mSubmitButton = findViewById(R.id.submit_button);

        mSubmitButton.setOnClickListener(v -> testApiKey());
    }

    private void testApiKey(){
        String apiKey = mUserInput.getText().toString();

        GenerativeModel gm = new GenerativeModel("gemini-pro", apiKey);
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        Content content = new Content.Builder().addText("Testing API Key").build();

        Executor executor = Executors.newCachedThreadPool();

        mSubmitButton.setText(R.string.validating_text);

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                System.out.println("API Key Success!");
                runOnUiThread(() -> {
                    saveApiKeyData(apiKey);
                    mSubmitButton.setText(R.string.submit_button_text);
                    switchToSearch();
                });
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                System.out.println("Error: " + t.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "API Key Error. Try again.", Toast.LENGTH_SHORT).show();
                    mUserInput.setText("");
                    mSubmitButton.setText(R.string.submit_button_text);
                });
            }
        }, executor);
    }

    private void saveApiKeyData(String api_key){
        SharedPreferences prefs = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("api_key", api_key);
        editor.apply();
    }

    private void switchToSearch(){
        Intent intent = new Intent(MainActivity.this, SearchActivity.class);
        startActivity(intent);
    }
}