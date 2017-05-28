package hw5;

import java.io.*;
import java.util.*;

public class Hw5 {

    private static Task[] bestScheduleFound;
    private static int bestTimeFound = Integer.MAX_VALUE;

    public static void main(String[] args) throws IOException {

        Set<Task> inputTasks;

        // read input
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(args[0]))))) {
            final int n = Integer.parseInt(br.readLine());
            inputTasks = new LinkedHashSet<>(n);
            for (int i=0; i<n; i++) {
                final String[] line = br.readLine().split("\\s+");
                inputTasks.add(new Task(
                        i,
                        Integer.parseInt(line[0]),
                        Integer.parseInt(line[1]),
                        Integer.parseInt(line[2])
                ));
            }
        }

        branch(new Task[0], inputTasks);

        // output
        try(PrintStream output = new PrintStream(new File(args[1]))) {
            if (bestTimeFound == Integer.MAX_VALUE) {
                System.out.println("-1");
                output.println("-1");
            } else {
                System.out.println(bestTimeFound);
                int time = 0;
                for (Task t : bestScheduleFound) {
                    time = Math.max(time, t.getR());
                    t.setFinalStartTime(time);
                    time += t.getP();
                }
                // output
                for (Task t : inputTasks) {
                    System.out.printf("%s starts at time %d\n", t, t.getFinalStartTime());
                    output.println(t.getFinalStartTime());
                }
            }
        }
    }

    private static void branch(final Task[] scheduled, final Set<Task> unscheduled) {
        for (Task t : unscheduled) {
            final Task [] nextSchedule = new Task[scheduled.length+1];
            System.arraycopy(scheduled, 0, nextSchedule, 0, scheduled.length);
            nextSchedule[scheduled.length] = t;
            final Set<Task> nextUnscheduled = new HashSet<>(unscheduled.size());
            nextUnscheduled.addAll(unscheduled);
            nextUnscheduled.remove(t);
            step(nextSchedule, nextUnscheduled);
        }
    }

    private static void step(final Task[] scheduled, final Set<Task> unscheduled) {
        final int c = calculateC(scheduled);
        if (unscheduled.isEmpty()) {
            // completed, save result
            if (c < bestTimeFound) {
                bestTimeFound = c;
                bestScheduleFound = scheduled;
            }
        } else {
            if (checkDeadlines(c, unscheduled) &&
                    checkEstimate(c, unscheduled)) {
                // everything is OK, continue
                branch(scheduled, unscheduled);
            } else {
                // bound
            }
        }
    }

    private static int calculateC(final Task[] scheduled) {
        int c = 0;
        for (Task t : scheduled) {
            if (t.getR() > c) {
                c = t.getR();
            }
            c += t.getP();
        }
        return c;
    }

    private static boolean checkDeadlines(final int c, final Collection<Task> unscheduled) {
        for (Task t : unscheduled) {
            if (Math.max(c, t.getR())+t.getP() > t.getD()) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkEstimate(final int c, final Collection<Task> unscheduled) {
        int maxD = Integer.MIN_VALUE;
        int minR = Integer.MAX_VALUE;
        int sumP = 0;
        for (Task t : unscheduled) {
            maxD = Math.max(maxD, t.getD());
            minR = Math.min(minR, t.getR());
            sumP += t.getP();
        }
        final int UB = maxD;
        final int LB = Math.max(minR, c) + sumP;
        return UB >= LB && bestTimeFound > LB;
    }

}
