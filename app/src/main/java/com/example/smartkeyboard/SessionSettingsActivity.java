package com.example.smartkeyboard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


public class SessionSettingsActivity extends AppCompatActivity {
    private EditText username, sessionName, keyboard, phraseNumber;
    private RadioGroup interactionGroup, orientationGroup;
    private RadioButton oneHand, twoThumb, cradle, portrait, landscape;
    private TypingMode interaction;
    private Orientation orientation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);

        String usernamePref, sessionPref, keyboardPref, phraseNumberPref, orientationPref, interactionPref;

        usernamePref = sharedPref.getString("username", "username");
        sessionPref = sharedPref.getString("session_name", "session1");
        keyboardPref = sharedPref.getString("keyboard", "default");
        phraseNumberPref = sharedPref.getString("number_of_phrases", "40");
        orientationPref = sharedPref.getString("orientation", "PORTRAIT");
        interactionPref = sharedPref.getString("interaction", "TWO_THUMBS");


        setContentView(R.layout.session_settings);
        username = findViewById(R.id.txtUsername);
        sessionName = findViewById(R.id.txtSessionName);
        keyboard = findViewById(R.id.txtKeyboard);
        phraseNumber = findViewById(R.id.txtNumberOfPhrases);

        interactionGroup = findViewById(R.id.radioGroupInteraction);
        orientationGroup = findViewById(R.id.radioGroupOrientation);

        oneHand = findViewById(R.id.radioOneHand);
        twoThumb = findViewById(R.id.radioTwoThumbs);
        cradle = findViewById(R.id.radioCradle);
        portrait = findViewById(R.id.radioPortrait);
        landscape = findViewById(R.id.radioLandscape);

        interaction = TypingMode.TWO_THUMBS;
        orientation = Orientation.PORTRAIT;

        username.setText(usernamePref);
        sessionName.setText(sessionPref);
        keyboard.setText(keyboardPref);
        phraseNumber.setText(phraseNumberPref);

        switch(interactionPref){
            case("TWO_THUMBS"): twoThumb.setChecked(true);
            break;
            case("ONE_HAND"): oneHand.setChecked(true);
            break;
            case("CRADLING"): cradle.setChecked(true);
            break;
        }

        switch(orientationPref){
            case("PORTRAIT"): portrait.setChecked(true);
                break;
            case("LANDSCAPE"): landscape.setChecked(true);
                break;
        }

        interactionGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch(i){
                    case R.id.radioOneHand:
                        Toast.makeText(SessionSettingsActivity.this, "One hand selected", Toast.LENGTH_SHORT).show();
                        interaction = TypingMode.ONE_HAND;
                        break;
                    case R.id.radioTwoThumbs:
                        Toast.makeText(SessionSettingsActivity.this, "Two thumbs selected", Toast.LENGTH_SHORT).show();
                        interaction = TypingMode.TWO_THUMBS;
                        break;
                    case R.id.radioCradle:
                        Toast.makeText(SessionSettingsActivity.this, "Cradle selected", Toast.LENGTH_SHORT).show();
                        interaction = TypingMode.CRADLING;
                        break;
                }
            }
        });

        orientationGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch(i){
                    case R.id.radioPortrait:
                        Toast.makeText(SessionSettingsActivity.this, "Portrait selected", Toast.LENGTH_SHORT).show();
                        orientation = Orientation.PORTRAIT;
                        break;
                    case R.id.radioLandscape:
                        Toast.makeText(SessionSettingsActivity.this, "Landscape selected", Toast.LENGTH_SHORT).show();
                        orientation = Orientation.LANDSCAPE;
                        break;
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        Session session = new Session();

        String usernameString = username.getText().toString();
        String sessionNameString = sessionName.getText().toString();
        String keyboardString = keyboard.getText().toString();
        String numberOfPhrases = phraseNumber.getText().toString();

        if(usernameString.isEmpty()){
            usernameString = "username";
        }
        if(sessionNameString.isEmpty()){
            sessionNameString = "session1";
        }
        if(keyboardString.isEmpty()){
            keyboardString = "default";
        }
        if(numberOfPhrases.isEmpty()){
            numberOfPhrases = "40";
        }

        session.setUser(usernameString);
        session.setSessionID(sessionNameString);
        session.setTestedKeyboard(keyboardString);
        session.setNumOfPhrases(Integer.parseInt(numberOfPhrases));
        session.setOrientation(orientation);
        session.setTypingMode(interaction);
        intent.putExtra("sessionDetails", session);
        setResult(49, intent);

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPref.edit();

        myEdit.putString("username", usernameString);
        myEdit.putString("session_name", sessionNameString);
        myEdit.putString("keyboard", keyboardString);
        myEdit.putString("number_of_phrases", numberOfPhrases);
        myEdit.putString("interaction", interaction.toString());
        myEdit.putString("orientation", orientation.toString());

        myEdit.commit();

        super.onBackPressed();
    }
}
