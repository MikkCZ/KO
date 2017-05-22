package hw4;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;

class TourFinder {

    static Collection<int[]> findTour(double[][] solution) {
        final Collection<int[]> tour = new LinkedHashSet<>(solution.length);
        final Collection<Integer> visited = new HashSet<>(solution.length);
        int i=0;
        visited.add(i);
        boolean tourClosed = false;
        while (!tourClosed) {
            for (int j=0; j<solution.length; j++) {
                if ((int)Math.round(solution[i][j]) == 1) {
                    tour.add(new int[] {i, j});
                    if (visited.contains(j)) {
                        tourClosed = true;
                    } else {
                        visited.add(j);
                        i = j;
                    }
                    break;
                }
            }
        }
        return tour;
    }

    static int[] tourToOrder(Collection<int[]> tour) {
        final int[] order = new int[tour.size()];
        int i = 0;
        for (int[] e : tour) {
            order[i++] = e[0];
        }
        return order;
    }
}
