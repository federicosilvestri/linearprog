package pl;

import pl.core.PrimalSimplex;

public class Main {

    public static void main(String[] args) {
        double A[][] = new double[][]{{-5, -2}, {-1, 0}, {0, 1}, {3, 3}, {-7, 6}};
        double b[] = new double[]{0, 1, 5, 30, 22};
        double c[] = new double[]{1, 3};
        int sp[] = new int[]{1, 2};

        PrimalSimplex lp = new PrimalSimplex(A, b, c);
        lp.compute(sp);

    }

}
