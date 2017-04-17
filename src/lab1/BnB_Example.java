package lab1;

import gurobi.*;

public class BnB_Example {
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
        GRBVar x1 = model.addVar(0.0, GRB.INFINITY, 0.0, GRB.INTEGER, "x1");
        GRBVar x2 = model.addVar(0.0, GRB.INFINITY, 0.0, GRB.INTEGER, "x2");

        // Integrate new variables into model.
        model.update();

        // Set objective: maximize 32x + 25y
        GRBLinExpr obj = new GRBLinExpr();
        obj.addTerm(-1, x1);
        obj.addTerm(2, x2);
        model.setObjective(obj, GRB.MAXIMIZE);

        {
            // Add constraint: 5x + 4y <= 59
            GRBLinExpr cons1 = new GRBLinExpr();
            cons1.addTerm(2, x1);
            cons1.addTerm(1, x2);
            // addConstr(leftHandSide, inequalityType, rightHandSide, name)
            model.addConstr(cons1, GRB.LESS_EQUAL, 5, "cons1");
        }

        {
            // Add constraint: 4x + 3y <= 46
            GRBLinExpr cons2 = new GRBLinExpr();
            cons2.addTerm(-4, x1);
            cons2.addTerm(4, x2);
            model.addConstr(cons2, GRB.LESS_EQUAL, 5, "cons2");
        }

        // Solve the model.
        model.optimize();

        // Print the objective
        // and the values of the decision variables in the solution.
        System.out.println(x1.get(GRB.StringAttr.VarName)+ " " +x1.get(GRB.DoubleAttr.X));
        System.out.println(x2.get(GRB.StringAttr.VarName) + " " +x2.get(GRB.DoubleAttr.X));
        System.out.println("Obj: " + model.get(GRB.DoubleAttr.ObjVal));
    }
}
