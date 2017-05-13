package coco.threshhold;

import java.util.Comparator;

public class DescendingBalanceFirstPersonComparator implements Comparator<Person> {
    @Override
    public int compare(Person o1, Person o2) {
        final double doubleCompare = -(o1.getBalance() - o2.getBalance());
        if (doubleCompare < 0) {
            return -1;
        } else if(doubleCompare > 0) {
            return 1;
        } else {
            return (int)doubleCompare;
        }
    }
}
