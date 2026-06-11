package com.vocal.app.global.util;

public class NoteUtil {
    private static final String[] NOTE_NAMES = {"C","C#","D","D#","E","F","F#","G","G#","A","A#","B"};

    public static String toLabel(int midiNote) {
        return NOTE_NAMES[midiNote % 12] + ((midiNote / 12) - 1);
    }

    public static String adjustKey(String originalKey, int semitones) {
        for (int i = 0; i < NOTE_NAMES.length; i++) {
            if (NOTE_NAMES[i].equalsIgnoreCase(originalKey))
                return NOTE_NAMES[((i + semitones) % 12 + 12) % 12];
        }
        return originalKey;
    }
    private NoteUtil() {}
}
