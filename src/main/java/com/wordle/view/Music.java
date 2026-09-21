package com.wordle.view;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class Music {
    private MediaPlayer mediaPlayer;

    public void play() {
        Media media = new Media(getClass().getResource("/music.mp3").toExternalForm());
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        mediaPlayer.play();
    }
}
