package hw4;

import gurobi.*;

import java.util.Collection;

public class Callback extends GRBCallback {

    private final GRBVar[][] vars;

    Callback(GRBVar[][] x) {
        this.vars = x;
    }

    @Override
    protected void callback() {
        if (where == GRB.CB_MIPSOL) {
            try {
                final Collection<int[]> tour = findTour(getSolution(vars));
                if (tour.size() < vars.length) {
                    GRBLinExpr left = new GRBLinExpr();
                    tour.forEach(e -> left.addTerm(1, vars[e[0]][e[1]]));
                    addLazy(left, GRB.LESS_EQUAL, tour.size()-1);
                }
            } catch (GRBException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Collection<int[]> findTour(double[][] solution) {
        return TourFinder.findTour(solution);
    }
}
