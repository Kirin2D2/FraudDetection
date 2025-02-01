import edu.princeton.cs.algs4.Merge;
import edu.princeton.cs.algs4.StdOut;

import java.util.Arrays;

public class WeakLearner {
    private int champdp; // dp
    private double champvp; // vp
    private int champsp; // sp
    private int dimensions; // dimension

    // train the weak learner
    public WeakLearner(double[][] input, double[] weights, int[] labels) {
        if (input == null || weights == null || labels == null) {
            throw new IllegalArgumentException("null argument");
        }
        if (input.length != labels.length || input.length != weights.length) {
            throw new IllegalArgumentException("incompatible lengths");
        }
        for (double[] point : input) {
            if (point == null) {
                throw new IllegalArgumentException("null 2nd degree input");
            }
        }
        for (int i = 0; i < weights.length; i++) {
            if (weights[i] < 0) {
                throw new IllegalArgumentException("weights not all non-negative");
            }
        }
        for (int i = 0; i < labels.length; i++) {
            if (!(labels[i] == 1 || labels[i] == 0)) {
                throw new IllegalArgumentException("labels not 1 or 0");
            }
        }
        dimensions = input[0].length;

        double champCorrect = Double.NEGATIVE_INFINITY;
        // array to be sorted
        Double[] positions = new Double[input.length];
        // array mapping original index to sorted index
        int[] permutations = new int[input.length];
        // input.length is 'n', input[0].length is 'k'
        double[] zeroWeightsArr = new double[input.length];
        double[] oneWeightsArr = new double[input.length];

        for (int k = 0; k < input[0].length; k++) {
            for (int n = 0; n < input.length; n++) {
                positions[n] = input[n][k];
            }
            int j = 0;
            for (int i : Merge.indexSort(positions)) {
                permutations[j] = i;
                j++;
            }

            double zeroWeights = 0;
            double oneWeights = 0;
            for (int n = 0; n < input.length; n++) {
                if (labels[permutations[n]] == 1) {
                    oneWeights += weights[permutations[n]];
                }
                else {
                    zeroWeights += weights[permutations[n]];
                }
                zeroWeightsArr[n] = zeroWeights;
                oneWeightsArr[n] = oneWeights;
            }
            for (int n = 0; n < input.length; n++) {
                for (int sp = 0; sp < 2; sp++) {
                    double vp = input[permutations[n]][k];

                    // dealing with duplicates: skip to last in array
                    double correctWeights;
                    if (n == input.length - 1
                            || input[permutations[n + 1]][k]
                            != input[permutations[n]][k]) {
                        if (sp == 0) {

                            // zero weights to left plus one weights to right
                            correctWeights = zeroWeightsArr[n] +
                                    (oneWeightsArr[input.length - 1] -
                                            oneWeightsArr[n]);
                        }
                        else {
                            correctWeights = oneWeightsArr[n] +
                                    (zeroWeightsArr[input.length - 1]
                                            - zeroWeightsArr[n]);
                        }


                        if (correctWeights > champCorrect) {
                            champCorrect = correctWeights;
                            champdp = k;
                            champvp = vp;
                            champsp = sp;
                        }
                    }
                }
            }
        }
    }

    // return the prediction of the learner for a new sample
    public int predict(double[] sample) {
        if (sample == null) throw new IllegalArgumentException("null argument");
        if (sample.length != dimensions) {
            throw new IllegalArgumentException("incompatible sample length");
        }

        double value = sample[champdp];
        if (champsp == 0) {
            if (value <= champvp) {
                return 0;
            }
            return 1;
        }
        if (value <= champvp) {
            return 1;
        }
        return 0;
    }

    // return the dimension the learner uses to separate the data
    public int dimensionPredictor() {
        return champdp;
    }

    // return the value the learner uses to separate the data
    public double valuePredictor() {
        return champvp;
    }

    // return the sign the learner uses to separate the data
    public int signPredictor() {
        return champsp;
    }

    // unit testing (required)
    public static void main(String[] args) {
        // DataSet dataSet = new DataSet("princeton_training.txt");
        // int n = dataSet.n;
        // double[] weights = new double[n];
        // for (int i = 0; i < n; i++) {
        //     weights[i] = 1;
        // }
        //
        // WeakLearner weakLearner = new WeakLearner(dataSet.input, weights,
        //                                           dataSet.labels);
        // StdOut.println("dp: " + weakLearner.dimensionPredictor());
        // StdOut.println("vp: " + weakLearner.valuePredictor());
        // StdOut.println("sp: " + weakLearner.signPredictor());

        //
        // read in the terms from a file
        DataSet training = new DataSet(args[0]);
        DataSet test = new DataSet(args[1]);

        double[] weights = new double[training.input.length];
        Arrays.fill(weights, 1.0);

        // train the model
        WeakLearner model = new WeakLearner(training.input, weights,
                                            training.labels);

        // test basic methods
        StdOut.println("dp: " + model.dimensionPredictor());
        StdOut.println("vp: " + model.valuePredictor());
        StdOut.println("sp: " + model.signPredictor());


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

        StdOut.println("Training accuracy of model: " + trainingAccuracy);
        StdOut.println("Test accuracy of model:     " + testAccuracy);
    }

}
