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
import androidx.core.content.ContextCompat;
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
import com.google.common.util.concurrent.SettableFuture;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class SearchActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private EditText mCityText;
    private EditText mStateText;
    private EditText mCuisineText;
    private Button mSearchBtn;
    private GenerativeModelFutures model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.search), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mCityText = findViewById(R.id.enter_city);
        mStateText = findViewById(R.id.enter_state);
        mCuisineText = findViewById(R.id.enter_cuisine);
        mSearchBtn = findViewById(R.id.search_button);

        prefs = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        String apiKey = prefs.getString("api_key", "No API Key Saved");

        GenerativeModel gm = new GenerativeModel("gemini-pro", apiKey);
        model = GenerativeModelFutures.from(gm);

        mSearchBtn.setOnClickListener(v -> checkValues());
    }

    private void checkValues() {
        mSearchBtn.setText(R.string.searching);

        checkCity(isValidCity -> checkState(isValidState -> runOnUiThread(() -> {
            if (isValidCity && isValidState) {
                saveResults().addListener(this::switchToResults, ContextCompat.getMainExecutor(this));
            } else if (!isValidCity && isValidState) {
                mSearchBtn.setText(R.string.search);
                Toast.makeText(SearchActivity.this, "Invalid city name. Try again.", Toast.LENGTH_SHORT).show();
            } else //noinspection ConstantValue
                if (isValidCity && !isValidState) {
                mSearchBtn.setText(R.string.search);
                Toast.makeText(SearchActivity.this, "Invalid state name. Try again.", Toast.LENGTH_SHORT).show();
            } else {
                mSearchBtn.setText(R.string.search);
                Toast.makeText(SearchActivity.this, "Invalid city and state. Try again.", Toast.LENGTH_SHORT).show();
            }
        })));
    }

    private void checkCity(Consumer<Boolean> callback) {
        Content content = new Content.Builder()
                .addText("Is the city \"" + mCityText.getText().toString() + "\" a real city in the US? (yes or no).")
                .build();

        Executor executor = Executors.newCachedThreadPool();
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                callback.accept("yes".equals(result.getText()));
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                callback.accept(false);
            }
        }, executor);
    }

    private void checkState(Consumer<Boolean> callback) {
        Content content = new Content.Builder()
                .addText("Is the state \"" + mStateText.getText().toString() + "\" a real state in the US? (yes or no).")
                .build();

        Executor executor = Executors.newCachedThreadPool();
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                callback.accept("yes".equals(result.getText()));
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                callback.accept(false);
            }
        }, executor);
    }

    private ListenableFuture<Void> saveResults() {
        String prompt = "Give me a list of 5 " + mCuisineText.getText().toString() + " restaurants"
                + " near " + mCityText.getText().toString() + ", " + mStateText.getText().toString()
                + ". List only. Response in the format \"Restaurant name: Restaurant address\". " +
                "Add indication of list order such as: -, 1., *. Example: SushiPOP: 123 main st, " +
                "city, state zipcode\n\nSushiPOP2: 122 main st, city, state zipcode\n\nSushiPOP3: "
                + "133 main st, city, state zipcode";

        Content content = new Content.Builder().addText(prompt).build();
        Executor executor = Executors.newCachedThreadPool();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        SettableFuture<Void> completionFuture = SettableFuture.create();

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("restaurant_list", result.getText());
                editor.apply();
                completionFuture.set(null); // Indicate completion
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("restaurant_list", "ERROR");
                editor.apply();
                completionFuture.setException(t);
            }
        }, executor);
        return completionFuture;
    }

    private void switchToResults() {
        mSearchBtn.setText(R.string.search);

        Intent intent = new Intent(SearchActivity.this, ResultsActivity.class);
        startActivity(intent);
    }
}
