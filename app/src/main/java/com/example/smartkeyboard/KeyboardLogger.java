package com.example.smartkeyboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public abstract class KeyboardLogger {

    public static void writeToCSV(Context context, Session session) {
        String filename = session.getSessionID() + "-" + session.getUser() + ".txt";
        File file = new File(context.getFilesDir(), filename);

        try {
            String output;
            @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy@HH:mm:ss");
            String timestamp = df.format(new Date());

            HashMap<String, Double> stat = session.getStats().get(session.getSize() - 1);

            output = timestamp + "-" + session.getUser() + "-" + session.getSessionID() + "||time-"
                    + session.getTime().replace(" ", "") + ";TER-" + String.format("%.2f", stat.get("TER"))
                    + ";WPM-" + String.format("%.2f", stat.get("WPM")) + ";AWPM-" + String.format("%.2f", stat.get("AWPM")) + "\n";

            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
            bw.write(output);
            bw.close();
            Log.d("FILE WRITER", "writeToCSV: SUCCESS");
        } catch (IOException e) {
            Log.d("FILE WRITER", "writeToCSV: IOException");
            e.printStackTrace();
        }
    }

    public static void readTest(Context context, Session session) {
        try {
            String filename = session.getSessionID() + "-" + session.getUser() + ".txt";
            File file = new File(context.getFilesDir(), filename);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String read;
            StringBuilder sb = new StringBuilder("");

            while((read = br.readLine()) != null) {
                sb.append(read);
            }
            Log.d("FILE WRITER", "readFromCSV: " + sb);
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
