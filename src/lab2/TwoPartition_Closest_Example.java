package lab2;

import gurobi.*;

public class TwoPartition_Closest_Example {
    public static void main(String[] args) throws Exception {
        /*
        max -x1 + 2*x2
        s.t.
        2*x1 + x2 <= 5
        -4*x1 + 4*x2 <=5
        */

        // Create new environment.
        GRBEnv env = new GRBEnv();

        // Create empty optimization model.
        GRBModel model = new GRBModel(env);

        // Create variables x, y.
        // addVar(lowerBound, upperBound, objectiveCoeff, variableType, name)
        int[] p = {100, 50, 50, 50, 20, 20, 10, 10};

        GRBVar s = model.addVar(0, GRB.INFINITY, 1, GRB.INTEGER, "s");
        GRBVar[] x = new GRBVar[p.length];
        for (int i=0; i<p.length; i++) {
            x[i] = model.addVar(0, 1, 0, GRB.INTEGER, "x"+i);
        }

        // Integrate new variables into model.
        model.update();

        // Set objective: maximize 32x + 25y
        GRBLinExpr obj = new GRBLinExpr();
        model.setObjective(obj, GRB.MINIMIZE);

        {
            GRBLinExpr cons1 = new GRBLinExpr();
            for (int i = 0; i < p.length; i++) {
                cons1.addTerm(p[i], x[i]);
            }
            model.addConstr(cons1, GRB.LESS_EQUAL, s, "cons1");
        }

        {
            GRBLinExpr cons1 = new GRBLinExpr();
            for (int i = 0; i < p.length; i++) {
                cons1.addTerm(p[i], x[i]);
            }
            model.addConstr(cons1, GRB.LESS_EQUAL, s, "cons1");
        }

        // Solve the model.
        model.optimize();

        // Print the objective
        // and the values of the decision variables in the solution.
        System.out.println(s.get(GRB.StringAttr.VarName)+ " " +s.get(GRB.DoubleAttr.X));
        for (GRBVar x1 : x) {
            System.out.println(x1.get(GRB.StringAttr.VarName)+ " " +x1.get(GRB.DoubleAttr.X));
        }
        System.out.println("Obj: " + model.get(GRB.DoubleAttr.ObjVal));
    }
}
