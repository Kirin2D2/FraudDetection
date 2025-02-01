import edu.princeton.cs.algs4.Point2D;
import edu.princeton.cs.algs4.StdOut;

import java.util.Arrays;
import java.util.LinkedList;

public class BoostingAlgorithm {
    private int[] labels; // labels
    private double[] weights; // weights
    private double[][] reducedDimensions; // reduced transaction summaries
    private LinkedList<WeakLearner> learners; // linked list of weak learners
    private Clustering cluster; // locations cluster

    // create the clusters and initialize your data structures
    public BoostingAlgorithm(double[][] input, int[] labels, Point2D[]
            locations, int k) {
        if (input == null || labels == null || locations == null) {
            throw new IllegalArgumentException("null argument");
        }
        for (int i = 0; i < input.length; i++) {
            if (input[i] == null) {
                throw new IllegalArgumentException("null arg");
            }
        }
        if (input.length != labels.length) {
            throw new IllegalArgumentException("incompatible lengths");
        }
        if (k < 1 || k > locations.length) {
            throw new IllegalArgumentException("k out of bounds");
        }
        for (int i = 0; i < labels.length; i++) {
            if (!(labels[i] == 0 || labels[i] == 1)) {
                throw new IllegalArgumentException("labels not 0 or 1");
            }
        }

        // map transaction summaries into arrays length k
        cluster = new Clustering(locations, k);

        learners = new LinkedList<>();
        this.labels = Arrays.copyOf(labels, labels.length);

        weights = new double[input.length];
        Arrays.fill(weights, 1.0 / input.length);

        reducedDimensions = new double[input.length][k];

        for (int i = 0; i < input.length; i++) {
            reducedDimensions[i] = cluster.reduceDimensions(input[i]);
        }
    }

    // return the current weights
    public double[] weights() {
        return weights.clone();
    }

    // apply one step of the boosting algorithm
    public void iterate() {
        WeakLearner weakLearner = new WeakLearner(reducedDimensions, weights, labels);

        for (int i = 0; i < reducedDimensions.length; i++) {
            if (weakLearner.predict(reducedDimensions[i]) != labels[i]) {
                weights[i] *= 2;
            }
        }
        double sum = Arrays.stream(weights).sum();
        for (int i = 0; i < weights.length; i++) {
            weights[i] /= sum;
        }

        learners.add(weakLearner);
    }

    // return the prediction of the learner for a new sample
    public int predict(double[] sample) {
        if (sample == null) {
            throw new IllegalArgumentException("sample null");
        }

        double[] reducedSample = cluster.reduceDimensions(sample);

        int zeroTally = 0;
        for (WeakLearner weakLearner : learners) {
            if (weakLearner.predict(reducedSample) == 0) {
                zeroTally++;
            }
        }

        int oneTally = learners.size() - zeroTally;
        if (zeroTally >= oneTally) {
            return 0;
        }
        return 1;
    }

    // unit testing (required)
    public static void main(String[] args) {
        // read in the terms from a file
        DataSet training = new DataSet(args[0]);
        DataSet test = new DataSet(args[1]);
        int k = Integer.parseInt(args[2]);
        int iterations = Integer.parseInt(args[3]);

        // train the model
        BoostingAlgorithm model = new
                BoostingAlgorithm(training.input, training.labels,
                                  training.locations, k);
        for (int t = 0; t < iterations; t++)
            model.iterate();

        // calculate the training data set accuracy
        double trainingAccuracy = 0;
        for (int i = 0; i < training.n; i++)
            if (model.predict(training.input[i]) == training.labels[i])
                trainingAccuracy += 1;
        trainingAccuracy /= training.n;

        // calculate the test data set accuracy
        double testAccuracy = 0;
        for (int i = 0; i < test.n; i++)
            if (model.predict(test.input[i]) == test.labels[i])
                testAccuracy += 1;
        testAccuracy /= test.n;

        StdOut.println(model.weights());
        StdOut.println("Training accuracy of model: " + trainingAccuracy);
        StdOut.println("Test accuracy of model:     " + testAccuracy);

    }
}
