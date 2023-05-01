package com.example.soulscript;

import org.json.JSONObject;

import java.util.Objects;

public class BibleVerse {
    private String verse;
    private String text;
    private String explanation;

    public BibleVerse() {}

    public String getVerse() {
        return verse;
    }

    public String getText() {
        return text;
    }

    public String getExplanation() {
        return explanation;
    }

    // Constructor for creating a BibleVerse object
    public BibleVerse(String vrs, String txt, String exp) {
        verse = vrs;
        text = txt;
        explanation = exp;
    }

    // This method returns the BibleVerse object in JSON format.
    public String toJsonString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("verse", verse);
            jsonObject.put("text", text);
            jsonObject.put("explanation", explanation);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

}
