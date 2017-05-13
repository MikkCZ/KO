package coco.threshhold;

import coco.threshhold.runnables.ByOrderRunnable;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CocoThr {

    // NOTE: the persons index in the code is always one less than the index in the input/output file

    private static int n, m; // n = number of persons, m = number of bills
    private static int allBillSum; // sum of all bills
    private static Person[] persons; // sum of bills for each person
    private static double s; // average each person should pay in total

    public static void main(String[] args) throws IOException, InterruptedException {

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
        }

        // calculate balances
        for (Person p : persons) {
            p.averageIs(s);
        }

        // TODO: run more variants of sorting and reordering in parallel
        final int totalRunnables = 2; // set to size of the number of runnables
        final ConcurrentMap<Integer, String> resultMap = new ConcurrentHashMap<>(totalRunnables);

        final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        executor.execute(new ByOrderRunnable(resultMap, clone(persons), new DescendingBalanceFirstPersonComparator(),true));
        executor.execute(new ByOrderRunnable(resultMap, clone(persons), new DescendingBalanceFirstPersonComparator(), false));

        executor.shutdown();
        while (!executor.isTerminated()) {
            Thread.sleep(10);
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
