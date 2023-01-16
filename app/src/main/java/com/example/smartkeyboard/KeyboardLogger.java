package com.example.smartkeyboard;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public abstract class KeyboardLogger {

    public static void writeToCSV(Context context, Session session) {
        String filename = session.getUser() + "-" + session.getSessionID()  +  ".txt";
        File file = new File(context.getFilesDir(), filename);

        try {
            String output;
            @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy@HH:mm:ss");
            String timestamp = df.format(new Date());

            HashMap<String, Double> stat = session.getStats().get(session.getSize() - 1);
            HashMap<String, String> phrases =  session.getTranscribed().get(session.getSize() - 1);

            output = timestamp + "-" + session.getUser() + "-" + session.getSessionID() + "||time-"
                    + session.getTime().replace(" ", "") + ";TER-" + String.format("%.2f", stat.get("TER"))
                    + ";WPM-" + String.format("%.2f", stat.get("WPM")) + ";AWPM-" + String.format("%.2f", stat.get("AWPM"))
                    + ";ORIGINAL-" +  phrases.get("ORIGINAL") + ";FINAL-" +  phrases.get("FINAL") + ";RAW-" + phrases.get("RAW")
                    + ";INDEX-" + (session.getSize() - 1) + "\n";

            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
            bw.write(output);
            bw.close();
            Log.d("FILE WRITER", "writeToCSV: SUCCESS");
        } catch (IOException e) {
            Log.d("FILE WRITER", "writeToCSV: IOException");
            e.printStackTrace();
        }
    }

    public static void writePointsToCSV(Context context, Session session) {
        String filename = "touches.csv";
        File file = new File(context.getFilesDir(), filename);

        try {
            StringBuilder output = new StringBuilder();

            for (int i = 0; i < session.getTranscribed().size() - 2; i++) {
                for(Point p : session.getTouchPoints().get(i)) {
                    output.append(p.x).append(",").append(p.y).append(",");
                }
                output.append(session.getTranscribed().get(i).get("RAW")).append("\n");
            }

            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
            bw.write(output.toString());
            bw.close();
            Log.d("FILE WRITER", "pointWriteToCSV: SUCCESS");
        } catch (IOException e) {
            Log.d("FILE WRITER", "pointWriteToCSV: IOException");
            e.printStackTrace();
        }
    }

    public static void uploadLog(Context context, Session session, StorageReference storageReference, Context mainAct){

        if(session.getSessionID().isEmpty() || session.getSessionID() == null){
            session.setSessionID("defaultSession");
        }
        if(session.getUser().isEmpty() || session.getUser() == null){
            session.setUser("defaultUser");
        }

        try {
            ProgressDialog progressDialog = new ProgressDialog(mainAct);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            //TODO:change this back to original
            String fileName = session.getUser() + "-" + session.getSessionID()  +  ".txt";
            //String fileName = "touches.csv";
            //String fileName = "keyboardConfig.txt";
            Uri filePath = Uri.fromFile(new File(context.getFilesDir(), fileName));

            StorageReference ref = storageReference.child("logFiles/" + fileName);

            ref.putFile(filePath)
                    .addOnSuccessListener(taskSnapshot -> {
                        progressDialog.dismiss();
                        Toast.makeText(context, "Log successfully uploaded!", Toast.LENGTH_SHORT).show(); })
                    .addOnFailureListener(
                            e -> {progressDialog.dismiss();
                                Toast.makeText(context, "Log upload failed!", Toast.LENGTH_SHORT).show(); })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + (int)progress + "%");
                        }
                    });
        }
        catch(Exception e){
            //progressDialog.dismiss();
            Toast.makeText(context, "Log upload failed! Reason: " + e, Toast.LENGTH_LONG).show();
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
