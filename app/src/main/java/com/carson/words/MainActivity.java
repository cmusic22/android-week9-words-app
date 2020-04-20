package com.carson.words;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "DICTIONARY";

    Button searchButton;
    EditText enterWord;
    TextView wordDefinition;
    ImageView wordImage;
    TextView wordPronunciation;
    ProgressBar loading;

    WordService wordService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://owlbot.info/api/v3/dictionary/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        wordService = retrofit.create(WordService.class);

        searchButton = findViewById(R.id.search_button);
        enterWord = findViewById(R.id.enter_word);
        wordDefinition = findViewById(R.id.word_definition);
        wordImage = findViewById(R.id.word_image);
        wordPronunciation = findViewById(R.id.word_pronunciation);
        loading = findViewById(R.id.loading);

        setSearchEnabled(true);

        // Hide ImageView until an image is available
        wordImage.setVisibility(GONE);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String word = enterWord.getText().toString();
                if (word.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Enter a word", Toast.LENGTH_SHORT).show();
                    return;
                }

                hideKeyboard();
                setSearchEnabled(false);
                getDefinitionForWord(word);
            }
        });
    }

    private void getDefinitionForWord(final String word) {
        wordService.getDefinition(word, BuildConfig.OWLBOT_TOKEN).enqueue(new Callback<Word>() {
            @Override
            public void onResponse(Call<Word> call, Response<Word> response) {
                setSearchEnabled(true);

                Word wordResponse = response.body();
                Log.d(TAG, "Word Response: " + wordResponse);
                if (wordResponse != null && wordResponse.definitions.length >= 1) {
                    String pronounce = wordResponse.pronunciation;
                    wordPronunciation.setText(pronounce);

                    wordDefinition.setText(wordResponse.definitions[0].definition);

                    String imageURL = wordResponse.definitions[0].image_url;
                    if (imageURL == null || imageURL.isEmpty()){
                        wordImage.setVisibility(GONE); //Hide, if no image available
                    } else {
                        wordImage.setVisibility(VISIBLE);
                        Picasso.get().load(imageURL).fit().centerCrop().into(wordImage);
                    }
                } else {
                    setSearchEnabled(true);
                    Log.d(TAG, "Search for " + word + " did not return any definitions");
                    Toast.makeText(MainActivity.this, "Notefinitions found for " + word, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Word> call, Throwable t) {
                Log.e(TAG, "Error fetching definition", t);
                Toast.makeText(MainActivity.this, "unable to fetch definition", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setSearchEnabled(boolean isEnabled){
        loading.setVisibility( isEnabled ? GONE : VISIBLE);
        searchButton.setEnabled(isEnabled);
        enterWord.setEnabled(isEnabled);

    }

    private void hideKeyboard() {
        View mainView = findViewById(android.R.id.content);
        InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(mainView.getWindowToken(), 0);
    }

}
