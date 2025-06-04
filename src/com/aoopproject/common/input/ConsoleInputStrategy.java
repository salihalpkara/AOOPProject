package com.aoopproject.common.input;

import com.aoopproject.framework.core.GameView;
import com.aoopproject.framework.core.InputStrategy;

import java.util.Scanner;

/**
 * An {@link InputStrategy} that reads user input from the console.
 * This is a simple strategy primarily for text-based games or debugging.
 */
public class ConsoleInputStrategy implements InputStrategy {

    private Scanner scanner;

    public ConsoleInputStrategy() {
    }

    @Override
    public void initialize(GameView gameView) {
        this.scanner = new Scanner(System.in);
        System.out.println("ConsoleInputStrategy initialized. Enter actions via console.");
    }

    @Override
    public void dispose() {
        if (scanner != null) {
            System.out.println("ConsoleInputStrategy disposed.");
        }
    }
}