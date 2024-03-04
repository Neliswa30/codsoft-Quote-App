package com.codsoft.dailyquote;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import org.json.JSONException;
import org.json.JSONObject;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.Response;
import java.io.IOException;

import java.util.concurrent.ExecutionException;

public class DailyQuoteActivity extends AppCompatActivity {

    private TextView quoteTextView;
    private TextView authorTextView;
    private TextView categoryTextView;
    private Button shareButton;
    private Button refreshButton;
    private Button saveButton;
    private ProgressBar loadingSpinner;

    // Add variables to store quote details
    private String currentQuote;
    private String currentAuthor;
    private String currentCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_quote);

        quoteTextView = findViewById(R.id.quoteTextView);
        authorTextView = findViewById(R.id.authorTextView);
        categoryTextView = findViewById(R.id.categoryTextView);
        shareButton = findViewById(R.id.shareButton);
        refreshButton = findViewById(R.id.refreshButton);
        saveButton = findViewById(R.id.saveButton);
        loadingSpinner = findViewById(R.id.loadingSpinner);

        // Set click listeners
        shareButton.setOnClickListener(v -> shareDailyQuote());
        refreshButton.setOnClickListener(v -> new FetchQuoteTask().execute());
        saveButton.setOnClickListener(v -> saveFavoriteQuote());

        // Execute AsyncTask to fetch and display the daily quote
        new FetchQuoteTask().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_daily_quote, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.menu_options) {
            showOptionsPopup(findViewById(R.id.menu_options));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showOptionsPopup(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.inflate(R.menu.menu_options_list);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                handlePopupMenuItemClick(menuItem.getItemId());
                return true;
            }
        });
        popupMenu.show();
    }

    private void handlePopupMenuItemClick(int itemId) {
            if (itemId ==R.id.menu_share){
                shareDailyQuote();
            }

            else if (itemId==R.id.menu_refresh){
                new FetchQuoteTask().execute();
            }

            else if (itemId==R.id.menu_save){
                saveFavoriteQuote();
            }
    }

    private void saveFavoriteQuote() {
        // Check if the quote is already a favorite
        if (isQuoteFavorite()) {
            Toast.makeText(this, "Quote already saved as a favorite", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "Quote saved to favorites!", Toast.LENGTH_SHORT).show();

        // Save the quote details in SharedPreferences as a favorite
        SharedPreferences sharedPreferences = getSharedPreferences("Favorites", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(currentQuote, currentAuthor + " - " + currentCategory);
        editor.apply();

        Toast.makeText(this, "Quote saved as a favorite", Toast.LENGTH_SHORT).show();
    }

    private boolean isQuoteFavorite() {
        SharedPreferences sharedPreferences = getSharedPreferences("Favorites", Context.MODE_PRIVATE);
        return sharedPreferences.contains(currentQuote);
    }

    private void shareDailyQuote() {
        String currentRandomQuote = quoteTextView.getText().toString() +
                "\n\n" + "Shared via Daily Quotes App";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, currentRandomQuote);
        String shareMessage = "\"" + currentQuote + "\" - " + currentAuthor + " (" + currentCategory + ")";
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }

    private class FetchQuoteTask extends AsyncTask<Void, Void, String> {

        @Override
 	protected String doInBackground(Void... voids) {
       	 AsyncHttpClient client = new DefaultAsyncHttpClient();

        try {
            // Create and execute an HTTP GET request using the Famous Quotes API
            Response response = client.prepareGet("https://famous-quotes4.p.rapidapi.com/random?category=all&count=2")
                    .setHeader("X-RapidAPI-Host", "famous-quotes4.p.rapidapi.com")
                    .setHeader("X-RapidAPI-Key", "b1e0468246mshd2984e1eac8300bp1dd906jsnea0b41e04e01")
                    .execute()
                    .get();  // Blocking call to get the result synchronously

            // Process the response as needed (e.g., convert to string)
            return response.getResponseBody();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;  // Handle errors appropriately
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

        @Override
        protected void onPreExecute() {
            // Show loading spinner before fetching the quote
            loadingSpinner.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            loadingSpinner.setVisibility(View.GONE); // Hide loading spinner

            if (result != null) {
                try {
                    // Parse JSON response
                    JSONObject json = new JSONObject(result);
                    JSONObject contents = json.getJSONObject("contents");
                    JSONObject quotes = contents.getJSONArray("quotes").getJSONObject(0);

                    // Store the current quote details
                    currentQuote = quotes.getString("quote");
                    currentAuthor = quotes.getString("author");
                    currentCategory = quotes.getString("category");

                    // Display the quote
                    quoteTextView.setText(currentQuote);
                    authorTextView.setText("Author: " + currentAuthor);
                    categoryTextView.setText("Category: " + currentCategory);
                } catch (JSONException e) {
                    e.printStackTrace();
                    showError("Error parsing JSON");
                }
            } else {
                showError("Failed to fetch quote. Please try again later.");
            }
            
        }
    }

    private void showError(String errorMessage) {
        // Show an error message to the user
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }
}
