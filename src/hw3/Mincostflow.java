package hw3;

import gurobi.*;

import java.util.HashSet;
import java.util.Set;

public class Mincostflow {

    public static int[][] mincostflow(int[][] l, int[][] u, int[][] c, int[] b) throws GRBException {
        final int dimension = verifyInput(l, u, c, b);

        // Create new environment.
        GRBEnv env = new GRBEnv();

        // Create empty optimization model.
        GRBModel model = new GRBModel(env);

        // Create variables from the input
        // addVar(lowerBound, upperBound, objectiveCoeff, variableType, name)
        GRBVar[][] f = new GRBVar[dimension][dimension];

        for (int i=0; i<dimension; i++) {
            for (int j=0; j<dimension; j++) {
                f[i][j] = model.addVar(l[i][j], u[i][j], c[i][j], GRB.INTEGER, "f"+i+","+j);
            }
        }

        // Integrate new variables into model.
        model.update();

        // Set objective
        {
            GRBLinExpr objective = new GRBLinExpr();
            for (int i=0; i<dimension; i++) {
                for (int j=0; j<dimension; j++) {
                    objective.addTerm(c[i][j], f[i][j]);
                }
            }
            model.setObjective(objective, GRB.MINIMIZE);
        }

        // balances
        {
            for (int i=0; i<dimension; i++) {
                GRBLinExpr left = new GRBLinExpr();
                for (int j=0; j<dimension; j++) {
                    left.addTerm(1, f[i][j]);
                    left.addTerm(-1, f[j][i]);
                }
                // addConstr(leftHandSide, inequalityType, rightHandSide, name)
                model.addConstr(left, GRB.EQUAL, b[i], "b"+i);
            }
        }

        // bounds
        {
            for (int i=0; i<dimension; i++) {
                for (int j=0; j<dimension; j++) {
                    // addConstr(leftHandSide, inequalityType, rightHandSide, name)
                    model.addConstr(l[i][j], GRB.LESS_EQUAL, f[i][j], "l"+i+","+j);
                    model.addConstr(f[i][j], GRB.LESS_EQUAL, u[i][j], "u"+i+","+j);
                }
            }
        }

        // Solve the model.
        model.optimize();

        // Check feasible.
        model.get(GRB.DoubleAttr.ObjVal);

        final int[][] results = new int[dimension][dimension];
        for (int i=0; i<dimension; i++) {
            for (int j=0; j<dimension; j++) {
                results[i][j] = (int)Math.round(f[i][j].get(GRB.DoubleAttr.X));
            }
        }
        return results;
    }

    private static int verifyInput(int[][] l, int[][] u, int[][] c, int[] b) throws IllegalArgumentException {
        Set<Integer> dimensions = new HashSet<>();
        dimensions.add(l.length); dimensions.add(l[0].length);
        dimensions.add(u.length); dimensions.add(u[0].length);
        dimensions.add(c.length); dimensions.add(c[0].length);
        dimensions.add(b.length);
        if (dimensions.size() != 1) {
            throw new IllegalArgumentException("Arguments dimensions do not correspond.");
        }
        return dimensions.iterator().next();
    }

}
