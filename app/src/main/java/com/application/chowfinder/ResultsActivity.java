package com.application.chowfinder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ResultsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_results);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.results), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences prefs = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        String results = prefs.getString("restaurant_list", "No Results");

        createResultsText(results, findViewById(R.id.results_container));

        Button mNewSearch = findViewById(R.id.new_search_button);
        mNewSearch.setOnClickListener(v -> switchToSearch());
    }

    private void createResultsText(String results, LinearLayout resultsContainer){
        TextView resultText = new TextView((this));

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 0, 0, 0);
        resultText.setLayoutParams(layoutParams);

        resultText.setText(results);
        resultText.setTextSize(15);
        resultText.setVisibility(View.VISIBLE);

        resultsContainer.addView(resultText);
        resultsContainer.setGravity(Gravity.CENTER);
    }

    private void switchToSearch(){
        Intent intent = new Intent(ResultsActivity.this, SearchActivity.class);
        startActivity(intent);
    }
}