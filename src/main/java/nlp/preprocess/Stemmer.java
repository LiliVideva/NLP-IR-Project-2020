package nlp.preprocess;

import java.util.List;

public class Stemmer {
    private char[] characters;
    private int firstIndex;
    private int lastIndex;
    private int tempFirstIndex;
    private int tempLastIndex;

    public Stemmer() {
        characters = new char[50];
        firstIndex = 0;
        lastIndex = 0;
    }

    public String toString() {
        return new String(characters, 0, lastIndex);
    }

    private boolean hasConsonant(int i) {
        List<Character> vowels = List.of('a', 'e', 'i', 'o', 'u');
        char letter = characters[i];

        return !vowels.contains(letter) && ((letter != 'y') || ((i == 0) || !hasConsonant(i - 1)));
    }

    private int countConsonantSequence() {
        int count = 0;
        int i = 0;
        while (true) {
            if (i > tempFirstIndex) {
                return count;
            }
            if (!hasConsonant(i)) {
                break;
            }
            i++;
        }
        i++;
        while (true) {
            while (true) {
                if (i > tempFirstIndex) {
                    return count;
                }
                if (!hasConsonant(i)) {
                    break;
                }
                i++;
            }
            i++;
            count++;
            while (true) {
                if (i > tempFirstIndex) {
                    return count;
                }
                if (!hasConsonant(i)) {
                    break;
                }
                i++;
            }
            i++;
        }
    }

    private boolean hasVowel() {
        for (int i = 0; i <= tempFirstIndex; i++) {
            if (!hasConsonant(i)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasDoubleConsonant(int i) {
        return (i >= 1) && ((characters[i] == characters[i - 1]) && hasConsonant(i));
    }

    private boolean hasConsonantVowelConsonant(int i) {
        char letter = characters[i];
        return (i >= 2 && hasConsonant(i) && !hasConsonant(i - 1) && hasConsonant(i - 2)) && (letter != 'w' && letter != 'x' && letter != 'y');
    }

    private boolean hasEnding(String word) {
        int wordLength = word.length();
        int initialWordIndex = tempLastIndex - wordLength + 1;
        if (initialWordIndex < 0) return false;
        for (int i = 0; i < wordLength; i++) {
            if (characters[initialWordIndex + i] != word.charAt(i)) {
                return false;
            }
        }
        tempFirstIndex = tempLastIndex - wordLength;
        return true;
    }

    private void updateLastCharacters(String word) {
        int wordLength = word.length();
        int initialWordIndex = tempFirstIndex + 1;
        for (int i = 0; i < wordLength; i++) {
            characters[initialWordIndex + i] = word.charAt(i);
        }
        tempLastIndex = tempFirstIndex + wordLength;
    }

    private void triggerUpdate(String s) {
        if (countConsonantSequence() > 0) {
            updateLastCharacters(s);
        }
    }

    private void removePlural() {
        if (characters[tempLastIndex] == 's') {
            if (hasEnding("sses")) {
                tempLastIndex -= 2;
            } else if (hasEnding("ies")) {
                updateLastCharacters("i");
            } else if (characters[tempLastIndex - 1] != 's') {
                tempLastIndex--;
            }
        }

        if (hasEnding("eed")) {
            if (countConsonantSequence() > 0) {
                tempLastIndex--;
            }
        } else if ((hasEnding("ed") || hasEnding("ing")) && hasVowel()) {
            tempLastIndex = tempFirstIndex;

            if (hasEnding("at")) {
                updateLastCharacters("ate");
            } else if (hasEnding("bl")) {
                updateLastCharacters("ble");
            } else if (hasEnding("iz")) {
                updateLastCharacters("ize");
            } else if (hasDoubleConsonant(tempLastIndex)) {
                tempLastIndex--;
                char letter = characters[tempLastIndex];
                if (letter == 'l' || letter == 's' || letter == 'z') {
                    tempLastIndex++;
                }
            } else if (countConsonantSequence() == 1 && hasConsonantVowelConsonant(tempLastIndex)) {
                updateLastCharacters("e");
            }
        }
    }

    private void substitudeY() {
        if (hasEnding("y") && hasVowel()) {
            characters[tempLastIndex] = 'i';
        }
    }

    private void modifySuffices() {
        if (tempLastIndex == 0) {
            return;
        }
        switch (characters[tempLastIndex - 1]) {
            case 'a':
                if (hasEnding("ational")) {
                    triggerUpdate("ate");
                } else if (hasEnding("tional")) {
                    triggerUpdate("tion");
                }
                break;
            case 'c':
                if (hasEnding("enci")) {
                    triggerUpdate("ence");
                } else if (hasEnding("anci")) {
                    triggerUpdate("ance");
                }
                break;
            case 'e':
                if (hasEnding("izer")) {
                    triggerUpdate("ize");
                }
                break;
            case 'l':
                if (hasEnding("bli")) {
                    triggerUpdate("ble");
                } else if (hasEnding("alli")) {
                    triggerUpdate("al");
                } else if (hasEnding("entli")) {
                    triggerUpdate("ent");
                } else if (hasEnding("eli")) {
                    triggerUpdate("e");
                } else if (hasEnding("ousli")) {
                    triggerUpdate("ous");
                }
                break;
            case 'o':
                if (hasEnding("ization")) {
                    triggerUpdate("ize");
                } else if (hasEnding("ation")) {
                    triggerUpdate("ate");
                } else if (hasEnding("ator")) {
                    triggerUpdate("ate");
                }
                break;
            case 's':
                if (hasEnding("alism")) {
                    triggerUpdate("al");
                } else if (hasEnding("iveness")) {
                    triggerUpdate("ive");
                } else if (hasEnding("fulness")) {
                    triggerUpdate("ful");
                } else if (hasEnding("ousness")) {
                    triggerUpdate("ous");
                }
                break;
            case 't':
                if (hasEnding("aliti")) {
                    triggerUpdate("al");
                } else if (hasEnding("iviti")) {
                    triggerUpdate("ive");
                } else if (hasEnding("biliti")) {
                    triggerUpdate("ble");
                }
                break;
            case 'g':
                if (hasEnding("logi")) {
                    triggerUpdate("log");
                }
                break;
        }
    }

    private void manageIcAndFullAndNess() {
        switch (characters[tempLastIndex]) {
            case 'e':
                if (hasEnding("icate")) {
                    triggerUpdate("ic");
                } else if (hasEnding("ative")) {
                    triggerUpdate("");
                } else if (hasEnding("alize")) {
                    triggerUpdate("al");
                }
                break;
            case 'i':
                if (hasEnding("iciti")) {
                    triggerUpdate("ic");
                }
                break;
            case 'l':
                if (hasEnding("ical")) {
                    triggerUpdate("ic");
                } else if (hasEnding("ful")) {
                    triggerUpdate("");
                }
                break;
            case 's':
                if (hasEnding("ness")) {
                    triggerUpdate("");
                }
                break;
        }
    }

    private void removeAntAndEnce() {
        if (tempLastIndex == 0) {
            return;
        }
        switch (characters[tempLastIndex - 1]) {
            case 'a':
                if (hasEnding("al")) {
                    break;
                }
                return;
            case 'c':
                if (hasEnding("ance") || hasEnding("ence")) {
                    break;
                }
                return;
            case 'e':
                if (hasEnding("er")) {
                    break;
                }
                return;
            case 'i':
                if (hasEnding("ic")) {
                    break;
                }
                return;
            case 'l':
                if (hasEnding("able") || hasEnding("ible")) {
                    break;
                }
                return;
            case 'n':
                if (hasEnding("ant") || hasEnding("ement") || hasEnding("ment") || hasEnding("ent")) {
                    break;
                }
                return;
            case 'o':
                if ((hasEnding("ion") && tempFirstIndex >= 0 && (characters[tempFirstIndex] == 's' || characters[tempFirstIndex] == 't')) || hasEnding("ou"))
                    break;
                return;
            case 's':
                if (hasEnding("ism")) {
                    break;
                }
                return;
            case 't':
                if (hasEnding("ate") || hasEnding("iti")) break;
                return;
            case 'u':
                if (hasEnding("ous")) {
                    break;
                }
                return;
            case 'v':
                if (hasEnding("ive")) {
                    break;
                }
                return;
            case 'z':
                if (hasEnding("ize")) {
                    break;
                }
                return;
            default:
                return;
        }
        if (countConsonantSequence() > 1) {
            tempLastIndex = tempFirstIndex;
        }
    }

    private void removeEndingE() {
        tempFirstIndex = tempLastIndex;

        if (characters[tempLastIndex] == 'e') {
            int count = countConsonantSequence();

            if (count > 1 || count == 1 && !hasConsonantVowelConsonant(tempLastIndex - 1)) {
                tempLastIndex--;
            }
        }
        if (characters[tempLastIndex] == 'l' && hasDoubleConsonant(tempLastIndex) && countConsonantSequence() > 1) {
            tempLastIndex--;
        }
    }

    public void stem() {
        tempLastIndex = firstIndex - 1;

        if (tempLastIndex > 1) {
            removePlural();
            substitudeY();
            modifySuffices();
            manageIcAndFullAndNess();
            removeAntAndEnce();
            removeEndingE();
        }
        lastIndex = tempLastIndex + 1;
        firstIndex = 0;
    }

    public CharSequence stem(CharSequence word) {
        characters = new char[word.length()];
        char[] arr = word.toString().toCharArray();
        System.arraycopy(arr, 0, characters, 0, characters.length);
        firstIndex = arr.length;
        stem();
        return toString();
    }
}

