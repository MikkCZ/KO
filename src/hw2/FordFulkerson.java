package hw2;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;

import static java.lang.Math.min;

public class FordFulkerson {

    final static int FOR_ARC_INDEX = 0;
    final static int BAC_ARC_INDEX = 1;

    /**
     *
     * @param e node x 2 x _, every row corresponds to one node, first column contains out edges nodes, second in edges nodes
     * @param u node x node, upper bound of each edge
     * @param l node x node, lower bound of each edge
     * @param s index of start node
     * @param t index of target node
     * @param f initial flow
     */
    public static void fordFulkerson(int[][][] e, int[][] u, int[][] l, int s, int t, int[][] f) {
        Optional<AugmentingPath> augPathOptional = findPathBfs(e, u, l, s, t, f);
        while (augPathOptional.isPresent()) {
            final AugmentingPath augPath = augPathOptional.get();

            int delta = Integer.MAX_VALUE;
            {
                System.out.print("Found augmenting path: ");
                int j = t;
                while (j != s) {
                    System.out.print("" + j + " ");
                    final int i = augPath.visitedFrom[j];
                    int diff;
                    if (!augPath.visitedBackwards[j]) { // i->j
                        diff = u[i][j] - f[i][j];
                    } else { // j->i
                        diff = f[j][i] - l[j][i];
                    }
                    delta = min(delta, diff);
                    j = i;
                }
                System.out.println();
            }
            System.out.println("Found delta: " + delta);

            {
                int j = t;
                while (j != s) {
                    final int i = augPath.visitedFrom[j];
                    if (!augPath.visitedBackwards[j]) { // i->j
                        f[i][j] += delta;
                    } else { // j->i
                        f[j][i] -= delta;
                    }
                    j = i;
                }
            }
            System.out.println("Updated flow by delta: " + delta);

            augPathOptional = findPathBfs(e, u, l, s, t, f);
        }
    }

    private static Optional<AugmentingPath> findPathBfs(int[][][] e, int[][] u, int[][] l, int s, int t, int[][] f) {
        final Queue<Integer> toVisit = new LinkedList<>();
        toVisit.add(s);

        final int[] visitedFrom = new int[e.length];
        for (int i=0; i<visitedFrom.length; i++) {
            visitedFrom[i] = Integer.MIN_VALUE;
        }
        final boolean[] visitedBackwards = new boolean[e.length];
        final IntPredicate notEnqueued = j -> visitedFrom[j] < 0;

        while (visitedFrom[t] < 0 && !toVisit.isEmpty()) {
            System.out.println("In queue to visit:" + toVisit);
            final int i = toVisit.poll();
            final IntConsumer enqueue = (j) -> {
                visitedFrom[j] = i;
                toVisit.add(j);
            };
            final IntConsumer enqueueBackwards = (j) -> {
                enqueue.accept(j);
                visitedBackwards[j] = true;
            };
            { // arcs i->j
                if (e[i][FOR_ARC_INDEX] != null) {
                    Arrays.stream(e[i][FOR_ARC_INDEX])
                            .filter(notEnqueued)
                            .filter(j -> f[i][j] < u[i][j])
                            .forEach(enqueue);
                }
            }
            { // arcs j->i
                if (e[i][BAC_ARC_INDEX] != null) {
                    Arrays.stream(e[i][BAC_ARC_INDEX])
                            .filter(notEnqueued)
                            .filter(j -> f[j][i] > l[j][i])
                            .forEach(enqueueBackwards);
                }
            }
        }

        if (visitedFrom[t] < 0) {
            return Optional.empty();
        } else {
            return Optional.of(new AugmentingPath(visitedFrom, visitedBackwards));
        }
    }

}
