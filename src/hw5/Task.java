package hw5;

public class Task {

    private final String name;

    private final int p, r, d;

    private int finalStartTime = Integer.MAX_VALUE;

    public Task(int name, int p, int r, int d) {
        this.name = Integer.toString(name);
        this.p = p;
        this.r = r;
        this.d = d;
    }

    public int getP() {
        return p;
    }

    public int getR() {
        return r;
    }

    public int getD() {
        return d;
    }

    public void setFinalStartTime(int startTime) {
        finalStartTime = startTime;
    }

    public int getFinalStartTime() {
        return finalStartTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task)) return false;

        Task task = (Task) o;

        if (getP() != task.getP()) return false;
        if (getR() != task.getR()) return false;
        if (getD() != task.getD()) return false;
        return name.equals(task.name);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + getP();
        result = 31 * result + getR();
        result = 31 * result + getD();
        return result;
    }

    @Override
    public String toString() {
        return name;
    }
}
