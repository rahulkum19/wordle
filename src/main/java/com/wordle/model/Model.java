package com.wordle.model;

import java.util.List;

public interface Model extends Subject {

    void startGame();
    void makeGuess(String guess);
    void useHint();
    void setModeStatus(MODE mode);
    MODE getModeStatus();
    STATUS getStatus();
    List<String> getGuesses();
    List<String> getGrayLetters();
    void addObserver(Observer o);
    int getMaxAttempts();
    void update();
    String getTargetWord();
    void triggerInvalidWord();
    boolean getAndClearInvalidWord();
    void setTargetWord(String target);
    void resetGame();
    boolean canUseHint();
    int getHintIndex();

    enum STATUS {
        START_GAME,
        END_GAME,
        IN_PROGRESS
    }

    enum MODE {
        EASY,
        MEDIUM,
        HARD
    }
}
