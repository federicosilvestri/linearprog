package pl;

import pl.core.DualSimplex;
import pl.core.PrimalSimplex;
import pl.core.Simplex;

public class Main {

    public static void main(String[] args) {
        double A[][] = new double[][]{{-5, -2}, {-1, 0}, {0, 1}, {3, 3}, {-7, 6}};
        double b[] = new double[]{0, 1, 5, 30, 22};
        double c[] = new double[]{1, 3};
        int sp[] = new int[]{3, 5};

        Simplex primal = new PrimalSimplex(A, b, c);
        Simplex dual = new DualSimplex(A, b, c);

        System.out.println("Primal Simplex:");
        primal.compute(sp);

        System.out.println("Dual Simplex:");
        dual.compute(sp);

    }

}
