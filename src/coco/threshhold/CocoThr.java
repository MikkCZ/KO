package coco.threshhold;

import coco.threshhold.runnables.ByOrderRunnable;

import java.io.*;
import java.util.Map;
import java.util.concurrent.*;

public class CocoThr {

    private static final long OUTPUT_MILLIS = 100;

    // NOTE: the persons index in the code is always one less than the index in the input/output file

    private static int n, m; // n = number of persons, m = number of bills
    private static int allBillSum; // sum of all bills
    private static Person[] persons; // sum of bills for each person
    private static double s; // average each person should pay in total

    public static void main(String[] args) {

        final long start = System.currentTimeMillis();
        final long timeLimit = Integer.parseInt(args[2])*1000;

        // read input
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(args[0]))))) {
            String[] counts = br.readLine().split("\\s+");
            n = Integer.parseInt(counts[0]);
            m = Integer.parseInt(counts[1]);

            persons = new Person[n];
            for (int i=0; i<n; i++) {
                persons[i] = new Person(i+1);
            }

            String[] bills = br.readLine().split("\\s+");
            String[] personString = br.readLine().split("\\s+");

            for (int i=0; i<m; i++) {
                int bill = Integer.parseInt(bills[i]);
                persons[Integer.parseInt(personString[i])-1].addBill(bill);
                allBillSum += bill;
            }
            s = ((double)allBillSum)/n;
        } catch (IOException e) {
            throw new RuntimeException("Error reading input.", e);
        }

        // calculate balances
        for (Person p : persons) {
            p.averageIs(s);
        }

        final int totalRunnables = 6; // set to size of the number of runnables
        final ConcurrentMap<Integer, String> resultMap = new ConcurrentHashMap<>(totalRunnables);

        final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        { // swap negative -> {100, 10, 0, -100, -10}
            executor.execute(new ByOrderRunnable(resultMap, clone(persons), new DescendingBalanceFirstPersonComparator(true),true));
            executor.execute(new ByOrderRunnable(resultMap, clone(persons), new DescendingBalanceFirstPersonComparator(true), false));
        }
        { // do not swap negative -> {100, 10, 0, -10, -100}
            executor.execute(new ByOrderRunnable(resultMap, clone(persons), new DescendingBalanceFirstPersonComparator(false),true));
            executor.execute(new ByOrderRunnable(resultMap, clone(persons), new DescendingBalanceFirstPersonComparator(false), false));
        }
        { // positive before negative -> {100, 10, 100, 0, -100, -10, -100}
            executor.execute(new ByOrderRunnable(resultMap, clone(persons), new PlusMinusArraySplitPersonComparator(),true));
            executor.execute(new ByOrderRunnable(resultMap, clone(persons), new PlusMinusArraySplitPersonComparator(), false));
        }

        try {
            executor.shutdown();
            long timeLeft = timeLimit - (System.currentTimeMillis() - start);
            executor.awaitTermination(timeLeft - OUTPUT_MILLIS, TimeUnit.MILLISECONDS);
        } catch (Exception ignored) {
            System.err.println(ignored);
        }

        int best = Integer.MAX_VALUE;
        for (Map.Entry<Integer, String> entry : resultMap.entrySet()) {
            if (entry.getKey() < best) {
                best = entry.getKey();
            }
        }

        // output
        try(PrintStream output = new PrintStream(new File(args[1]))) {
            output.print(resultMap.get(best));
        } catch (IOException e) {
            throw new RuntimeException(String.format("Error writing output.\n---\n%1$s\n---\n", resultMap.get(best)), e);
        } finally {
            System.out.printf("Elapsed time: %1$d ms\n", System.currentTimeMillis() - start);
            executor.shutdownNow();
        }
    }

    private static Person[] clone(Person[] origin) {
        final Person[] clone = new Person[origin.length];
        for (int i=0; i<origin.length; i++) {
            clone[i] = origin[i].clone();
        }
        return clone;
    }

}
