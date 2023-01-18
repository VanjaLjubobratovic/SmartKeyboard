package com.example.smartkeyboard;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {
    private final int CALIBRATION_PHRASES = 30;
    private MaterialButton generateBtn;
    private ArrayList<String> phrases;
    private int phraseIndex;
    private EditText phraseInput;
    private TextView timeTV, userTV, phraseCountTV, testKeyboardTV, handlingTV, nativeKeyboardTV, phraseResultTV;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private Session session;
    private BroadcastReceiver touchReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);
        myToolbar.showOverflowMenu();

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        generateBtn = findViewById(R.id.phraseGenerateBtn);
        userTV = findViewById(R.id.userTV);
        testKeyboardTV = findViewById(R.id.keyboardTV);
        handlingTV = findViewById(R.id.handlingTV);
        nativeKeyboardTV = findViewById(R.id.nativeKeyboardTV);
        timeTV = findViewById(R.id.timeTV);
        phraseCountTV = findViewById(R.id.countTV);
        phraseResultTV = findViewById(R.id.phraseResultsTV);
        phraseInput = findViewById(R.id.transcribeET);

        phraseInput.setEnabled(false);

        phrases = readPhrases(false);
        session = new Session();

        checkMemory();
        setSessionText();


        touchReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int x = intent.getIntExtra("x", 0);
                int y = intent.getIntExtra("y", 0);
                session.addTouchPoint(x, y);

                Log.d("TOUCH_BROADCAST", "TOUCH RECEIVED!\nX: " + x + "\nY: " + y);
            }
        };



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
                    //New line detected, input finished
                    checkIfInputChanged();
                    if(!session.transcribe(charSequence.toString(), generateBtn.getText().toString())) {
                        KeyboardLogger.writeToCSV(getApplicationContext(), session);
                        KeyboardLogger.writePointsToCSV(getApplicationContext(), session);
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
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(SmartInputService.KEYBOARD_TOUCH);
        registerReceiver(touchReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(touchReceiver);
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        final String TAG = "MENU";

        switch (item.getItemId()){
            case R.id.logSettings:
                if (session.isDone()) {
                    KeyboardLogger.uploadLog(getApplicationContext(), session, storageReference, this);
                }
                else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("You need to finish all phrases in the session to upload log files")
                            .setCancelable(false).setPositiveButton("OK", null);
                    AlertDialog alert = builder.create();
                    alert.show();
                }
                Log.d(TAG, "onOptionsItemSelected: LOG SETTINGS");
                return true;

            case R.id.sessionSettings:
                Intent myIntent = new Intent(MainActivity.this, SessionSettingsActivity.class);
                activityLauncher.launch(myIntent);
                Log.d(TAG, "onOptionsItemSelected: SESSION SETTINGS");
                return true;

            case R.id.initTestSession:
                initSessionConfirm();
                Log.d(TAG, "onOptionsItemSelected: SESSION INIT");
                return true;

            case R.id.bringUp:
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

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
            isTestDialog();
        });

        builder.setNegativeButton(R.string.cancel, ((dialogInterface, i) -> {
        }));

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void isTestDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Is this a calibration session?")
                .setTitle("Calibrate?")
                .setCancelable(false);

        builder.setPositiveButton("YES", (dialogInterface, i) -> {
            initTestSession(true);
        });

        builder.setNegativeButton("NO", ((dialogInterface, i) -> {
            initTestSession(false);
        }));

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public ArrayList<String> readPhrases(boolean isCalibration) {
        ArrayList<String> readLines = new ArrayList<>();
        int resource;

        //Loading different set depending on whether we are calibrating or not
        if(isCalibration) {
             resource = R.raw.calibrationphrases;
        } else {
            resource = R.raw.phrases2;
        }

        try (InputStream is = getResources().openRawResource(resource)){
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
                + "Transcribed: " + session.getTranscribed().get(session.getSize() - 1).get("FINAL") + "\n"
                + "Raw input: " + session.getTranscribed().get(session.getSize() - 1).get("RAW") + "\n"
                + session.getStatsString(-1);

        phraseResultTV.setText(currentInfo);

        //I was young and dumb when I was writing functions for getting sizes
        int size = session.getSize();
        if(size == session.getNumOfPhrases())
            size--;

        generateBtn.setText(phrases.get(size));

        session.putOriginalPhrase(generateBtn.getText().toString());

        //Hide keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = MainActivity.this.getCurrentFocus();
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        //Check if done
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (session.isDone()) {
                //KeyboardLogger.readTest(getApplicationContext(), session);
                phraseInput.setEnabled(false);
                generateBtn.setText("New session not yet started");
                Toast.makeText(this, "You have successfully finished with this session!", Toast.LENGTH_LONG).show();

                if(session.isCalibrationSession()) {
                    session.resizeKeyboard(getApplicationContext());
                }
            }
        }
    }

    public void initTestSession(boolean isCalibration) {
        phrases = readPhrases(isCalibration);
        if(phrases != null) {
            phraseIndex = 0;
            Collections.shuffle(phrases);
            generateBtn.setText(phrases.get(phraseIndex));
            phraseInput.setEnabled(true);

            session.clearData();
            session.setCalibrationSession(isCalibration);
            session.putOriginalPhrase(generateBtn.getText().toString());
            phraseInput.setText("");

            //In case of calibration session number of phrases is forced
            if(isCalibration) {
                session.setNumOfPhrases(CALIBRATION_PHRASES);
                SharedPreferences sharedPref = getSharedPreferences("sessionSettings", Context.MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPref.edit();
                myEdit.putString("number_of_phrases", Integer.toString(CALIBRATION_PHRASES));
            }

            setSessionText();
            mapKeys();

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

        checkIfInputChanged();

        nativeKeyboardTV.setText(R.string.native_keyboard);
        nativeKeyboardTV.append(" " + session.getNativeKeyboard());

        phraseResultTV.setText("");
    }

    private void mapKeys() {
        session.fillKeyMap(SmartInputService.readKeyboardConfig(getApplicationContext()));
    }

    private boolean isLeftEdge(int index) {
        return (index == 0 || index == 10 || index == 19 || index == 28);
    }


    private void checkIfInputChanged() {
        //TODO: Fix
        /*InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        String name = imm.getCurrentInputMethodSubtype().getDisplayName(getApplicationContext(), getPackageName(), getApplicationInfo()).toString();*/
        String name = Settings.Secure.getString(getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
        //Toast.makeText(this, name, Toast.LENGTH_SHORT).show();

        session.setNativeKeyboard(name);
        nativeKeyboardTV.setText(R.string.native_keyboard);
        nativeKeyboardTV.append(" " + session.getNativeKeyboard());
    }

    ActivityResultLauncher<Intent> activityLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if(result.getResultCode() == 49) {
            Intent intent = result.getData();
            if (intent != null) {
                boolean changed = intent.getBooleanExtra("somethingChanged", false);
                if(changed) {
                    session = intent.getParcelableExtra("sessionDetails");
                    Log.d("ACTIVITY_RESULT", session.getSessionID());
                    isTestDialog();
                }
            } else {
                Toast.makeText(this, "Intent null", Toast.LENGTH_SHORT).show();
            }
        } else {
           //Toast.makeText(this, "Result code not 49" + " " + result.getResultCode(), Toast.LENGTH_SHORT).show();
        }
    });
}