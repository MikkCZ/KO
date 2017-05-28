package hw4;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;

class TourFinder {

    static Collection<int[]> findShortestTour(double[][] solution) {
        Collection<int[]> shortestTour = findTour(solution);
        for (int i=0; i<solution.length; i++) {
            if (!isInTour(i, shortestTour)) {
                Collection<int[]> t = findTour(solution, i);
                if (t.size() < shortestTour.size()) {
                    shortestTour = t;
                }
            }
        }
        return shortestTour;
    }

    static Collection<int[]> findTour(double[][] solution) {
        return findTour(solution, 0);
    }

    private static Collection<int[]> findTour(double[][] solution, int start) {
        final Collection<int[]> tour = new LinkedHashSet<>(solution.length);
        final Collection<Integer> visited = new HashSet<>(solution.length);
        int i=start;
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

    private static boolean isInTour(int i, Collection<int[]> tour) {
        for (int[] e : tour) {
            if (i == e[0] || i == e[1]) {
                return true;
            }
        }
        return false;
    }
}
