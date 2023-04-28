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

    public BibleVerse(String vrs, String txt, String exp) {
        verse = vrs;
        text = txt;
        explanation = exp;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BibleVerse that = (BibleVerse) o;
        return Objects.equals(verse, that.verse) &&
                Objects.equals(text, that.text) &&
                Objects.equals(explanation, that.explanation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(verse, text, explanation);
    }

}
