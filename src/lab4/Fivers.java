package lab4;

import gurobi.*;

public class Fivers {
    public static void main(String[] args) throws Exception {
        // Create new environment.
        GRBEnv env = new GRBEnv();

        // Create empty optimization model.
        GRBModel model = new GRBModel(env);

        // Create variables x, y.
        // addVar(lowerBound, upperBound, objectiveCoeff, variableType, name)
        int n = 18;
        GRBVar[][] x = new GRBVar[n+2][n+2];
        GRBVar[][] k = new GRBVar[n+2][n+2];
        for (int i=0; i<n+2; i++) {
            for (int j=0; j<n+2; j++) {
                if (i == 0 || i == n+1 || j == 0 || j == n+1) { // okoli
                    x[i][j] = model.addVar(0,0,0,GRB.BINARY, "x"+i+j);
                } else { // skutecna mrizka
                    x[i][j] = model.addVar(0,1,0,GRB.BINARY, "x"+i+j);
                    k[i][j] = model.addVar(0,Integer.MAX_VALUE,0,GRB.INTEGER, "k"+i+j);
                }
            }
        }

        // Integrate new variables into model.
        model.update();

        for (int i=1; i<n+1; i++) {
            for (int j=1; j<n+1; j++) {
                GRBLinExpr cons = new GRBLinExpr();
                cons.addTerm(1, x[i][j]);
                cons.addTerm(1, x[i-1][j]);
                cons.addTerm(1, x[i+1][j]);
                cons.addTerm(1, x[i][j-1]);
                cons.addTerm(1, x[i][j+1]);
                cons.addTerm(-2, k[i][j]);
                model.addConstr(cons, GRB.EQUAL, 1, "cons"+i+j);
            }
        }

        GRBLinExpr obj = new GRBLinExpr();
        for (int i=1; i<n+1; i++) {
            for (int j=1; j<n+1; j++) {
                obj.addTerm(1, x[i][j]);
            }
        }
        model.setObjective(obj, GRB.MINIMIZE);

        // Solve the model.
        model.optimize();

        // Print the objective
        // and the values of the decision variables in the solution.
        for (int i=1; i<n+1; i++) {
            for (int j=1; j<n+1; j++) {
                System.out.printf("%s: %f.0\n", x[i][j].get(GRB.StringAttr.VarName), x[i][j].get(GRB.DoubleAttr.X));
            }
        }
        for (int i=1; i<n+1; i++) {
            System.out.print("|");
            for (int j=1; j<n+1; j++) {
                if(x[i][j].get(GRB.DoubleAttr.X) == 1) {
                    System.out.print("x");
                } else {
                    System.out.print(" ");
                }
                System.out.print("|");
            }
            System.out.println();
        }
        System.out.printf("=== Obj: %f.0 + ===\n", model.get(GRB.DoubleAttr.ObjVal));
    }
}
