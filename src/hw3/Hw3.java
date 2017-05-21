package hw3;

import gurobi.GRBException;

import java.io.*;

public class Hw3 {

    private static int n, k, diagN;
    private static int[] sumR, sumC, sumA, sumD;
    private static String[] projections;

    public static void main(String[] args) throws IOException, GRBException {
        // read input
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(args[0]))))) {
            String[] firstLine = br.readLine().split("\\s+");

            n = Integer.parseInt(firstLine[0]);
            k = Integer.parseInt(firstLine[1]);
            diagN = n + (n-1);

            sumR = new int[n];
            sumC = new int[n];
            sumA = new int[diagN];
            sumD = new int[diagN];

            final int[][] sums = new int[][] {sumR, sumC, sumA, sumD};

            for (int[] sum : sums) {
                String[] sumLine = br.readLine().split("\\s+");
                for (int i=0; i<sum.length; i++) {
                    sum[i] = Integer.parseInt(sumLine[i]);
                }
            }

            projections = br.readLine().split("\\s+");
        }

        boolean[][] I = new boolean[n][n];
        for (int i=0; i<k; i++) {
            for (int j=0; j<projections.length; j+=2) {
                final char[] pair = new char[] {projections[j].charAt(0), projections[j+1].charAt(0)};
                I = nextIteration(pair, I);
                printI(I);
            }
        }

        // output
        try(PrintStream output = new PrintStream(new File(args[1]))) {
            for (int i=0; i<n; i++) {
                for (int j=0; j<n; j++) {
                    if (I[i][j]) {
                        output.print(1);
                    } else {
                        output.print(0);
                    }
                    if (j != n-1) {
                        output.print(' ');
                    }
                }
                if (i != n-1) {
                    output.println();
                }
            }
        }
    }

    private static boolean[][] nextIteration(char[] pair, boolean[][] I) throws GRBException {
        if (pair[0] == 'R' && pair[1] == 'C') {
            return iterationRC(I, sumR, sumC);
        } else if (pair[0] == 'C' && pair[1] == 'R') {
            return iterationCR(I, sumR, sumC);
        } else if (pair[0] == 'R' && pair[1] == 'A') {
            return iterationRA(I, sumR, sumA);
        } else if (pair[0] == 'C' && pair[1] == 'A') {
            return iterationCA(I, sumC, sumD);
        } else if (pair[0] == 'C' && pair[1] == 'D') {
            return iterationCD(I, sumC, sumD);
        } else if (pair[0] == 'A' && pair[1] == 'R') {
            return iterationAR(I, sumA, sumR);
        } else if (pair[0] == 'A' && pair[1] == 'C') {
            return iterationAC(I, sumA, sumC);
        } else if (pair[0] == 'D' && pair[1] == 'C') {
            return iterationDC(I, sumD, sumC);
        } else {
            return I;
        }
    }

    private static boolean[][] iterationRC(boolean[][] I, int[] sumRow, int[] sumCol) throws GRBException {
        final int[][] l = new int[2*n][2*n]; // all zeros

        final int[][] u = new int[2*n][2*n]; // ones in top right
        for (int i=0; i<n; i++) {
            for (int j=n; j<2*n; j++) {
                u[i][j] = 1;
            }
        }

        final int[][] c = new int[2*n][2*n]; // ones where I = false
        for (int i=0; i<n; i++) {
            for (int j=0; j<n; j++) {
                if (!I[i][j]) {
                    c[i][n+j] = 1;
                }
            }
        }

        final int[] b = concatArrays(sumRow, minusArray(sumCol)); // concat sumRow, -sumCol

        final int[][] f = Mincostflow.mincostflow(l, u, c, b);
        for (int i=0; i<n; i++) {
            for (int j=0; j<n; j++) {
                I[i][j] = (f[i][n+j] == 1);
            }
        }
        return I;
    }

    private static boolean[][] iterationCR(boolean[][] I, int[] sumRow, int[] sumCol) throws GRBException {
        return mainDiagonalReflection(iterationRC(mainDiagonalReflection(I), sumCol, sumRow));
    }

    private static boolean[][] iterationRA(boolean[][] I, int[] sumRow, int[] sumDiag) throws GRBException {
        final int[][] l = new int[n+diagN][n+diagN]; // all zeros

        final int[][] u = new int[n+diagN][n+diagN]; // fill top right n*diagN
        for (int i=0; i<n; i++) {
            for (int j=n; j<n+n; j++) {
                u[i][j+i] = 1;
            }
        }

        final int[][] c = new int[n+diagN][n+diagN]; // ones where I = false
        for (int i=0; i<n; i++) {
            for (int j=0; j<n; j++) {
                if (!I[i][j]) {
                    c[i][n+j+i] = 1;
                }
            }
        }

        final int[] b = concatArrays(sumRow, minusArray(sumDiag)); // concat sumRow, -sumDiag

        final int[][] f = Mincostflow.mincostflow(l, u, c, b);
        for (int i=0; i<n; i++) {
            for (int j=0; j<n; j++) {
                I[i][j] = (f[i][n+j+i] == 1);
            }
        }
        return I;
    }

    private static boolean[][] iterationCA(boolean[][] I, int[] sumCol, int[] sumDiag) throws GRBException {
        return mainDiagonalReflection(iterationRA(mainDiagonalReflection(I), sumCol, sumDiag));
    }

    private static boolean[][] iterationCD(boolean[][] I, int[] sumCol, int[] sumDiag) throws GRBException {
        return turnCounterClockwise(iterationRA(turnClockwise(I), sumCol, sumDiag));
    }

    private static boolean[][] iterationAR(boolean[][] I, int[] sumDiag, int[] sumRow) throws GRBException {
        final int[][] l = new int[diagN+n][diagN+n]; // all zeros

        final int[][] u = new int[diagN+n][diagN+n]; // fill top right diagN*n
        for (int i=0; i<diagN; i++) {
            for (int j=Math.max(diagN, diagN-(n-1)+i); j<Math.min(diagN+n, diagN+i+1); j++) {
                u[i][j] = 1;
            }
        }

        final int[][] c = new int[diagN+n][diagN+n]; // ones where I = false
        for (int i=0; i<n; i++) {
            for (int j=0; j<n; j++) {
                if (!I[i][j]) {
                    c[i+j][diagN+i] = 1;
                }
            }
        }

        final int[] b = concatArrays(sumDiag, minusArray(sumRow)); // concat sumDiag, -sumRow

        final int[][] f = Mincostflow.mincostflow(l, u, c, b);
        for (int i=0; i<n; i++) {
            for (int j=0; j<n; j++) {
                I[i][j] = (f[i+j][diagN+i] == 1);
            }
        }
        return I;
    }

    private static boolean[][] iterationAC(boolean[][] I, int[] sumDiag, int[] sumCol) throws GRBException {
        return mainDiagonalReflection(iterationAR(mainDiagonalReflection(I), sumDiag, sumCol));
    }

    private static boolean[][] iterationDC(boolean[][] I, int[] sumDiag, int[] sumCol) throws GRBException {
        return turnCounterClockwise(iterationAR(turnClockwise(I), sumDiag, sumCol));
    }

    private static int[] concatArrays(final int[] first, final int[] second) {
        final int[] result = new int[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    private static int[] minusArray(final int[] original) {
        final int[] result = new int[original.length];
        for (int i=0; i<original.length; i++) {
            result[i] = -original[i];
        }
        return result;
    }

    private static boolean[][] mainDiagonalReflection(final boolean[][] original) {
        final boolean[][] reflected = new boolean[original.length][original.length];
        for (int i=0; i<original.length; i++) {
            for (int j=0; j<original.length; j++) {
                reflected[j][i] = original[i][j];
            }
        }
        return reflected;
    }

    private static boolean[][] turnCounterClockwise(final boolean[][] original) {
        final boolean[][] turned = new boolean[original.length][original.length];
        for (int i=0; i<original.length; i++) {
            for (int j=0; j<original.length; j++) {
                turned[original.length-1-j][i] = original[i][j];
            }
        }
        return turned;
    }

    private static boolean[][] turnClockwise(final boolean[][] original) {
        boolean[][] turned = original;
        for (int i=0; i<3; i++) {
            turned = turnCounterClockwise(turned);
        }
        return turned;
    }

    private static void printI(boolean[][] I) {
        for (int i=0; i<I.length; i++) {
            for (int j=0; j<I[i].length; j++) {
                System.out.print(I[i][j] ? "1" : "0");
                if (j!= I[i].length-1) {
                    System.out.print(", ");
                }
            }
            System.out.println();
        }
    }

}
