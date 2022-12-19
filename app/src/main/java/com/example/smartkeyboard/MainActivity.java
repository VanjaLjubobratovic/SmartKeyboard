package com.example.smartkeyboard;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private MaterialButton generateBtn;
    /*private ArrayList<Integer> usedPhrases;
    private int phrase_index;*/

    private ArrayList<String> phrases;
    private int numOfPhrases, phraseIndex;
    private TextView timeTV, userTV, phraseCountTV, testKeyboardTV, handlingTV, nativeKeyboardTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);
        myToolbar.showOverflowMenu();

        generateBtn = findViewById(R.id.phraseGenerateBtn);

        timeTV = findViewById(R.id.timeTV);
        phraseCountTV = findViewById(R.id.countTV);

        //TODO: Read from settings
        numOfPhrases = 40;

        phrases = readPhrases();


        //TODO: Delete this when certain it is no longer necessary
        //usedPhrases = new ArrayList<>();
        /*generateBtn.setOnClickListener(view -> {
            if(phrases != null) {
                do {
                    Random r = new Random();
                    phrase_index = r.nextInt(phrases.size());
                } while (usedPhrases.contains(phrase_index));

                generateBtn.setText(phrases.get(phrase_index));
            } else {
                Log.e("RANDOM_PHRASE", "Phrases list empty!");
            }
        });*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        final String TAG = "MENU";

        switch (item.getItemId()){
            case R.id.logSettings:
                //TODO: Open logging settings
                Log.d(TAG, "onOptionsItemSelected: LOG SETTINGS");
                return true;

            case R.id.sessionSettings:
                //TODO: Open session settings
                Log.d(TAG, "onOptionsItemSelected: SESSION SETTINGS");

            case R.id.initTestSession:
                initTestSession();
                Log.d(TAG, "onOptionsItemSelected: SESSION INIT");

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public ArrayList<String> readPhrases() {
        ArrayList<String> readLines = new ArrayList<>();
        try (InputStream is = getResources().openRawResource(R.raw.phrases2)){
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = reader.readLine();

            while(line != null) {
                readLines.add(line);
                line = reader.readLine();
                //Log.d("FILE_READ", "Read line: " + line);
            }
            Log.d("FILE_READ", "Read num of lines: " + readLines.size());
        } catch (FileNotFoundException e) {
            Log.e("FILE_READ", "Phrases file not found!");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("FILE_READ", "Error reading file");
            e.printStackTrace();
        }

        return readLines;
    }

    public void initTestSession() {
        //TODO: Finish initializing. Add checking if user is set.
        if(phrases != null) {
            phraseIndex = 0;
            Collections.shuffle(phrases);
            generateBtn.setText(phrases.get(phraseIndex));

            timeTV.setText(R.string.time);
            timeTV.append(" 00:00");

            phraseCountTV.setText(R.string.phrase_count);
            phraseCountTV.append(" 0/" + numOfPhrases);

            Toast.makeText(getApplicationContext(),"New session initialized", Toast.LENGTH_SHORT).show();
        } else {
            Log.e("RANDOM_PHRASE", "Phrases list empty!");
        }
    }
}