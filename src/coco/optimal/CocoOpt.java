package coco.optimal;

import gurobi.*;

import java.io.*;

public class CocoOpt {

    // NOTE: the persons index in the code is always one less than the index in the input/output file

    private static int n, m; // n = number of persons, m = number of bills
    private static int allBillSum; // sum of all bills
    private static int[] b; // sum of bills for each person
    private static double s; // average each person should pay in total

    public static void main(String[] args) throws IOException, GRBException {

        // read input
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(args[0]))))) {
            String[] counts = br.readLine().split("\\s+");
            n = Integer.parseInt(counts[0]);
            m = Integer.parseInt(counts[1]);

            b = new int[n];

            String[] bills = br.readLine().split("\\s+");
            String[] persons = br.readLine().split("\\s+");

            for (int i=0; i<m; i++) {
                int bill = Integer.parseInt(bills[i]);
                b[Integer.parseInt(persons[i])-1] += bill;
                allBillSum += bill;
            }
            s = ((double)allBillSum)/n;
        }

        // Create new environment.
        GRBEnv env = new GRBEnv();

        // Create empty optimization model.
        GRBModel model = new GRBModel(env);

        // Create variables from the input
        GRBVar[][] t = new GRBVar[n][n]; // has there been a payment between p->q
        GRBVar[][] a = new GRBVar[n][n]; // amount of the payment between p->q

        // addVar(lowerBound, upperBound, objectiveCoeff, variableType, name)
        for (int i=0; i<n; i++) {
            for (int j=0; j<n; j++) {
                t[i][j] = model.addVar(0, 1, 1, GRB.BINARY, "t"+i+","+j);
                a[i][j] = model.addVar(0, allBillSum, 0, GRB.CONTINUOUS, "a"+i+","+j);
            }
        }

        // Integrate new variables into model.
        model.update();
        //model.write("debug.lp");

        // for each person, add balance
        for (int i=0; i<n; i++) {
            GRBLinExpr left = new GRBLinExpr();
            for (int j=0; j<n; j++) {
                left.addTerm(1, a[i][j]);
            }
            for (int j=0; j<n; j++) {
                left.addTerm(-1, a[j][i]);
            }
            left.addConstant(b[i]);
            GRBLinExpr right = new GRBLinExpr();
            right.addConstant(s);
            // addConstr(leftHandSide, inequalityType, rightHandSide, name)
            model.addConstr(left, GRB.EQUAL, right, "pay"+i);
        }

        // link t with a
        for (int i=0; i<n; i++) {
            for (int j=0; j<n; j++) {
                GRBLinExpr left = new GRBLinExpr();
                left.addTerm(allBillSum, t[i][j]);
                GRBLinExpr right = new GRBLinExpr();
                right.addTerm(1, a[i][j]);
                model.addConstr(left, GRB.GREATER_EQUAL, right, "t"+i+","+j);
            }
        }

        // Solve the model.
        model.optimize();

        // output
        try(PrintStream output = new PrintStream(new File(args[1]))) {
            output.println(Math.round(model.get(GRB.DoubleAttr.ObjVal)));
            for (int i=0; i<n; i++) {
                for (int j=0; j<n; j++) {
                    double value = a[i][j].get(GRB.DoubleAttr.X);
                    if (value > 0) {
                        output.printf("%1$d %2$d %3$f\n", i+1, j+1, value);
                    }
                }
            }
        }
    }
}
