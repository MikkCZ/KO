package lab5;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CzechRepublic {
    public static void main(String[] args) throws Exception {
        InputStream is = System.in;
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        String[] lineArray;

        br.readLine(); // skip the first line containing number of points

        final List<Integer> xList = new ArrayList<>();
        final List<Integer> yList = new ArrayList<>();
        while((line = br.readLine()) != null) {
            lineArray = line.split("\\s+");
            xList.add(Integer.parseInt(lineArray[0]));
            yList.add(Integer.parseInt(lineArray[1]));
        }

        //final double[] x = new double[]{0, 1.26, 2.51, 3.77, 5.03, 6.28};
        //final double[] y = new double[]{0.01, 1.16, 0.70, -0.34, -0.80, 0.2100};
        final Integer[] x = (Integer[])xList.toArray();
        final Integer[] y = (Integer[])yList.toArray();
        final double[][] G = new double[x.length][y.length];

        final double beta = 1/2000;
        // Create variables x, y.
        // addVar(lowerBound, upperBound, objectiveCoeff, variableType, name)
        for (int i = 0; i<x.length; i++) {
            for (int j=0; j<=i; j++) {
                G[i][j] = Integer.MAX_VALUE;
            }
            for (int j = i+1; j<x.length; j++) {
                G[i][j] = cij(beta, x, y, i, j);
            }
        }
    }

    private static double cij(double beta, Integer[] x, Integer[] y, int i, int j) {
        double cij = 1 + beta*vzorecek(x, y, i, j);
        return cij;
    }

    private static double vzorecek(Integer[] x, Integer[] y, int i, int j) {
        double suma = 0;
        for (int k=i+1; k<j; k++) {
            suma += Math.sqrt(
                    (y[j]-y[i])*x[k] - (x[j]-x[i])*y[k] + x[j]*y[i] - y[j]*x[i]
            ) / (
                    Math.pow(y[j]-y[i],2) + Math.pow(x[j]-x[i],2)
            );
        }
        return suma;
    }

    private static double[] bellmanFord(double[][] G) {
        //for (int k = )
        return null;
    }
}
