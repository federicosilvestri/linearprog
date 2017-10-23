package com.company;

import org.la4j.LinearAlgebra;
import org.la4j.Matrix;

import org.la4j.matrix.dense.Basic2DMatrix;

import java.util.*;


public class LinProg {

    private final double A[][];

    private final double b[];

    private final double c[];

    private int bases[];

    private ArrayList<Integer> nonBasesIndex;

    private boolean stop;


    public LinProg(double A[][], double b[], double c[]) {
        // Check size
        if (A == null || b == null || c == null) {
            throw new IllegalArgumentException("You cannot pass null parameter/s!");
        }

        if (A.length != b.length) {
            throw new IllegalArgumentException("Rows of A must be equals to b vector size!");
        }

        if (A[0].length != c.length) {
            throw new IllegalArgumentException("Columns of A must be equals to c vector size!");
        }

        // Check that matrix is correct
        for (int i = 0; i < A.length - 1; i++) {
            if (A[i].length != A[i + 1].length) {
                throw new IllegalArgumentException("Matrix columns size mismatch! Please pass a valid matrix!");
            }
        }

        // Check matrix rango
        Basic2DMatrix checkMatrix = new Basic2DMatrix(A);
        if (checkMatrix.rank() != A[0].length) {
            throw new IllegalArgumentException("Matrix A must have rank=n !");
        }

        this.A = A;
        this.b = b;
        this.c = c;
    }


    public String getData() {
        String s = "";

        s += "=== A Matrix ===";

        for (double a[] : A) {
            s += "\n" + Arrays.toString(a);
        }


        s += "\n=== b Vector ===\n";
        s += Arrays.toString(b);

        s += "\n=== c Vector ===\n";
        s += Arrays.toString(c);

        return s;
    }


    private void checkStartingPoint(int[] startingPoint) {
        if (startingPoint == null) {
            throw new IllegalArgumentException("Starting point must not be null!");
        }
        for (int k : startingPoint) {
            if (k < 1 || k > A.length) {
                throw new IllegalArgumentException("Starting point must be a base of polyhedron!");
            }
        }
    }

    private int[] adjustStartingPoint(int[] startingPoint) {
        int adjusted[] = new int[startingPoint.length];

        // Adjusting indexes
        for (int i = 0; i < startingPoint.length; i++) {
            adjusted[i] = startingPoint[i] - 1;
        }

        return adjusted;
    }

    private int[] readjustStartingPoint(int[] startingPoint) {
        int adjusted[] = new int[startingPoint.length];

        // Adjusting indexes
        for (int i = 0; i < startingPoint.length; i++) {
            adjusted[i] = startingPoint[i] + 1;
        }

        return adjusted;
    }

    public void compute(int startingPoint[]) {
        checkStartingPoint(startingPoint);

        bases = adjustStartingPoint(startingPoint);
        stop = false;

        while (!stop) {
            compute();
        }
    }

    private void compute() {
        System.out.println("---> Generating sub-data with bases: " + Arrays.toString(bases));

        double subMatrix[][] = new double[bases.length][A[0].length];
        double subB[] = new double[bases.length];

        for (int i = 0; i < bases.length; i++) {
            subMatrix[i] = A[bases[i]];
            subB[i] = b[bases[i]];
        }

        computeNonbaseIndexes();

        System.out.println("---> Sub A Matrix:");
        Main.printMatrix(subMatrix);
        System.out.println("---> Sub b Vector:");
        System.out.println(Arrays.toString(subB));

        System.out.println("--> Inverting sub matrix");
        Matrix invertedSubMatrix = new Basic2DMatrix(subMatrix)
                .withInverter(LinearAlgebra.InverterFactory.GAUSS_JORDAN)
                .inverse();

        double x[] = computeX(invertedSubMatrix, subB);

        System.out.println("---> Base solution of x is: ");
        System.out.println(Arrays.toString(x));

        double y[] = computeY(invertedSubMatrix, c, bases, A.length);

        System.out.println("---> Base solution of y is: ");
        System.out.println(Arrays.toString(y));

        System.out.println("-->Checking if solutions are optimal");
        int outBoundIndex = computeOutboundIndex(y, x);

        if (outBoundIndex == -1) {
            System.out.println("--> Stop, soluctions are optimal");
            stop = true;
        } else {
            System.out.println("--> Solutions are not optimal, OutBoundIndex=" + (outBoundIndex + 1));

            int inboundIndex = computeInboundIndex(invertedSubMatrix, A, bases, outBoundIndex, b, x);

            System.out.println("--> InboundIndex=" + (inboundIndex + 1));

            computeNewBase(outBoundIndex, inboundIndex);

            System.out.println("--> New bases:" + Arrays.toString(readjustStartingPoint(bases)));
        }
    }


    private void computeNonbaseIndexes() {
        nonBasesIndex = new ArrayList<Integer>();

        for (int i = 0; i < A.length; i++) {
            nonBasesIndex.add(i);
        }

        for (int baseIndex : bases) {
            nonBasesIndex.remove(new Integer(baseIndex));
        }
    }

    private double[] computeX(Matrix invertedSubMatrix, double[] b) {
        System.out.println("---> Calculating x base solution");

        // Calculate base solutions with inverted matrix
        Matrix bMatrix = new Basic2DMatrix(new double[][]{b});

        System.out.println("---> Inverted Sub A Matrix:");
        System.out.print(invertedSubMatrix);

        Matrix results = invertedSubMatrix.multiply(bMatrix.transpose());

        return results.transpose().toDenseMatrix().toArray()[0];
    }

    private double[] computeY(Matrix invertedSubMatrix, double c[], int[] startingPoint, int matrixRows) {
        System.out.println("---> Calculating y base solution");

        // Calculate base solutions with inverted matrix
        Matrix cMatrix = new Basic2DMatrix(new double[][]{c});
        Matrix yResults = cMatrix.multiply(invertedSubMatrix);

        double yResultVector[] = yResults.toDenseMatrix().toArray()[0];
        double result[] = new double[matrixRows];

        for (int i = 0; i < startingPoint.length; i++) {
            result[startingPoint[i]] = yResultVector[i];
        }

        return result;
    }

    private int computeOutboundIndex(double y[], double x[]) {
        double min = y[0];
        int outIndex = (y[0] < 0) ? 0 : -1;

        // Try to check y
        for (int i = 1; i < y.length; i++) {
            if (y[i] < 0 && y[i] < min) {
                outIndex = i;
                min = y[i];
            }
        }

        return outIndex;
    }


    private int computeInboundIndex(Matrix invertedSubMatrix, double A[][], int[] startingPoint, int outBoundIndex, double b[], double x[]) {
        Matrix W = invertedSubMatrix.multiply(-1);
        int columnIndex = Arrays.binarySearch(startingPoint, outBoundIndex);

        assert (columnIndex > -1);

        // Getting matrix column
        Matrix Wh = W.getColumn(columnIndex).toColumnMatrix();

        System.out.println("--> Checking infinity...");

        double rapports[] = new double[nonBasesIndex.size()];
        int rapportsIndex = 0;
        boolean infinity = true;

        for (int index : nonBasesIndex) {
            Matrix a = new Basic2DMatrix(new double[][]{A[index]});

            double rapportNum = b[index] - a.multiply(
                    new Basic2DMatrix(new double[][]{x}).transpose()
            ).get(0, 0);

            double rapportDen = a.multiply(Wh).get(0, 0);

            if (rapportDen > 0) {
                infinity = false;
            }
            rapports[rapportsIndex] = rapportNum / rapportDen;
            rapportsIndex += 1;
        }

        System.out.println("--> Calculating rapports");
        double min = Double.MAX_VALUE;
        int minIndex = -1;
        for (int i = 0; i < rapports.length; i++) {
            if (rapports[i] > 0 && rapports[i] < min) {
                min = rapports[i];
                minIndex = i;
            }

            System.out.print(rapports[i] > 0 ? rapports[i] + "\n" : "");
        }

        if (infinity) {
            System.out.println("--> Solutions is infinity!");
            return -1;
        }

        return minIndex;
    }


    private void computeNewBase(int out, int in) {
        int outIndex = Arrays.binarySearch(bases, out);
        bases[outIndex] = in;
        Arrays.sort(bases);
    }
}
