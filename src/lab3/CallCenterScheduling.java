package lab3;

import gurobi.*;

public class CallCenterScheduling {
    public static void main(String[] args) throws Exception {
        // Create new environment.
        GRBEnv env = new GRBEnv();

        // Create empty optimization model.
        GRBModel model = new GRBModel(env);

        // Create variables x, y.
        // addVar(lowerBound, upperBound, objectiveCoeff, variableType, name)
        int[] d = {6, 6, 6, 6, 6, 8, 9, 12, 18, 22, 25, 21, 21, 20, 18, 21, 21, 24, 24, 18, 18, 18, 12, 8};
        GRBVar[] x = new GRBVar[d.length];
        for (int i=0; i<d.length; i++) { // TODO: rewrite to streams
            x[i] = model.addVar(0, 100, 1, GRB.INTEGER, "x"+i);
        }

        // Integrate new variables into model.
        model.update();

        for (int i=0; i<=6; i++) {
            GRBLinExpr cons = new GRBLinExpr();
            for (int j = i+17; j<d.length; j++) {
                cons.addTerm(1, x[j]);
            }
            for (int j = 0; j<=i; j++) {
                cons.addTerm(1, x[j]);
            }
            model.addConstr(cons, GRB.GREATER_EQUAL, d[i], "cons"+i);
        }
        for (int i=7; i<d.length; i++) {
            GRBLinExpr cons = new GRBLinExpr();
            for (int j = i-7; j<=i; j++) {
                cons.addTerm(1, x[j]);
            }
            model.addConstr(cons, GRB.GREATER_EQUAL, d[i], "cons"+i);
        }

        // Solve the model.
        model.optimize();

        // Print the objective
        // and the values of the decision variables in the solution.
        for (GRBVar x_i : x) {
            System.out.printf("%s: %f.0\n", x_i.get(GRB.StringAttr.VarName), x_i.get(GRB.DoubleAttr.X));
        }
        System.out.printf("=== Obj: %f.0 + ===\n", model.get(GRB.DoubleAttr.ObjVal));

        for (int i=0; i<=6; i++) {
            int sum = 0;
            for (int j = i+17; j<d.length; j++) {
                sum += x[j].get(GRB.DoubleAttr.X);
            }
            for (int j = 0; j<=i; j++) {
                sum += x[j].get(GRB.DoubleAttr.X);
            }
            System.out.printf("hour %d: %d\n", i, sum);
        }
        for (int i=7; i<d.length; i++) {
            int sum = 0;
            for (int j = i-7; j<=i; j++) {
                sum += x[j].get(GRB.DoubleAttr.X);
            }
            System.out.printf("hour %d: %d\n", i, sum);
        }
    }
}
