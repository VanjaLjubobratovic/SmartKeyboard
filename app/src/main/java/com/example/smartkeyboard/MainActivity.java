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

import com.google.android.material.button.MaterialButton;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private MaterialButton generateBtn;
    private BufferedReader reader;
    private ArrayList<String> phrases;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);
        myToolbar.showOverflowMenu();

        try {
            InputStream inputStream = getResources().openRawResource(R.raw.phrases2);
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = reader.readLine();

            while(line != null) {
                phrases.add(line);
                line = reader.readLine();

                Log.d("FILE_READ", "Read line: " + line);
            }
        } catch (FileNotFoundException e) {
            Log.e("FILE_READ", "Phrases file not found!");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("FILE_READ", "Error reading file");
            e.printStackTrace();
        }

        generateBtn = findViewById(R.id.phraseGenerateBtn);

        generateBtn.setOnClickListener(view -> {

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
                Log.d(TAG, "onOptionsItemSelected: SESSION");
                return true;

            case R.id.sessionSettings:
                //TODO: Open session settings
                Log.d(TAG, "onOptionsItemSelected: SESSION");

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}