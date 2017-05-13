package coco.threshhold.runnables;

import coco.threshhold.Person;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

public class ByOrderRunnable implements Runnable {

    private final ConcurrentMap<Integer, String> resultMap;
    private final Person[] persons;
    private final boolean reorderInEachStep;

    public ByOrderRunnable(ConcurrentMap<Integer, String> resultMap, Person[] persons, boolean reorderInEachStep) {
        this.resultMap = resultMap;
        this.persons = persons;
        this.reorderInEachStep = reorderInEachStep;
    }

    @Override
    public void run() {
        int transactions = 0;
        final StringBuilder sb = new StringBuilder();

        // split to those who haven't paid enough (plus) and those, who have overpaid (minus) and get them sorted
        Person[][] plusMinus = splitToPlusAndMinus(persons);
        int personsLeft;
        do {
            final Person plusP = plusMinus[0][0];
            final Person minusP = plusMinus[1][0];
            final double transaction = Math.min(plusP.getBalance(), -minusP.getBalance());
            plusP.sendMoney(transaction);
            minusP.acceptMoney(transaction);
            transactions ++;
            sb.append(String.format("\n%1$s %2$s %3$f", plusP, minusP, transaction));
            if (reorderInEachStep) {
                plusMinus = splitToPlusAndMinus(persons);
                reverse(plusMinus[1]);
            } else {
                if (plusP.getBalance() == 0) {
                    Person[] newArray = new Person[plusMinus[0].length-1];
                    System.arraycopy(plusMinus[0], 1, newArray, 0, plusMinus[0].length-1);
                    plusMinus[0] = newArray;
                }
                if (minusP.getBalance() == 0) {
                    Person[] newArray = new Person[plusMinus[1].length-1];
                    System.arraycopy(plusMinus[1], 1, newArray, 0, plusMinus[1].length-1);
                    plusMinus[1] = newArray;
                }
            }
            personsLeft = plusMinus[0].length + plusMinus[1].length;
        } while(personsLeft > 0);
        // TODO: check for balances matching equally (in + and -)

        final String output = ""+transactions+sb.toString();
        /*synchronized (this) {
            System.out.println("Solution found: " + output);
        }*/
        resultMap.computeIfAbsent(transactions, integer -> output);
    }

    private static Person[][] splitToPlusAndMinus(Person[] allPersons) {
        Arrays.sort(allPersons); // order array descending by balance
        if (allPersons[0].getBalance() == 0) {
            return new Person[][] {{},{}};
        }
        int i = 0, plusPersons = 0, minusPersons = 0, firstMinusPerson;
        while (allPersons[i].getBalance() > 0) {
            i++; plusPersons++;
        }
        try {
            while (!(allPersons[i].getBalance() < 0)) {
                i++;
            }
        } catch (IndexOutOfBoundsException e) {
            System.out.println();
        }
        firstMinusPerson = i;
        minusPersons = allPersons.length - firstMinusPerson;

        final Person[] plus = new Person[plusPersons];
        System.arraycopy(allPersons, 0, plus, 0, plusPersons);
        final Person[] minus = new Person[minusPersons];
        System.arraycopy(allPersons, firstMinusPerson, minus, 0, minusPersons);

        return new Person[][] {plus, minus};
    }

    private static void reverse(Person[] arr) {
        final List<Person> list = Arrays.asList(arr);
        Collections.reverse(list);
        list.toArray(arr);
    }
}
