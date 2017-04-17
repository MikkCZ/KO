package lab3;

import gurobi.*;

import java.io.*;

public class CallCenterSchedulingHW {
    public static void main(String[] args) throws Exception {
        int[] demand;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(args[0]))))) {
            String[] strDemands = br.readLine().split("\\s+");
            demand = new int[strDemands.length];
            for (int i = 0; i < strDemands.length; i++) {
                demand[i] = Integer.parseInt(strDemands[i]);
            }
        }

        final GRBEnv env = new GRBEnv();
        final GRBModel model = new GRBModel(env);

        // Create variables x, y.
        // addVar(lowerBound, upperBound, objectiveCoeff, variableType, name)
        final GRBVar[] x = new GRBVar[demand.length];
        final GRBVar[] z = new GRBVar[demand.length];
        final GRBVar[] d = new GRBVar[demand.length];
        for (int i = 0; i < demand.length; i++) { // TODO: rewrite to streams
            x[i] = model.addVar(0, Integer.MAX_VALUE, 0, GRB.INTEGER, "x" + i);
            z[i] = model.addVar(0, Integer.MAX_VALUE, 1, GRB.INTEGER, "z" + i);
            d[i] = model.addVar(demand[i], demand[i], 0, GRB.INTEGER, "d" + i);
        }

        // Integrate new variables into model.
        model.update();

        for (int i = 0; i < demand.length; i++) {
            final GRBLinExpr consPlus = new GRBLinExpr();
            final GRBLinExpr consMinus = new GRBLinExpr();
            consPlus.addTerm(1, d[i]);
            consMinus.addTerm(-1, d[i]);
            for (int j = i - 7; j <= i; j++) {
                final int index = ((j + 24) % 24);
                consPlus.addTerm(-1, x[index]);
                consMinus.addTerm(1, x[index]);
            }
            model.addConstr(consPlus, GRB.LESS_EQUAL, z[i], "consPlus" + i);
            model.addConstr(consMinus, GRB.LESS_EQUAL, z[i], "consMinus" + i);
        }

        // Solve the model.
        model.optimize();

        // Print the objective
        // and the values of the decision variables in the solution.
        final PrintStream output = new PrintStream(new File(args[1]));
        output.println(new Double(model.get(GRB.DoubleAttr.ObjVal)).intValue());
        for (GRBVar x_i : x) {
            output.print(new Double(x_i.get(GRB.DoubleAttr.X)).intValue());
            if (x_i != x[demand.length - 1]) {
                output.print(" ");
            }
        }
    }
}
