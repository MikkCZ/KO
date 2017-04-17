package hw2.test;

import hw2.FordFulkerson;

public class FordFulkersonTest {

    public static void main(String[] args) {
        final int[][][] e = new int[][][] {
                {{1,3},{}},
                {{2,3},{0,3}},
                {{},{1,4}},
                {{1,4},{0,1}},
                {{2},{3}},
        };
        final int[][] u = new int[][] {
                {0, 3, 0, 2, 0},
                {0, 0, 2, 1, 0},
                {0, 0, 0, 0, 0},
                {0, 2, 0, 0, 4},
                {0, 0, 5, 0, 0},
        };
        final int[][] l = new int[][] {
                {0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0},
        };
        final int[][] f = l;
        final int s = 0;
        final int t = 2;

        FordFulkerson.fordFulkerson(e, u, l, s, t, f);

        printFlow(f);
    }

    private static void printFlow(int[][] f) {
        for (int i=0; i<f.length; i++) {
            System.out.printf("%d: ", i);
            for (int j=0; j<f[i].length; j++) {
                System.out.printf("%1$d:%2$d", j, f[i][j]);
                if (j!= f[i].length-1) {
                    System.out.print(", ");
                }
            }
            System.out.println();
        }
    }

}
