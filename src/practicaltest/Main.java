package practicaltest;

import gurobi.*;

import java.io.*;

public class Main {

    private static final int MACHINES = 3;
    private static final int PRODUCTS = 17;

    private static int C, C1, C23;
    private static int[] t_input = new int[PRODUCTS];
    private static int[] e_input = new int[3];
    private static int[][] c_input = new int[MACHINES][PRODUCTS];
    private static int[][] p_input = new int[MACHINES][PRODUCTS];
    private static int M;

    public static void main(String[] args) throws Exception {
        // read input
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(args[0]))))) {
            C = Integer.parseInt(br.readLine());
            C1 = Integer.parseInt(br.readLine());
            C23 = Integer.parseInt(br.readLine());

            String[] t_strings = br.readLine().split("\\s+");
            for (int j=0; j<PRODUCTS; j++) {
                t_input[j] = Integer.parseInt(t_strings[j]);
            }

            String[] e_strings = br.readLine().split("\\s+");
            for (int i=0; i<3; i++) {
                e_input[i] = Integer.parseInt(e_strings[i]);
            }

            for (int i=0; i<MACHINES; i++) {
                String[] c_strings = br.readLine().split("\\s+");
                for (int j=0; j<PRODUCTS; j++) {
                    c_input[i][j] = Integer.parseInt(c_strings[j]);
                }
            }

            for (int i=0; i<MACHINES; i++) {
                String[] p_strings = br.readLine().split("\\s+");
                for (int j=0; j<PRODUCTS; j++) {
                    p_input[i][j] = Integer.parseInt(p_strings[j]);
                }
            }
        }
        M = 0;
        for (int j=0; j<PRODUCTS; j++) {
            int tmp = 0;
            for (int i=0; i<MACHINES; i++) {
                tmp += c_input[i][j];
            }
            M += tmp*t_input[j];
        }

        // Create new environment.
        GRBEnv env = new GRBEnv();

        // Create empty optimization model.
        GRBModel model = new GRBModel(env);

        // Create variables from the input
        // addVar(lowerBound, upperBound, objectiveCoeff, variableType, name)

        // z
        GRBVar z = model.addVar(0, GRB.INFINITY, 1.0, GRB.INTEGER, "z");
        // x(i,j)
        GRBVar[][] x = new GRBVar[MACHINES][PRODUCTS];
        for (int i=0; i<MACHINES; i++) {
            for (int j=0; j<PRODUCTS; j++) {
                x[i][j] = model.addVar(0, GRB.INFINITY, 0, GRB.INTEGER, "x"+i+","+j);
            }
        }
        // k(i)
        GRBVar[] k = new GRBVar[PRODUCTS];
        for (int j=0; j<PRODUCTS; j++) {
            k[j] = model.addVar(0, GRB.INFINITY, 0, GRB.INTEGER, "k"+j);
        }
        // y
        GRBVar y = model.addVar(0, GRB.INFINITY, 0, GRB.BINARY, "y");

        // Integrate new variables into model.
        model.update();

        // Set objective: min z
        {
            GRBLinExpr objective = new GRBLinExpr();
            objective.addTerm(1, z);
            model.setObjective(objective, GRB.MINIMIZE);
        }

        // z >= ... (a)
        {
            GRBLinExpr left = new GRBLinExpr();
            left.addTerm(1, z);
            GRBLinExpr right = new GRBLinExpr();
            for (int j=0; j<PRODUCTS; j++) {
                right.addTerm(p_input[0][j], x[0][j]);
                right.addTerm(-p_input[2][j], x[2][j]);
            }
            // addConstr(leftHandSide, inequalityType, rightHandSide, name)
            model.addConstr(left, GRB.GREATER_EQUAL, right, "f)1");
        }
        // z >= ... (b)
        {
            GRBLinExpr left = new GRBLinExpr();
            left.addTerm(1, z);
            GRBLinExpr right = new GRBLinExpr();
            for (int j=0; j<PRODUCTS; j++) {
                right.addTerm(-p_input[0][j], x[0][j]);
                right.addTerm(p_input[2][j], x[2][j]);
            }
            // addConstr(leftHandSide, inequalityType, rightHandSide, name)
            model.addConstr(left, GRB.GREATER_EQUAL, right, "f)2");
        }
        // ei >= sum{x(i,j)}, Vi € 1,2,3; Vj
        {
            for (int i=0; i<MACHINES; i++) {
                GRBLinExpr right = new GRBLinExpr();
                for (int j=0; j<PRODUCTS; j++) {
                    right.addTerm(1, x[i][j]);
                }
                // addConstr(leftHandSide, inequalityType, rightHandSide, name)
                model.addConstr(e_input[i], GRB.GREATER_EQUAL, right, "e)"+i);
            }
        }
        // C >= sum{x(i,j)*c(i,j)}
        {
            GRBLinExpr right = new GRBLinExpr();
            for (int i=0; i<MACHINES; i++) {
                for (int j=0; j<PRODUCTS; j++) {
                    right.addTerm(c_input[i][j], x[i][j]);
                }
            }
            // addConstr(leftHandSide, inequalityType, rightHandSide, name)
            model.addConstr(C, GRB.GREATER_EQUAL, right, "a)");
        }
        // t(j) <= sum{x(i,j)}
        {
            for (int j=0; j<PRODUCTS; j++) {
                GRBLinExpr right = new GRBLinExpr();
                for (int i=0; i<MACHINES; i++) {
                    right.addTerm(1, x[i][j]);
                }
                // addConstr(leftHandSide, inequalityType, rightHandSide, name)
                model.addConstr(t_input[j], GRB.LESS_EQUAL, right, "b)"+j);
            }
        }
        // x(3,j) = 7*k
        {
            for (int j=0; j<PRODUCTS; j++) {
                GRBLinExpr right = new GRBLinExpr();
                right.addTerm(7, k[j]);
                // addConstr(leftHandSide, inequalityType, rightHandSide, name)
                model.addConstr(x[2][j], GRB.EQUAL, right, "c)"+j);
            }
        }
        // x(1,j)*c(1,j) <= C1 + (1-y)*M
        {
            GRBLinExpr left = new GRBLinExpr();
            for (int j=0; j<PRODUCTS; j++) {
                left.addTerm(c_input[0][j], x[0][j]);
            }
            GRBLinExpr right = new GRBLinExpr();
            right.addConstant(C1);
            right.addConstant(M);
            right.addTerm(-M, y);
            // addConstr(leftHandSide, inequalityType, rightHandSide, name)
            model.addConstr(left, GRB.LESS_EQUAL, C1, "d)"+1);
        }
        // x(2,j)*c(2,j) + x(3,j)*c(3,j) <= C23 + y*M
        {
            GRBLinExpr left = new GRBLinExpr();
            for (int j=0; j<PRODUCTS; j++) {
                left.addTerm(c_input[1][j], x[1][j]);
                left.addTerm(c_input[2][j], x[2][j]);
            }
            GRBLinExpr right = new GRBLinExpr();
            right.addConstant(C23);
            right.addTerm(M, y);
            // addConstr(leftHandSide, inequalityType, rightHandSide, name)
            model.addConstr(left, GRB.LESS_EQUAL, right, "d)"+23);
        }

        // Solve the model.
        model.optimize();

        // output
        try(PrintStream output = new PrintStream(new File(args[1]))) {
            try {
                model.get(GRB.DoubleAttr.ObjVal);
            } catch (GRBException e) {
                // infeasible
                output.println("-1");
                return;
            }
            output.println((int)Math.round(model.get(GRB.DoubleAttr.ObjVal)));
            for (int i=0; i<MACHINES; i++) {
                for (int j=0; j<PRODUCTS; j++) {
                    output.print((int)Math.round(x[i][j].get(GRB.DoubleAttr.X)));
                    if (j<PRODUCTS-1) {
                        output.print(' ');
                    }
                }
                output.println();
            }
        }
    }
}
