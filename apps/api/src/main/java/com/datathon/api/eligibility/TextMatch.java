package com.datathon.api.eligibility;

import java.util.Locale;

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
