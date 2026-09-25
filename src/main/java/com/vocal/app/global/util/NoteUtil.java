package com.vocal.app.global.util;

import java.util.Map;

public class NoteUtil {
    private static final String[] NOTE_NAMES = {"C","C#","D","D#","E","F","F#","G","G#","A","A#","B"};
    private static final Map<String, String> FLAT_TO_SHARP = Map.of(
        "Db", "C#", "Eb", "D#", "Gb", "F#", "Ab", "G#", "Bb", "A#"
    );

    public static String toLabel(int midiNote) {
        return NOTE_NAMES[midiNote % 12] + ((midiNote / 12) - 1);
    }

    public static String adjustKey(String originalKey, int semitones) {
        if (originalKey == null || originalKey.isBlank()) {   // ← 이 3줄 추가
        return null;
        }
        String normalizedKey = FLAT_TO_SHARP.getOrDefault(originalKey, originalKey);
        for (int i = 0; i < NOTE_NAMES.length; i++) {
            if (NOTE_NAMES[i].equalsIgnoreCase(normalizedKey))
                return NOTE_NAMES[((i + semitones) % 12 + 12) % 12];
        }
        return originalKey;
    }
    private NoteUtil() {}
}