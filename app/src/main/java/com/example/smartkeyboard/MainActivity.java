package com.example.smartkeyboard;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
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
    private ArrayList<String> phrases;
    private int phraseIndex;
    private EditText phraseInput;
    private TextView timeTV, userTV, phraseCountTV, testKeyboardTV, handlingTV, nativeKeyboardTV;

    private Session session;

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

        phrases = readPhrases();
        session = new Session();

        phraseInput = findViewById(R.id.transcribeET);

        phraseInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!session.isSet()) {
                    Toast.makeText(MainActivity.this, "New session not started", Toast.LENGTH_SHORT).show();
                    phraseInput.getText().clear();
                } else {
                    if(!session.transcribe(charSequence.toString())) {
                        phraseInput.getText().clear();
                        nextPhrase();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
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

    public void nextPhrase() {
        phraseCountTV.setText(R.string.phrase_count);
        phraseCountTV.append(" " + session.getSize() + "/" + session.getNumOfPhrases());

        timeTV.setText(R.string.time);
        timeTV.append(" " + session.getTime());

        generateBtn.setText(phrases.get(session.getSize()));
    }

    public void initTestSession() {
        //TODO: Finish initializing. Add checking if user is set.
        if(phrases != null) {
            phraseIndex = 0;
            Collections.shuffle(phrases);
            generateBtn.setText(phrases.get(phraseIndex));

            session = new Session();

            timeTV.setText(R.string.time);
            timeTV.append(" 00:00");

            phraseCountTV.setText(R.string.phrase_count);
            phraseCountTV.append(" 0/" + session.getNumOfPhrases());

            Toast.makeText(getApplicationContext(),"New session initialized", Toast.LENGTH_SHORT).show();
        } else {
            Log.e("RANDOM_PHRASE", "Phrases list empty!");
        }
    }
}