package com.wordle.controller;

import com.wordle.model.Model;
import okhttp3.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;

public class ControllerImpl implements Controller {
    private final Model model;
    private String input;
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();
    private String loadEasy = "";
    private String loadMedium = "";
    private String loadHard = "";

    private static final Pattern OBJECT_PATTERN = Pattern.compile("\\{[^}]*\\}");
    private static final Pattern WORD_PATTERN = Pattern.compile("\"word\":\"([^\"]+)\"");
    private static final Pattern FREQ_PATTERN = Pattern.compile("f:([0-9.]+)");

    public ControllerImpl(Model model) {
        this.model = model;
        this.input = "";
    }

    @Override
    public void setEasyMode() {
        model.setModeStatus(Model.MODE.EASY);
    }

    @Override
    public void setMediumMode() {
        model.setModeStatus(Model.MODE.MEDIUM);
    }

    @Override
    public void setHardMode() {
        model.setModeStatus(Model.MODE.HARD);
    }

    @Override
    public void startGame() {
        input = "";
        model.startGame();
    }

    @Override
    public void processKeyPress(String key){
        if(model.getTargetWord().isEmpty()){
            return;
        }
        if(model.getStatus() != Model.STATUS.IN_PROGRESS){
            return;
        }
        if(key.equals("ENTER")){
            inputIsEnter();
        }
        else if(key.equals("BACK_SPACE")){
            inputIsBackspace();
        }
        else if(key.length() == 1 && Character.isLetter(key.charAt(0))){
            inputisLetter(key.toUpperCase());
        }
    }

    private void inputisLetter(String key){
        if(input.length() < 5){
            input += key;
            model.update();
        }
    }

    private void inputIsBackspace(){
        if(input.length() > 0){
            input = input.substring(0, input.length()-1);
            model.update();
        }
    }

    private void inputIsEnter(){
        if(input.length() == 5){
            validateGuess(input);
        }
    }

    private void validateGuess(String guess){
        HttpUrl url = HttpUrl.parse("https://api.datamuse.com/words")
                .newBuilder()
                .addQueryParameter("sp", guess.toLowerCase())
                .addQueryParameter("max", "1")
                .build();
        okhttp3.Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            public void onFailure(Call call, IOException e){
                e.printStackTrace();
                Platform.runLater(() -> model.triggerInvalidWord());
            }

            public void onResponse(Call call, Response response) throws IOException{
                try(response){
                    if(response.isSuccessful()){
                        String body = response.body().string();
                        Matcher m = WORD_PATTERN.matcher(body);
                        boolean valid = m.find() && m.group(1).equalsIgnoreCase(guess);
                        Platform.runLater(() -> {
                            if(valid){
                                model.makeGuess(guess);
                                input = "";
                            } else{
                                model.triggerInvalidWord();
                            }
                        });
                    } else{
                        Platform.runLater(() -> {
                            model.triggerInvalidWord();
                        });
                    }
                }
            }
        });
    }

    @Override
    public void initializeWords(){
        loader(Model.MODE.EASY);
        loader(Model.MODE.MEDIUM);
        loader(Model.MODE.HARD);
    }

    private void loader(Model.MODE m){
        HttpUrl httpUrl = HttpUrl.parse("https://api.datamuse.com/words")
                .newBuilder()
                .addQueryParameter("sp", "?????")
                .addQueryParameter("md", "f")
                .addQueryParameter("max", "500")
                .build();
        okhttp3.Request request = new Request.Builder()
                .url(httpUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException{
                if(response.isSuccessful()){
                    String body = response.body().string();
                    String randomWord = pickWordForDifficulty(body, m);

                    if(randomWord == null){
                        return;
                    }

                    if(m == Model.MODE.EASY){
                        loadEasy = randomWord;
                    } else if(m == Model.MODE.MEDIUM){
                        loadMedium = randomWord;
                    }else{
                        loadHard = randomWord;
                    }
                    Platform.runLater(() -> {
                        if(model.getStatus() == Model.STATUS.START_GAME || model.getTargetWord().isEmpty()){
                            if(m==model.getModeStatus()){
                                getTargetWord();
                            }
                        }
                    });
                }
            }
            @Override
            public void onFailure(Call call, IOException e){
                e.printStackTrace();
            }
        });
    }

    private String pickWordForDifficulty(String json, Model.MODE m){
        List<String[]> wordFreqPairs = new ArrayList<>();
        Matcher objMatcher = OBJECT_PATTERN.matcher(json);
        while(objMatcher.find()){
            String obj = objMatcher.group();
            Matcher wordMatcher = WORD_PATTERN.matcher(obj);
            Matcher freqMatcher = FREQ_PATTERN.matcher(obj);
            if(wordMatcher.find() && freqMatcher.find()){
                String word = wordMatcher.group(1);
                String freq = freqMatcher.group(1);
                if(word.length() == 5){
                    wordFreqPairs.add(new String[]{word, freq});
                }
            }
        }
        if(wordFreqPairs.isEmpty()){
            return null;
        }

        wordFreqPairs.sort((a, b) -> Double.compare(Double.parseDouble(b[1]), Double.parseDouble(a[1])));

        int size = wordFreqPairs.size();
        int startIdx;
        int endIdx;
        if(m == Model.MODE.EASY){
            startIdx = 0;
            endIdx = Math.max(1, (int)(size * 0.2));
        } else if(m == Model.MODE.MEDIUM){
            startIdx = (int)(size * 0.15);
            endIdx = Math.max(startIdx + 1, (int)(size * 0.35));
        } else{
            startIdx = (int)(size * 0.8);
            endIdx = size;
        }
        endIdx = Math.min(endIdx, size);
        if(startIdx >= endIdx){
            startIdx = 0;
            endIdx = size;
        }

        int randomIndex = startIdx + (int)(Math.random() * (endIdx - startIdx));
        return wordFreqPairs.get(randomIndex)[0];
    }

    public void getTargetWord(){
        model.resetGame();
        input = "";
        String targetWord = null;

        if(model.getModeStatus() == Model.MODE.EASY){
            targetWord = loadEasy;
        } else if(model.getModeStatus() == Model.MODE.MEDIUM){
            targetWord = loadMedium;
        } else{
            targetWord = loadHard;
        }

        if(targetWord != null && !targetWord.isEmpty()){
            model.setTargetWord(targetWord);
            if(model.getModeStatus() == Model.MODE.EASY){
                loadEasy = "";
            } else if(model.getModeStatus() == Model.MODE.MEDIUM){
                loadMedium = "";
            } else{
                loadHard = "";
            }
            loader(model.getModeStatus());
        }
    }

    @Override
    public void useHint(){
        model.useHint();
    }

    @Override
    public String getInput(){
        return input;
    }
}
