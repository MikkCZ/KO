package coco.threshhold;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CocoThr {

    // NOTE: the persons index in the code is always one less than the index in the input/output file

    private static int n, m; // n = number of persons, m = number of bills
    private static int allBillSum; // sum of all bills
    private static Person[] persons; // sum of bills for each person
    private static double s; // average each person should pay in total

    public static void main(String[] args) throws IOException {

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
            plusMinus = splitToPlusAndMinus(persons);
            reverse(plusMinus[1]);
            personsLeft = plusMinus[0].length + plusMinus[1].length;
        } while(personsLeft > 0);
        // TODO: check for balances matching equally (in + and -)
        // TODO: reorder after each step (optional)
        // TODO: run N variants of sorting and reordering in parallel

        // output
        try(PrintStream output = new PrintStream(new File(args[1]))) {
            output.print(transactions);
            output.print(sb.toString());
        }
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

    public static void reverse(Person[] arr) {
        final List<Person> list = Arrays.asList(arr);
        Collections.reverse(list);
        list.toArray(arr);
    }



}
