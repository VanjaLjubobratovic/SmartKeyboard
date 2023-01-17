package com.example.smartkeyboard;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

    private boolean somethingChanged = false;
    private String usernamePref, sessionPref, keyboardPref, phraseNumberPref, orientationPref, interactionPref;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkMemory();

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

    private void checkMemory() {
        SharedPreferences sharedPref = getSharedPreferences("sessionSettings", Context.MODE_PRIVATE);

        usernamePref = sharedPref.getString("username", "username");
        sessionPref = sharedPref.getString("session_name", "session1");
        keyboardPref = sharedPref.getString("keyboard", "default");
        phraseNumberPref = sharedPref.getString("number_of_phrases", "40");
        orientationPref = sharedPref.getString("orientation", "PORTRAIT");
        interactionPref = sharedPref.getString("interaction", "TWO_THUMBS");
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

        if(usernameString.equals(usernamePref) && sessionNameString.equals(sessionPref) && keyboardString.equals(keyboardPref) && numberOfPhrases.equals(phraseNumberPref)
        && orientation.toString().equals(orientationPref) && interaction.toString().equals(interactionPref)){
            somethingChanged = false;
        }
        else{
            somethingChanged = true;
        }

        if(somethingChanged){
            String finalUsernameString = usernameString;
            String finalSessionNameString = sessionNameString;
            String finalKeyboardString = keyboardString;
            String finalNumberOfPhrases = numberOfPhrases;

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Do you wish to save the changes").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    //Sending to Main Activity
                    session.setUser(finalUsernameString);
                    session.setSessionID(finalSessionNameString);
                    session.setTestedKeyboard(finalKeyboardString);
                    session.setNumOfPhrases(Integer.parseInt(finalNumberOfPhrases));
                    session.setOrientation(orientation);
                    session.setTypingMode(interaction);
                    intent.putExtra("sessionDetails", session);

                    //Saving to SP
                    SharedPreferences sharedPref = getSharedPreferences("sessionSettings", Context.MODE_PRIVATE);
                    SharedPreferences.Editor myEdit = sharedPref.edit();

                    myEdit.putString("username", finalUsernameString);
                    myEdit.putString("session_name", finalSessionNameString);
                    myEdit.putString("keyboard", finalKeyboardString);
                    myEdit.putString("number_of_phrases", finalNumberOfPhrases);
                    myEdit.putString("interaction", interaction.toString());
                    myEdit.putString("orientation", orientation.toString());
                    myEdit.commit();


                    //TODO malo uljepsat ovo
                    intent.putExtra("somethingChanged", somethingChanged);
                    setResult(49, intent);
                    finish();
                }
            })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            somethingChanged = false;
                            intent.putExtra("somethingChanged", somethingChanged);
                            setResult(49, intent);
                            finish();
                        }
                    }).setCancelable(false);
            AlertDialog alert = builder.create();
            alert.show();
        }
        else{
            finish();
        }
    }
}
