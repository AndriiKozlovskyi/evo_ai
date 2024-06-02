package org.example;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.util.List;
import java.util.Random;

public class Organism {
    MultiLayerNetwork model;
    double speed;
    double energy;
    double x, y;
    double targetX, targetY;
    int offspringCount;
    static final double MAX_SPEED = 20;

    public Organism() {
        int inputSize = 6; // x, y, скорость, энергия, ближайшая еда (x, y)
        int outputSize = 3; // dx, dy, решение о размножении

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(12345)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Adam(0.001))
                .list()
                .layer(0, new DenseLayer.Builder().nIn(inputSize).nOut(32)
                        .activation(Activation.RELU)
                        .build())
                .layer(1, new DenseLayer.Builder().nIn(32).nOut(16)
                        .activation(Activation.RELU)
                        .build())
                .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .activation(Activation.IDENTITY)
                        .nIn(16).nOut(outputSize)
                        .build())
                .build();

        this.model = new MultiLayerNetwork(conf);
        this.model.init();
        this.model.setListeners(new ScoreIterationListener(10));

        this.speed = 10;
        this.energy = 10;
        this.x = new Random().nextDouble() * 800;
        this.y = new Random().nextDouble() * 600;
        this.targetX = this.x;
        this.targetY = this.y;
        this.offspringCount = 0;
    }

    public double[] decide(double[] state) {
        INDArray input = Nd4j.create(state, new int[]{1, state.length});
        INDArray output = model.output(input);
        return output.toDoubleVector();
    }

    public void update(double[] state, double[] action, List<Organism> population) {
        // Устанавливаем целевые координаты
        this.targetX = this.x + action[0] * speed;
        this.targetY = this.y + action[1] * speed;

        // Проверка на коллизии с другими организмами
        for (Organism other : population) {
            if (other != this && Math.sqrt(Math.pow(this.targetX - other.x, 2) + Math.pow(this.targetY - other.y, 2)) < 20) {
                // Столкновение обнаружено, корректируем движение
                double angle = Math.atan2(this.targetY - other.y, this.targetX - other.x);
                this.targetX = other.x + 20 * Math.cos(angle);
                this.targetY = other.y + 20 * Math.sin(angle);
            }
        }

        // Проверка на границы карты
        if (this.targetX < 0) this.targetX = 0;
        if (this.targetX > 800) this.targetX = 800;
        if (this.targetY < 0) this.targetY = 0;
        if (this.targetY > 600) this.targetY = 600;

        // Ограничение скорости
        double dx = this.targetX - this.x;
        double dy = this.targetY - this.y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance > MAX_SPEED) {
            double scale = MAX_SPEED / distance;
            this.targetX = this.x + dx * scale;
            this.targetY = this.y + dy * scale;
        }

        // Обработка размножения
        if (action[2] > 0.5 && this.energy >= 20) {
            this.energy -= 20;
            this.offspringCount++;
        }

        // Уменьшение энергии на каждую итерацию
        this.energy -= 0.1;
    }

    public void interpolate() {
        // Линейная интерполяция к целевым координатам
        this.x += (this.targetX - this.x) * 0.01;
        this.y += (this.targetY - this.y) * 0.01;
    }

    public Organism reproduce() {
        Organism offspring = new Organism();
        offspring.model.setParams(this.model.params().dup());

        Random rand = new Random();
        INDArray params = offspring.model.params();
        for (int i = 0; i < params.length(); i++) {
            if (rand.nextDouble() < 0.05) {
                params.putScalar(i, params.getDouble(i) + rand.nextGaussian() * 0.1);
            }
        }
        offspring.model.setParams(params);
        offspring.energy = 20; // Начальная энергия потомка
        offspring.x = this.x;
        offspring.y = this.y;
        return offspring;
    }
}