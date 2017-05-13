package coco.threshhold;

public class Person implements Cloneable {

    private static final double EPSILON = 10e-14;

    private final String number;
    private int bills = 0;
    private double average;
    private double moneyAccepted = 0;
    private double moneySent = 0;

    public Person(int number) {
        this.number = ""+number;
    }

    public void addBill(int bill) {
        this.bills += bill;
    }

    public void averageIs(double average) {
        this.average = average;
    }

    public void acceptMoney(double money) {
        this.moneyAccepted += money;
    }

    public void sendMoney(double money) {
        this.moneySent += money;
    }

    public double getBalance() {
        double balance = this.average - this.bills + this.moneyAccepted - this.moneySent;
        if (Math.abs(balance) < EPSILON) {
            return 0;
        }
        return this.average - this.bills + this.moneyAccepted - this.moneySent;
    }

    @Override
    public String toString() {
        return number;
    }

    @Override
    public Person clone() {
        if (moneyAccepted > 0 || moneySent > 0) {
            throw new IllegalStateException("Cannot clone person with transactions. " + this);
        }
        final Person clone = new Person(Integer.parseInt(this.number));
        clone.addBill(this.bills);
        clone.averageIs(this.average);
        return clone;
    }
}
