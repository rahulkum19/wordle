# Wordle (JavaFX)

### A Desktop Word-Guessing Game in Java & JavaFX
**Tech Stack:** Java 23 | JavaFX 21 | Apache Maven | MVC Architecture | OkHttp | Datamuse API

---

## Overview

**Wordle (JavaFX)** is a desktop clone of the popular word-guessing game built with Java and JavaFX. The application follows the **Model-View-Controller (MVC)** and **Observer** architectural patterns, keeping game rules, dictionary network calls, and UI rendering cleanly separated.

The game features dynamic word selection and real-time guess verification powered by the Datamuse API, three customizable difficulty tiers, an intelligent hint system, on-screen and physical keyboard support, and responsive UI animations.

---

## Key Features

- **Dynamic Word Generation**: Fetches target words directly from the [Datamuse API](https://www.datamuse.com/api/) filtered by English word frequency scores.
- **Live Dictionary Validation**: Verifies player guesses asynchronously against the Datamuse API before evaluating them to ensure valid English words.
- **3 Difficulty Tiers**: Adjusts allowed attempts, word rarity, on-screen keyboard support, and hint access across Easy, Medium, and Hard modes.
- **Smart Hint System**: Reveals an unsolved letter in its correct position upon request when playing on Easy or Medium modes.
- **Dual Input Support**: Full support for both standard physical keyboard typing and an on-screen, color-coded virtual keyboard.
- **Visual Feedback & Animations**: Shake animations trigger on invalid guesses, and modal overlays display game outcomes.
- **Observer-Driven UI Updates**: Uses the Observer pattern so JavaFX views update automatically in response to state changes in the model, keeping game logic independent of the UI.

---

## Architecture & Design Patterns

### Model-View-Controller (MVC)

- **Model (`com.wordle.model`)**:
  - `Model` & `ModelImpl`: Manages core game state, including active guesses, excluded (grayed) letters, current difficulty mode, hint logic, and remaining attempts.
- **Controller (`com.wordle.controller`)**:
  - `Controller` & `ControllerImpl`: Handles user input (physical keystrokes, on-screen clicks, mode changes, and hints), coordinates asynchronous network requests using OkHttp, and updates the model.
- **View (`com.wordle.view`)**:
  - `AppLauncher`: JavaFX entry point that configures the application window and stage.
  - `View`: Renders the visual interface, including the header, letter grid, on-screen keyboard, end-game modal, and tile animations.
  - `FXComponent`: Functional interface defining renderable JavaFX UI components.

### Observer Pattern

The model implements the `Subject` interface, allowing any view implementing `Observer` to register via `addObserver(Observer o)`. When the game state changes, the model calls `notifyObservers()`, safely dispatching UI updates to the JavaFX Application Thread using `Platform.runLater`.

### Asynchronous Networking

Network calls to the Datamuse API run on background threads using OkHttp's asynchronous `enqueue()` mechanism. This ensures the JavaFX UI thread is never blocked during dictionary validation or word fetching, keeping animations and typing completely smooth.

---

## Project Structure

```text
src/
|-- main/
|   |-- java/com/wordle/
|   |   |-- Main.java                    # Application entry point
|   |   |-- controller/
|   |   |   |-- Controller.java          # Controller interface
|   |   |   `-- ControllerImpl.java      # Input handling & async networking
|   |   |-- model/
|   |   |   |-- Model.java               # Core model contract
|   |   |   |-- ModelImpl.java           # Game rules & dictionary state
|   |   |   |-- Observer.java            # Observer interface
|   |   |   `-- Subject.java             # Subject interface
|   |   `-- view/
|   |       |-- AppLauncher.java         # JavaFX application bootstrapper
|   |       |-- FXComponent.java         # View component interface
|   |       `-- View.java                # Master view & UI rendering
|   `-- resources/
|       `-- style/
|           `-- wordle.css               # Styling for tiles, keyboard, and modals
pom.xml                                  # Maven configuration & dependencies
```

---

## Getting Started

### Prerequisites

- **Java Development Kit (JDK)**: Version 21 or higher (JDK 23 recommended).
- **Apache Maven**: Version 3.8+ installed and available on PATH.
- **Internet Connection**: Required for Datamuse API word retrieval and guess verification.

### Build and Execution

Compile and launch the game directly via the JavaFX Maven plugin:

```bash
mvn clean javafx:run
```

### Packaging Executable JAR

Build a standalone executable JAR bundling all required dependencies:

```bash
mvn clean package
```

```bash
java -jar target/wordle-1.0-SNAPSHOT.jar
```

---

## How to Play

### Rules & Tile Feedback

The objective is to guess a hidden 5-letter word within a limited number of attempts:
- Each guess must be a valid 5-letter word recognized by the dictionary.
- After submitting a guess, tiles change color to give feedback:
  - 🟩 **Green**: The letter is correct and in the correct position.
  - 🟨 **Yellow**: The letter is in the word, but in a different position.
  - ⬜ **Gray**: The letter is not in the target word.

### Controls

| Action | Physical Keyboard | On-Screen Keyboard |
| :--- | :---: | :---: |
| Enter Letter | `A`–`Z` | Click letter key |
| Delete Letter | `Backspace` | Click `⌫` key |
| Submit Guess | `Enter` | Click `ENTER` key |
| Request Hint | — | Click `REVEAL HINT` |

### Difficulty Modes

- **Easy Mode**: 7 attempts, high-frequency/familiar words (top 20% frequency), on-screen keyboard enabled, hint system available.
- **Medium Mode**: 6 attempts, moderately frequent words (15%–35% frequency), on-screen keyboard enabled, hint system available.
- **Hard Mode**: 5 attempts, rare/challenging words (bottom 20% frequency), on-screen keyboard disabled (physical typing only), hints disabled, and strict hard-mode rules (cannot reuse gray letters).
