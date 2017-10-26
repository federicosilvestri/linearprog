package pl.core;

import org.la4j.LinearAlgebra;
import org.la4j.Matrix;

import org.la4j.Vector;

import java.util.Arrays;

/**
 * Implementation of Primal Simplex.
 *
 * @author federicosilvestri silves.federico@gmail.com
 */
public class PrimalSimplex extends Simplex {


    public PrimalSimplex(double[][] A, double[] b, double[] c) {
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
        int outBoundIndex = computeOutboundIndex(y);

        if (outBoundIndex == -1) {
            System.out.println("YES!");
            stop = true;
        } else {
            System.out.println("NO");
            System.out.println("------> OutBoundIndex=" + outBoundIndex);

            int inboundIndex = computeInboundIndex(invertedBaseMatrix, outBoundIndex, b, x);

            if (inboundIndex == -1) {
                stop = true;
                System.out.println("--> Solution IS INFINITY!");
            } else {
                System.out.println("------> InboundIndex=" + inboundIndex);

                super.computeNewBase(outBoundIndex, inboundIndex);
                System.out.println("--> New baseIndexes:" + Arrays.toString(baseIndexes));
            }
        }
    }

    /**
     * We need to check if all y are negative. If all y are >= 0 the solution is optimal.
     * Else we can use the first negative value index as outbound index.
     *
     * @param y y result vector
     * @return -1 if solution is optimal, else the outbound index.
     */
    private int computeOutboundIndex(Vector y) {
        int outIndex = -1;

        for (int i = 0; i < y.length(); i++) {
            if (y.get(i) < 0 && outIndex == -1) {
                outIndex = baseIndexes[i];
            }
        }

        return outIndex;
    }


    /**
     * In this method we need to compute the inbound index, i.e. the index of minimum rapport.
     *
     * @param invertedSubMatrix the inverted base matrix
     * @param outBoundIndex     the inbound index
     * @param b                 b coefficients vector
     * @param x                 base solution
     * @return -1 if solution is Infinity, else the inbound index.
     */
    private int computeInboundIndex(Matrix invertedSubMatrix, int outBoundIndex, Vector b, Vector x) {
        Matrix W = invertedSubMatrix.multiply(-1);
        System.out.println(invertedSubMatrix);
        System.out.println("BASE INDEXES: " + Arrays.toString(baseIndexes) + " NONBASE: " + Arrays.toString(nonBaseIndexes));
        System.out.println("SEARCHING " + outBoundIndex + " IN " + Arrays.toString(baseIndexes));
        int WColumnIndex = Arrays.binarySearch(baseIndexes, outBoundIndex);

        // Getting matrix column
        Matrix Wh = W.getColumn(WColumnIndex).toColumnMatrix();

        System.out.println("--> Checking infinity...");

        double min = Double.MAX_VALUE;
        int minIndex = -1;

        for (int index : nonBaseIndexes) {
            Vector ARow = A.getRow(index);

            double rapportNum = b.get(index) - ARow.multiply(x.toColumnMatrix()).get(0);
            double rapportDen = ARow.multiply(Wh).get(0);

            System.out.println("INDEX " + index + ": " + rapportNum / rapportDen);

            if (rapportDen > 0) {
                double rapport = rapportNum / rapportDen;

                if (rapport < min) {
                    min = rapport;
                    minIndex = index;
                }
            }
        }

        return minIndex;
    }
}