package com.company;

import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        double A[][] = new double[][] {{-2, 1}, {1, 2}, {1, 0}, {1, -1}, {-2, -1}, {-1, -1}, {-3, -2}};
        double b[] = new double[] {-8, -6, 4, 12, 4, 6, 10};
        double c[] = new double[] {-9, -5};
        int sp[] = new int[] {1, 4};

        LinProg lp = new LinProg(A, b, c);
        lp.compute(sp);

    }

    public static void printMatrix(double A[][]) {

        for (double a[] : A) {
            System.out.println(Arrays.toString(a));
        }
    }
}
