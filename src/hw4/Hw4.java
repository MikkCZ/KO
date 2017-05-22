package hw4;

import gurobi.*;

import java.io.*;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Hw4 {

    private static int n, w, m;
    private static int[][][] stripes;

    public static void main(String[] args) throws IOException, GRBException {
        // read input
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(args[0]))))) {
            String[] firstLine = br.readLine().split("\\s+");

            n = Integer.parseInt(firstLine[0]);
            w = Integer.parseInt(firstLine[1]);
            m = Integer.parseInt(firstLine[2]);

            stripes = new int[n][m][w];
            for (int i=0; i<n; i++) {
                final String[] line = br.readLine().split("\\s+");
                final int[][] stripe = new int[m][3*w];
                for (int j=0; j<m; j++) {
                    for (int k=0; k<3*w; k++) {
                        stripe[j][k] = Integer.parseInt(line[j*3*w + k]);
                    }
                }
                stripes[i] = stripe;
            }
        }

        final int[][] distances = calculateDistancesWithDummyNodeLast(stripes);
        final int[] solution = removeDummyNode(solveTsp(distances));

        final String result = Arrays.stream(solution)
                .mapToObj(Integer::toString)
                .collect(Collectors.joining(" "));

        // output
        try(PrintStream output = new PrintStream(new File(args[1]))) {
            output.println(result);
        }
    }

    private static int[] solveTsp(final int[][] c) throws GRBException {
        // Create new environment.
        GRBEnv env = new GRBEnv();

        // Create empty optimization model.
        GRBModel model = new GRBModel(env);
        model.getEnv().set(GRB.IntParam.LazyConstraints, 1);

        // addVar(lowerBound, upperBound, objectiveCoeff, variableType, name)
        final GRBVar[][] x = new GRBVar[c.length][c.length];

        for (int i=0; i<c.length; i++) {
            for (int j=0; j<c.length; j++) {
                if (i==j) {
                    x[i][j] = model.addVar(0, 0, c[i][j], GRB.BINARY, "x" + i + "," + j);
                } else {
                    x[i][j] = model.addVar(0, 1, c[i][j], GRB.BINARY, "x" + i + "," + j);
                }
            }
        }

        model.update();

        // Set objective
        {
            GRBLinExpr objective = new GRBLinExpr();
            for (int i=0; i<c.length; i++) {
                for (int j=0; j<c.length; j++) {
                    objective.addTerm(c[i][j], x[i][j]);
                }
            }
            model.setObjective(objective, GRB.MINIMIZE);
        }

        { // 14
            for (int i=0; i<c.length; i++) {
                GRBLinExpr left = new GRBLinExpr();
                for (int j=0; j<c.length; j++) {
                    left.addTerm(1, x[i][j]);
                }
                model.addConstr(left, GRB.EQUAL, 1, "x" + i + "j = 1");
            }
        }

        { // 15
            for (int j=0; j<c.length; j++) {
                GRBLinExpr left = new GRBLinExpr();
                for (int i=0; i<c.length; i++) {
                    left.addTerm(1, x[i][j]);
                }
                model.addConstr(left, GRB.EQUAL, 1, "x" + j + "i = 1");
            }
        }

        model.setCallback(new Callback(x));
        model.optimize();

        final double[][] solution = new double[c.length][c.length];
        for (int i=0; i<c.length; i++) {
            for (int j=0; j<c.length; j++) {
                solution[i][j] = x[i][j].get(GRB.DoubleAttr.X);
            }
        }

        return TourFinder.tourToOrder(TourFinder.findTour(solution));
    }

    private static int[] removeDummyNode(int[] solutionWithDummyNode) {
        final int dummyNode = solutionWithDummyNode.length-1;
        int dummyNodeIndex = 0;
        for (int i=0; i<solutionWithDummyNode.length; i++) {
            if (solutionWithDummyNode[i] == dummyNode) {
                dummyNodeIndex = i;
                break;
            }
        }

        final int[] cleanSolution = new int[solutionWithDummyNode.length-1];
        int index = 0;
        for (int i=dummyNodeIndex+1; i<solutionWithDummyNode.length; i++) {
            cleanSolution[index++] = solutionWithDummyNode[i];
        }
        for (int i=0; i<dummyNodeIndex; i++) {
            cleanSolution[index++] = solutionWithDummyNode[i];
        }

        return cleanSolution;
    }

    private static int[][] calculateDistancesWithDummyNodeLast(final int[][][] stripes) {
        final int[][] distances = new int[stripes.length+1][stripes.length+1];
        for (int i=0; i<stripes.length; i++) {
            for (int j=0; j<stripes.length; j++) {
                distances[i][j] = calculateDistance(stripes[i], stripes[j]);
            }
        }
        return distances;
    }

    private static int calculateDistance(final int[][] stripeA, final int[][] stripeB) {
        int distance = 0;
        for (int i=0; i<m; i++) {
            for (int j=0; j<3; j++) {
                distance += Math.abs(stripeA[i][(3*w)-3+j] - stripeB[i][j]);
            }
        }
        return distance;
    }

}
