package nl.tno.idsa.framework.utils;

/**
 * Basic tuple.
 * @param <I> First element.
 * @param <J> Second element.
 */
public class Tuple<I, J> {
    private I first;
    private J second;

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
