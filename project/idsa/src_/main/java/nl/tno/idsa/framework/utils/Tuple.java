package nl.tno.idsa.framework.utils;

// TODO Document class.

public class Tuple<I, J> {
    I first;
    J second;

    public Tuple(I i, J j) {
        this.first = i;
        this.second = j;
    }

    public I getFirst() {
        return first;
    }

    public J getSecond() {
        return second;
    }

    @Override
    public String toString() {
        return "<" + first + ", " + second + '>';
    }
}
