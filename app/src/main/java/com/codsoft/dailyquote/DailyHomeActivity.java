package com.codsoft.dailyquote;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.Response;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class DailyHomeActivity extends AppCompatActivity {

    private TextView randomQuoteTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_home);

        randomQuoteTextView = findViewById(R.id.randomQuoteTextView);

        // Add a button to navigate to the DailyQuoteActivity
        Button startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DailyHomeActivity.this, DailyQuoteActivity.class));
            }
        });

        // Load and display a random quote on the HomeActivity
        new FetchRandomQuoteTask().execute();
        // Add a button to share the current random quote
        Button shareButton = findViewById(R.id.shareButton);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareRandomQuote();
            }
        });
    }

    private void shareRandomQuote() {
        String currentRandomQuote = randomQuoteTextView.getText().toString();

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, currentRandomQuote);

        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }

    private class FetchRandomQuoteTask extends AsyncTask<Void, Void, String> {

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
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                try {
                    // Parse JSON response
                    JSONObject json = new JSONObject(result);
                    JSONObject contents = json.getJSONObject("contents");
                    JSONObject quotes = contents.getJSONArray("quotes").getJSONObject(0);

                    // Display the random quote
                    String randomQuote = quotes.getString("quote");
                    randomQuoteTextView.setText(randomQuote);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
