import edu.princeton.cs.algs4.CC;
import edu.princeton.cs.algs4.Edge;
import edu.princeton.cs.algs4.EdgeWeightedGraph;
import edu.princeton.cs.algs4.Graph;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.KruskalMST;
import edu.princeton.cs.algs4.Point2D;
import edu.princeton.cs.algs4.StdOut;

import java.util.ArrayList;

public class Clustering {

    private CC connected; // cluster graph
    private int k; // number of clusters
    private int m; // number of locations

    // run the clustering algorithm and create the clusters
    public Clustering(Point2D[] locations, int k) {
        if (locations == null) {
            throw new IllegalArgumentException("null argument");
        }
        if (k < 1 || k > locations.length) {
            throw new IllegalArgumentException("k out of bounds");
        }
        for (int i = 0; i < locations.length; i++) {
            if (locations[i] == null) {
                throw new IllegalArgumentException("location " + i + " is null");
            }
        }

        this.k = k;
        m = locations.length;

        EdgeWeightedGraph graph = new EdgeWeightedGraph(locations.length);

        for (int i = 0; i < locations.length; i++) {
            for (int j = 0; j < locations.length; j++) {
                if (i != j) {
                    Edge edge = new Edge(
                            i, j, locations[i].distanceSquaredTo(locations[j]));
                    graph.addEdge(edge);
                }
            }
        }

        KruskalMST mst = new KruskalMST(graph);
        ArrayList<Edge> arrayList = new ArrayList<Edge>();
        for (Edge e : mst.edges()) {
            arrayList.add(e);

        }

        arrayList.sort((o1, o2) -> Double.compare(o1.weight(), o2.weight()));

        Graph cluster = new Graph(m);
        for (int i = 0; i < m - k; i++) {
            Edge e = arrayList.get(i);
            int v = e.either();
            cluster.addEdge(v, e.other(v));
        }

        connected = new CC(cluster);

    }

    // return the cluster of the ith point
    public int clusterOf(int i) {
        if (i < 0 || i > m - 1) {
            throw new IllegalArgumentException("i out of bounds");
        }
        return connected.id(i);
    }

    // use the clusters to reduce the dimensions of an input
    public double[] reduceDimensions(double[] input) {
        if (input == null) {
            throw new IllegalArgumentException("null input");
        }
        if (input.length != m) {
            throw new IllegalArgumentException("input arr wrong length");
        }

        double[] reduced = new double[k];

        for (int i = 0; i < input.length; i++) {
            int clusterNumber = clusterOf(i);
            reduced[clusterNumber] = reduced[clusterNumber] + input[i];
        }

        return reduced;
    }

    // unit testing (required)
    public static void main(String[] args) {
        In in = new In("princeton_locations.txt");
        Point2D[] locations = new Point2D[in.readInt()];
        int i = 0;
        while (!in.isEmpty()) {
            locations[i] = new Point2D(in.readDouble(), in.readDouble());
            i++;
        }

        Clustering clustering = new Clustering(locations, 5);
        In in2 = new In("test2.txt");
        double[] transSummary = new double[21];
        int j = 0;
        while (!in2.isEmpty()) {
            transSummary[j] = in2.readInt();
            j++;
        }

        StdOut.println(clustering.clusterOf(0));
        for (double weight : clustering.reduceDimensions(transSummary)) {
            StdOut.println(weight);
        }

    }
}
