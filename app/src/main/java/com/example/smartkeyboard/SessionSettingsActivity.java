package com.example.smartkeyboard;

import android.content.Intent;
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
        session.setUser(username.getText().toString());
        session.setSessionID(sessionName.getText().toString());
        session.setTestedKeyboard(keyboard.getText().toString());
        session.setNumOfPhrases(Integer.parseInt(phraseNumber.getText().toString()));
        session.setOrientation(orientation);
        session.setTypingMode(interaction);
        intent.putExtra("sessionDetails", session);
        setResult(49, intent);
        super.onBackPressed();
    }
}
