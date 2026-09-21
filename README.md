# Wordle (JavaFX)

A desktop clone of the popular word-guessing game **Wordle**, built with **Java 21**, **JavaFX**, and an asynchronous **Model-View-Controller (MVC)** architecture. The game features real-time dictionary validation and dynamic word selection using the **Datamuse API**, customizable difficulty tiers, an intelligent hint system, on-screen and physical keyboard support, and smooth UI animations.

---

## 🎮 Gameplay & Rules

The objective is to guess a hidden 5-letter word within a limited number of attempts:
- Each guess must be a valid 5-letter word recognized by the dictionary.
- After submitting a guess, tiles change color to give feedback:
  - 🟩 **Green**: The letter is correct and in the correct position.
  - 🟨 **Yellow**: The letter is in the word, but in a different position.
  - ⬜ **Gray**: The letter is not in the target word.

---

## ✨ Features

- **🌐 Dynamic Word Generation**: Fetches target words directly from the [Datamuse API](https://www.datamuse.com/api/) based on real-world English word frequency metrics.
- **🔍 Live Dictionary Validation**: Verifies player guesses against the Datamuse API before evaluation to ensure legitimate English words.
- **🎯 3 Difficulty Modes**:
  - **Easy**: 7 attempts, high-frequency/familiar words (top 20% frequency), on-screen keyboard enabled, hint system available.
  - **Medium**: 6 attempts, moderately frequent words (15%–35% frequency), on-screen keyboard enabled, hint system available.
  - **Hard**: 5 attempts, rare/challenging words (bottom 20% frequency), on-screen keyboard disabled (physical typing only), hints disabled, and strict hard-mode rules (cannot reuse gray letters).
- **💡 Smart Hint System**: When playing on Easy or Medium, if more than half of the attempts have been used and 2 or fewer correct positions are found, a **"REVEAL HINT"** button appears to reveal an unsolved letter in its correct spot.
- **⌨️ Dual Input Support**: Use either the mouse on the virtual color-coded keyboard or standard physical keyboard input (letters, `Backspace`, `Enter`).
- **📳 Shake Animation**: Visual shake animation triggers on invalid or unrecognized word submissions.
- **🏆 End Game Overlay**: Displays game outcome (win/loss), reveals the mystery word upon defeat, and provides an instant restart option.
- **🎵 Background Music Component**: Includes an integrated audio player using JavaFX Media.

---

## 🏛️ Architecture & Design Patterns

The project follows clean object-oriented design and architectural patterns:

- **Model-View-Controller (MVC)**:
  - **Model (`com.wordle.model`)**:
    - `Model` & `ModelImpl`: Manages game state (status, guesses, grayed letters, active mode, hint logic, and attempts).
  - **Controller (`com.wordle.controller`)**:
    - `Controller` & `ControllerImpl`: Handles player actions (keystrokes, mode changes, hint requests), manages async network requests via OkHttp, and updates the model.
  - **View (`com.wordle.view`)**:
    - `AppLauncher`: JavaFX application entry point and stage setup.
    - `View`: Renders the UI (header, board grid, virtual keyboard, end-game modal, animations).
    - `FXComponent`: Functional interface defining UI components.
    - `Music`: Background media controller.
- **Observer Pattern**:
  - `Model` extends `Subject`, and `View` implements `Observer`. State changes notify registered observers to render UI updates cleanly on the JavaFX Application Thread (`Platform.runLater`).
- **Asynchronous Networking**:
  - HTTP requests to the Datamuse API run off the UI thread using OkHttp's asynchronous `enqueue()` mechanism to keep the interface smooth and responsive.

---

## 📁 Project Structure

```
wordle/
├── pom.xml                               # Maven project configuration and dependencies
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── wordle/
        │           ├── Main.java         # Application launcher entry point
        │           ├── controller/
        │           │   ├── Controller.java
        │           │   └── ControllerImpl.java
        │           ├── model/
        │           │   ├── Model.java
        │           │   ├── ModelImpl.java
        │           │   ├── Observer.java
        │           │   └── Subject.java
        │           └── view/
        │               ├── AppLauncher.java
        │               ├── FXComponent.java
        │               ├── Music.java
        │               └── View.java
        └── resources/
            ├── music.mp3                 # Background music asset
            └── style/
                └── wordle.css            # Stylesheet for tiles and UI elements
```

---

## 🛠️ Prerequisites

- **Java Development Kit (JDK)**: Version 21 or newer
- **Apache Maven**: Version 3.8 or newer
- **Internet Connection**: Required for Datamuse API word retrieval and guess verification.

---

## 🚀 Getting Started

### 1. Navigate to the Wordle Directory
```bash
cd wordle
```

### 2. Compile the Project
```bash
mvn clean compile
```

### 3. Run with Maven (JavaFX Plugin)
```bash
mvn javafx:run
```

### 4. Build and Run Executable JAR
You can package the project into a standalone runnable JAR (with dependencies included):
```bash
mvn package
java -jar target/wordle-1.0-SNAPSHOT.jar
```

---

## 📦 Dependencies

- [OpenJFX (JavaFX)](https://openjfx.io/) `21` – JavaFX Controls, FXML, and Media.
- [OkHttp](https://square.github.io/okhttp/) `4.12.0` – HTTP client for asynchronous Datamuse API calls.
- [JUnit](https://junit.org/junit4/) `4.13.2` – Unit testing framework.