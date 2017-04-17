package hw2;

class AugmentingPath {
    final int[] visitedFrom;
    final boolean[] visitedBackwards;

    AugmentingPath(int[] visitedFrom, boolean[] visitedBackwards) {
        this.visitedFrom = visitedFrom;
        this.visitedBackwards = visitedBackwards;
    }
}
