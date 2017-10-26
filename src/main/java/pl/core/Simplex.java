package pl.core;

import org.la4j.Matrix;
import org.la4j.Vector;
import org.la4j.matrix.dense.Basic2DMatrix;
import org.la4j.vector.dense.BasicVector;

import java.util.Arrays;

abstract class Simplex {

    protected final Matrix A;

    protected final Vector b;

    protected final Vector c;

    /**
     * Array that contains base indexes.
     */
    protected int baseIndexes[];

    /**
     * Array that contains the indexes that are not base indexes.
     */
    protected int nonBaseIndexes[];

    /**
     * Boolean variable to stop computation.
     */
    protected boolean stop;

    public Simplex(double A[][], double b[], double c[]) {
        // Check size
        if (A == null || b == null || c == null) {
            throw new IllegalArgumentException("You cannot pass null parameter/s!");
        }

        if (A.length != b.length) {
            throw new IllegalArgumentException("Rows of matrix must be equals to b vector size!");
        }

        if (A[0].length != c.length) {
            throw new IllegalArgumentException("Columns of matrix must be equals to c vector size!");
        }

        // Check matrix rank
        Basic2DMatrix aMatrix = new Basic2DMatrix(A);
        if (aMatrix.rank() != A[0].length) {
            throw new IllegalArgumentException("Matrix matrix must have rank=n !");
        }

        this.A = aMatrix;
        this.b = new BasicVector(b);
        this.c = new BasicVector(c);
    }

    protected Matrix computeBaseMatrix(int[] baseIndexes) {
        Basic2DMatrix baseMatrix = new Basic2DMatrix(baseIndexes.length, baseIndexes.length);

        int i = 0;
        for (int index : baseIndexes) {
            baseMatrix.setRow(i, A.getRow(index));
            i += 1;
        }

        return baseMatrix;
    }

    protected Vector computeBaseB(int[] baseIndexes) {
        Vector baseB = new BasicVector(baseIndexes.length);

        int i = 0;
        for (int index : baseIndexes) {
            baseB.set(i, b.get(index));
            i += 1;
        }

        return baseB;
    }


    private void setStartingPoint(int[] startingPoint) {
        if (startingPoint == null) {
            throw new IllegalArgumentException("Starting point must not be null!");
        }

        for (int k : startingPoint) {
            if (k < 1 || k > A.rows()) {
                throw new IllegalArgumentException("Starting point must be a baseIndexes of polyhedron!");
            }
        }

        baseIndexes = new int[startingPoint.length];

        // Decrement all starting point from 0-based to 1-based vectors.
        for (int i = 0; i < startingPoint.length; i++) {
            baseIndexes[i] = startingPoint[i] - 1;
        }
    }

    private void computeIndexes() {
        nonBaseIndexes = new int[A.rows() - baseIndexes.length];

        for (int i = 0, j = 0; i < A.rows(); i++) {
            if (Arrays.binarySearch(baseIndexes, i) < 0) {
                nonBaseIndexes[j] = i;
                j += 1;
            }
        }
    }

    protected Vector computeX(Matrix invertedSubMatrix, Vector b) {
        System.out.println("---> Calculating x baseIndexes solution");

        // Calculate baseIndexes solutions with inverted matrix
        Vector results = invertedSubMatrix.multiply(b);

        return results;
    }

    protected Vector computeY(Matrix invertedSubMatrix, Vector c, int[] startingPoint) {
        System.out.println("---> Calculating y baseIndexes solution");

        // Calculate baseIndexes solutions with inverted matrix
        Vector yResults = c.multiply(invertedSubMatrix);

        return yResults;
    }

    protected void computeNewBase(int out, int in) {
        int outIndex = Arrays.binarySearch(baseIndexes, out);
        baseIndexes[outIndex] = in;
        Arrays.sort(baseIndexes);
    }

    protected abstract void compute();

    public void compute(int startingPoint[]) {
        setStartingPoint(startingPoint);

        stop = false;

        while (!stop) {
            computeIndexes();
            compute();
        }
    }
}
