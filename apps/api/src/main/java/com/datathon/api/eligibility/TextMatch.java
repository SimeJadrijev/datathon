package com.datathon.api.eligibility;

import java.util.Locale;

/** Compares quotes with the call text while ignoring spaces, punctuation, quote marks and letter case. */
final class TextMatch {

    private TextMatch() {
    }

    static String squash(String text) {
        StringBuilder squashed = new StringBuilder(text.length());
        text.codePoints()
                .filter(Character::isLetterOrDigit)
                .forEach(squashed::appendCodePoint);
        return squashed.toString().toLowerCase(Locale.ROOT);
    }
}
