package hw3.test;

import gurobi.GRBException;
import hw3.Mincostflow;

public class MincostflowTest {

    public static void main(String[] args) throws GRBException {
        final int[][] l = new int[][] {
                {0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0},
        };
        final int[][] u = new int[][] {
                {0, 0, 0, 1, 1, 1},
                {0, 0, 0, 1, 1, 1},
                {0, 0, 0, 1, 1, 1},
                {0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0},
        };
        final int[][] c = new int[][] {
                {0, 0, 0, 0, 1, 1},
                {0, 0, 0, 0, 1, 1},
                {0, 0, 0, 0, 0, 1},
                {0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0},
        };
        final int[] b = new int[] {1, 1, 1, -1, -1, -1};

        int f[][] = Mincostflow.mincostflow(l, u, c, b);

        printFlow(f);
    }

    private static void printFlow(int[][] f) {
        for (int i=0; i<f.length; i++) {
            for (int j=0; j<f[i].length; j++) {
                System.out.printf("%1$d", f[i][j]);
                if (j!= f[i].length-1) {
                    System.out.print(", ");
                }
            }
            System.out.println();
        }
    }

}
