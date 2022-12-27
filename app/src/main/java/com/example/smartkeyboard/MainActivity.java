package com.example.smartkeyboard;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.renderscript.ScriptGroup;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
    private TextView timeTV, userTV, phraseCountTV, testKeyboardTV, handlingTV, nativeKeyboardTV, phraseResultTV;

    private Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);
        myToolbar.showOverflowMenu();

        generateBtn = findViewById(R.id.phraseGenerateBtn);
        userTV = findViewById(R.id.userTV);
        testKeyboardTV = findViewById(R.id.keyboardTV);
        handlingTV = findViewById(R.id.handlingTV);
        nativeKeyboardTV = findViewById(R.id.nativeKeyboardTV);
        timeTV = findViewById(R.id.timeTV);
        phraseCountTV = findViewById(R.id.countTV);
        phraseResultTV = findViewById(R.id.phraseResultsTV);
        phraseInput = findViewById(R.id.transcribeET);

        phrases = readPhrases();
        session = new Session();

        checkMemory();
        setSessionText();


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

    private void checkMemory() {
        SharedPreferences sharedPref = getSharedPreferences("sessionSettings", Context.MODE_PRIVATE);

        session.setUser(sharedPref.getString("username", "username"));
        session.setSessionID(sharedPref.getString("session_name", "session1"));
        session.setTestedKeyboard(sharedPref.getString("keyboard", "default"));
        session.setNumOfPhrases(Integer.parseInt(sharedPref.getString("number_of_phrases", "40")));
        session.setOrientation(sharedPref.getString("orientation", "PORTRAIT"));
        session.setTypingMode(sharedPref.getString("interaction", "TWO_THUMBS"));
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
                Intent myIntent = new Intent(MainActivity.this, SessionSettingsActivity.class);
                activityLauncher.launch(myIntent);
                Log.d(TAG, "onOptionsItemSelected: SESSION SETTINGS");
                return true;

            case R.id.initTestSession:
                initSessionConfirm();
                //initTestSession();
                Log.d(TAG, "onOptionsItemSelected: SESSION INIT");
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void initSessionConfirm() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(R.string.init_message)
                .setTitle(R.string.init_title)
                .setCancelable(false);

        builder.setPositiveButton(R.string.OK, (dialogInterface, i) -> {
            initTestSession();
        });

        builder.setNegativeButton(R.string.cancel, ((dialogInterface, i) -> {
        }));

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public ArrayList<String> readPhrases() {
        ArrayList<String> readLines = new ArrayList<>();
        try (InputStream is = getResources().openRawResource(R.raw.phrases2)){
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = reader.readLine();

            while(line != null) {
                readLines.add(line.toLowerCase());
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

        //TODO: give this to session class to handle
        String currentInfo = "Time: " + session.getTime() + "\n"
                + "Phrase given: " + generateBtn.getText() + "\n"
                + "Transcribed: " + session.getTranscribed().get(session.getSize() - 1).second + "\n"
                + "Raw input: " + session.getTranscribed().get(session.getSize() - 1).first + "\n"
                + session.getErrorsString(-1);

        phraseResultTV.setText(currentInfo);

        generateBtn.setText(phrases.get(session.getSize()));

        //Hide keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = MainActivity.this.getCurrentFocus();
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        //Check if done
        if (session.getSize() == session.getNumOfPhrases()) {
            phraseInput.setEnabled(false);
            generateBtn.setText("New session not yet started");
            Toast.makeText(this, "You have successfully finished with this session!", Toast.LENGTH_LONG).show();
        }
    }

    public void initTestSession() {
        //TODO: Finish initializing. Add checking if user is set.
        if(phrases != null) {
            phraseIndex = 0;
            Collections.shuffle(phrases);
            generateBtn.setText(phrases.get(phraseIndex));
            phraseInput.setEnabled(true);

            session.clearData();

            setSessionText();

            Toast.makeText(getApplicationContext(),"New session initialized", Toast.LENGTH_SHORT).show();
        } else {
            Log.e("RANDOM_PHRASE", "Phrases list empty!");
        }
    }

    public void setSessionText() {
        timeTV.setText(R.string.time);
        timeTV.append(" 00.00");

        phraseCountTV.setText(R.string.phrase_count);
        phraseCountTV.append(" 0/" + session.getNumOfPhrases());

        userTV.setText(R.string.user);
        userTV.append(" " + session.getUser());

        testKeyboardTV.setText(R.string.tested_keyboard);
        testKeyboardTV.append(" " + session.getTestedKeyboard());

        handlingTV.setText(R.string.handling);
        handlingTV.append(" " + session.getTypingMode().toString() + " @ " + session.getOrientation());

        //checkIfInputChanged();

        nativeKeyboardTV.setText(R.string.native_keyboard);
        nativeKeyboardTV.append(" " + session.getNativeKeyboard());

        phraseResultTV.setText("");
    }


    private void checkIfInputChanged() {
        //TODO: Fix
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        session.setNativeKeyboard(Settings.Secure.getString(getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD));
        nativeKeyboardTV.setText(R.string.native_keyboard);
        nativeKeyboardTV.append(" " + session.getNativeKeyboard());
    }

    ActivityResultLauncher<Intent> activityLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if(result.getResultCode() == 49) {
            Intent intent = result.getData();
            if (intent != null) {
                session = intent.getParcelableExtra("sessionDetails");
                Log.d("ACTIVITY_RESULT", session.getSessionID());
                initTestSession();
            } else {
                Toast.makeText(this, "Intent null", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Result code not 49" + " " + result.getResultCode(), Toast.LENGTH_SHORT).show();
        }
    });
}