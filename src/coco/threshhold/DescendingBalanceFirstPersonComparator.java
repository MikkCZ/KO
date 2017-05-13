package coco.threshhold;

import java.util.Comparator;

public class DescendingBalanceFirstPersonComparator implements Comparator<Person> {

    private final boolean swapNegative;

    public DescendingBalanceFirstPersonComparator(boolean swapNegative) {
        this.swapNegative = swapNegative;
    }

    @Override
    public int compare(Person o1, Person o2) {
        double doubleCompare = -(o1.getBalance() - o2.getBalance());
        if (swapNegative && o1.getBalance() < 0 && o2.getBalance() < 0) {
            doubleCompare = -doubleCompare;
        }
        if (doubleCompare < 0) {
            return -1;
        } else if(doubleCompare > 0) {
            return 1;
        } else {
            return (int)doubleCompare;
        }
    }
}
