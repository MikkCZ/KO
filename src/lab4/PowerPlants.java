package lab4;

import gurobi.*;

public class PowerPlants {
    public static void main(String[] args) throws Exception {
        // Create new environment.
        GRBEnv env = new GRBEnv();

        // Create empty optimization model.
        GRBModel model = new GRBModel(env);

        // Create variables x, y.
        // addVar(lowerBound, upperBound, objectiveCoeff, variableType, name)
        int d[] = new int[]{5, 5, 5, 5, 5, 10, 10, 15, 20, 20, 30, 30, 40, 50, 60, 60, 60, 50, 40, 30, 30, 20, 10, 5};
        int n_base = 10;    // pocet
        int e_base = 7;     // energie jedne
        int c_base = 2;     // cena jedne
        int n_peak = 10;    // pocet
        int e_peak = 7;     // energie jedne
        int c_peak = 2;     // cena jedne
        int s_max = 100;    // max kapacita
        double gamma = 0.75;    // efektivita

        GRBVar[] base = new GRBVar[n_base];
        GRBVar[][] peak = new GRBVar[n_peak][d.length];
        GRBVar[] s = new GRBVar[d.length];
        GRBVar[] put = new GRBVar[d.length];
        GRBVar[] take = new GRBVar[d.length];
        GRBVar[] x = new GRBVar[d.length]; // 1 kdyz ukladam
        for (int i=0; i<n_base; i++) { // base je bud zapnuta nebo vypnuta cely den
            base[i] = model.addVar(0, 1, c_base*d.length, GRB.BINARY, "base"+i);
        }
        for (int i=0; i<n_peak; i++) { // peak muze bezet kazdou hodinu
            for (int t=0; t<d.length; t++) {
                peak[i][t] = model.addVar(0, 1, c_peak, GRB.BINARY, "peak"+i+":"+t);
            }
        }
        for (int t=0; t<d.length; t++) {
            s[t] = model.addVar(0, s_max, 0, GRB.INTEGER, "storage"+t);
            put[t] = model.addVar(0, Integer.MAX_VALUE, 0, GRB.INTEGER, "put"+t);
            take[t] = model.addVar(0, Integer.MAX_VALUE, 0, GRB.INTEGER, "take"+t);
            x[t] = model.addVar(0, 1, 0, GRB.BINARY, "x"+t);
        }

        // Integrate new variables into model.
        model.update();

        for (int t=0; t<d.length; t++) { // splnim demand
            GRBLinExpr cons = new GRBLinExpr();
            for (int i=0; i<n_base; i++) {
                cons.addTerm(e_base, base[i]);
            }
            for (int i=0; i<n_peak; i++) {
                cons.addTerm(e_peak, peak[i][t]);
            }
            cons.addTerm(1, take[t]);
            cons.addTerm(-1, put[t]);
            model.addConstr(cons, GRB.EQUAL, d[t], "cons"+t);
        }

        for (int t=0; t<d.length-1; t++) { // uroven storage
            GRBLinExpr cons = new GRBLinExpr();
            cons.addTerm(1, s[t]);
            cons.addTerm(-1, take[t]);
            cons.addTerm(1, put[t]);
            model.addConstr(cons, GRB.EQUAL, s[t+1], "s"+t+1);
        }

        // TODO: big M - jen ber nebo ukladej
        int M = Integer.MAX_VALUE;
        for (int t=0; t<d.length; t++) { // uroven jen take nebo put
            GRBLinExpr cons = new GRBLinExpr();
            cons.addTerm(M, x[t]);
            model.addConstr(cons, GRB.GREATER_EQUAL, put[t], "put"+t);
        }

        // TODO: kriterium
    }
}
