package com.example.smartkeyboard;

import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;

public class Session {
    private String user, sessionID, testedKeyboard, nativeKeyboard;
    private int numOfPhrases;
    private Orientation orientation;
    private TypingMode typingMode;

    //Pair<RawTranscription, FinalTranscription>
    private ArrayList<Pair<String, String>> transcribed;

    public Session() {
        this.transcribed = new ArrayList<>();
        this.numOfPhrases = 40;
        this.nextPhrase();
    }

    public int getNumOfPhrases() {
        return numOfPhrases;
    }

    public void setNumOfPhrases(int numOfPhrases) {
        this.numOfPhrases = numOfPhrases;
    }

    public ArrayList<Pair<String, String>> getTranscribed() {
        return transcribed;
    }

    public void setTranscribed(ArrayList<Pair<String, String>> transcribed) {
        this.transcribed = transcribed;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getSessionID() {
        return sessionID;
    }

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    public String getTestedKeyboard() {
        return testedKeyboard;
    }

    public void setTestedKeyboard(String testedKeyboard) {
        this.testedKeyboard = testedKeyboard;
    }

    public String getNativeKeyboard() {
        return nativeKeyboard;
    }

    public void setNativeKeyboard(String nativeKeyboard) {
        this.nativeKeyboard = nativeKeyboard;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    public TypingMode getTypingMode() {
        return typingMode;
    }

    public void setTypingMode(TypingMode typingMode) {
        this.typingMode = typingMode;
    }

    public int getSize() {
        if (transcribed.size() == 0) {
            return 0;
        } else {
            return transcribed.size() - 1;
        }
    }

    public void nextPhrase() {
        transcribed.add(new Pair<>("", ""));
    }

    public boolean transcribe(String newInput) {
        int index = getSize();
        StringBuilder sb = new StringBuilder(transcribed.get(index).first);

        if(newInput.length() != 0 && newInput.charAt(newInput.length() - 1) == '\n') {
            nextPhrase();
            return false;
        }

        if (transcribed.get(index).second.length() > newInput.length()) {
            sb.append("-");
        } else if (sb.length() == 0) {
            sb.append(newInput);
        } else {
            sb.append(newInput.charAt(newInput.length() - 1));
        }

        transcribed.set(index, new Pair<>(sb.toString(), newInput));

        Log.d("TRANSCRIBE", "RAW: " + transcribed.get(index).first + " || TRANSCRIBED: " + transcribed.get(index).second);
        return true;
    }

    public boolean isSet() {
        if (this.user != null
                && this.sessionID != null
                && this.testedKeyboard != null
                && this.nativeKeyboard != null
                && this.orientation != null
                && this.typingMode != null) {
            return true;
        } else {
            //TODO: this back to false after testing
            return true;
        }
    }
}
