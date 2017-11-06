package pl.core;

import org.la4j.LinearAlgebra;
import org.la4j.Matrix;
import org.la4j.Vector;
import org.la4j.vector.dense.BasicVector;

import java.util.Arrays;

public class DualSimplex extends Simplex {

    public DualSimplex(double[][] A, double[] b, double[] c) {
        super(A, b, c);
    }

    protected void compute() {
        System.out.println("!!! ---> Generating base data with baseIndexes: " + Arrays.toString(baseIndexes));

        Matrix baseMatrix = super.computeBaseMatrix(baseIndexes);
        Matrix invertedBaseMatrix = baseMatrix.withInverter(LinearAlgebra.InverterFactory.GAUSS_JORDAN).inverse();
        Vector baseB = super.computeBaseB(baseIndexes);

        System.out.println("| Base Matrix |");
        System.out.print(baseMatrix);
        System.out.println("| Inverted Base Matrix |");
        System.out.print(invertedBaseMatrix);
        System.out.println("| Base B Vector | ");
        System.out.println(baseB);

        Vector x = computeX(invertedBaseMatrix, baseB);

        System.out.println("---> Base solution of x is: ");
        System.out.println(x);

        Vector y = computeY(invertedBaseMatrix, c, baseIndexes);

        System.out.println("---> Base solution of y is: ");
        System.out.println(y);

        System.out.print("-->Checking if solution is optimal...");
        int inboundIndex = computeInboundIndex(x);

        if (inboundIndex == -1) {
            System.out.println("YES!");
            stop = true;
        } else {
            System.out.println("NO");
            System.out.println("------> InBoundIndex=" + inboundIndex);

            int outBoundIndex = computeOutboundIndex(invertedBaseMatrix, y, inboundIndex);

            if (inboundIndex == -1) {
                stop = true;
                System.out.println("--> Solution IS INFINITY!");
            } else {
                System.out.println("------> OutBoundIndex=" + outBoundIndex);

                super.computeNewBase(outBoundIndex, inboundIndex);
                System.out.println("--> New baseIndexes:" + Arrays.toString(baseIndexes));
            }
        }
    }

    /**
     * This method calculates the inbound index of given x.
     *
     * @param x x base solution vector
     * @return -1 if solution is optimal, else the inbound index
     */
    private int computeInboundIndex(Vector x) {
        int inboundIndex = -1;

        for (int index : nonBaseIndexes) {
            double result = this.b.get(index) - A.getRow(index).toRowMatrix().multiply(x).get(0);

            if (result < 0 && inboundIndex == -1) {
                inboundIndex = index;

            }
        }

        return inboundIndex;
    }

    /**
     * This method calculates the outbound index of given status.
     * @param invertedBaseMatrix inverted base matrix
     * @param y y vector
     * @param inboundIndex the inbound index
     * @return -1 if solution is infinity, else the outbound index
     */
    private int computeOutboundIndex(Matrix invertedBaseMatrix, Vector y, int inboundIndex) {
        Matrix W = invertedBaseMatrix.multiply(-1);
        Matrix Ak = A.getRow(inboundIndex).toRowMatrix();

        // Generating sub vector y with base indexes.
        Vector subY = new BasicVector(baseIndexes.length);

        int outBoundIndex = -1;
        double minRapport = Double.MAX_VALUE;

        for (double baseIndex : baseIndexes) {
            subY.add(baseIndex);
        }

        for (int i = 0; i < W.columns(); i++) {
            double den = Ak.multiply(W.getColumn(i)).get(0);

            if (den < 0) {
                den *= -1;

                double r = subY.get(i) / den;
                if (r < minRapport) {
                    minRapport = r;
                    outBoundIndex = baseIndexes[i];
                }
            }

        }

        return outBoundIndex;
    }
}
