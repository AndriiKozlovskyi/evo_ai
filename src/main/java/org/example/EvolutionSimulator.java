package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EvolutionSimulator extends JPanel {
    List<Organism> population = new ArrayList<>();
    {{
        for (int i = 0; i < 1; i++) {
            population.add(new Organism());
        }
    }}
    List<Food> foodSources = new ArrayList<>();
    int generation = 0;
    Random rand = new Random();

    public EvolutionSimulator() {

        for (int i = 0; i < 5; i++) {
            foodSources.add(new Food());
        }
        Timer timer = new Timer(10, e -> update());
        timer.start();
    }

    public void update() {
        generation++;
        List<Organism> newGeneration = new ArrayList<>();
        List<Food> eatenFood = new ArrayList<>();

        for (Organism org : population) {
            Food closestFood = null;
            double minDistance = Double.MAX_VALUE;
            for (Food food : foodSources) {
                double distance = Math.sqrt(Math.pow(org.x - food.x, 2) + Math.pow(org.y - food.y, 2));
                if (distance < minDistance) {
                    minDistance = distance;
                    closestFood = food;
                }
            }

            double[] state = {org.x, org.y, org.speed, org.energy, closestFood.x, closestFood.y};
            double[] action = org.decide(state);

            org.update(state, action, population);

            if (closestFood != null && Math.sqrt(Math.pow(org.x - closestFood.x, 2) + Math.pow(org.y - closestFood.y, 2)) < 5) {
                org.energy += 1;
                eatenFood.add(closestFood);
            }

            if (org.energy > 0) {
                newGeneration.add(org);
            }
        }

        foodSources.removeAll(eatenFood);
        while (foodSources.size() < 100) {
            foodSources.add(new Food());
        }

        newGeneration.sort((o1, o2) -> Double.compare(o2.energy, o1.energy));


        for (int i = 0; i < 50 / 2; i++) {
            Organism parent = newGeneration.get(rand.nextInt(newGeneration.size()));
            Organism offspring = parent.reproduce();
            newGeneration.add(offspring);
        }
        population = newGeneration;

        for (Organism org : population) {
            org.interpolate();
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(Color.GREEN);
        for (Food food : foodSources) {
            g.fillOval((int) food.x, (int) food.y, 5, 5);
        }

        g.setColor(Color.BLUE);
        for (Organism org : population) {
            g.fillOval((int) org.x, (int) org.y, 10, 10);
        }

        g.setColor(Color.BLACK);
        g.drawString("Generation: " + generation, 10, 10);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Evolution Simulator");
        EvolutionSimulator simulator = new EvolutionSimulator();
        frame.add(simulator);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}