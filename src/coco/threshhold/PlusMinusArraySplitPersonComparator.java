package coco.threshhold;

import java.util.Comparator;

public class PlusMinusArraySplitPersonComparator implements Comparator<Person> {
    @Override
    public int compare(Person o1, Person o2) {
        if (o1.getBalance() == 0 && o2.getBalance() == 0) {
            return 0;
        }
        if (o1.getBalance()*o2.getBalance() > 0) {
            return 0;
        }
        return new DescendingBalanceFirstPersonComparator(false).compare(o1, o2);
    }
}
