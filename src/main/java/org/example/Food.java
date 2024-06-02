package org.example;

import java.util.Random;

public class Food {
    double x, y;

    public Food() {
        Random rand = new Random();
        this.x = rand.nextDouble() * 800;
        this.y = rand.nextDouble() * 600;
    }
}