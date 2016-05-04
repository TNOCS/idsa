package nl.tno.idsa.framework.utils;

import java.util.*;

// TODO Document class.

public class RandomNumber {
    private static final Random rnd = new Random(2); // TODO Why this seed? And should we have a Mersenne or something?

    public static Random getRandom() {
        return rnd;
    }

    public static int nextInt(int max) {
        return rnd.nextInt(max);
    }

    public static int nextBoundedInt(int max) {
        return nextBoundedInt(0, max);
    }

    public static int nextBoundedInt(int min, int max) {
        if (max < min) {
            return -1;
        }
        if (max == min) {
            return min;
        }
        return rnd.nextInt(max - min) + min;
    }

    public static double nextDouble(double max) {
        return rnd.nextDouble() * max;
    }

    public static double nextGaussian(double mean, double standardDeviation) {
        return rnd.nextGaussian() * standardDeviation + mean;
    }

    public static double nextDouble() {
        return rnd.nextDouble();
    }

    public static int drawFrom(ArrayList<Integer> list) {
        ArrayList<Long> longList = new ArrayList<>();
        for (Integer i : list) {
            longList.add((long) i);
        }
        return drawFromLong(longList);
    }

    public static int drawFromLong(ArrayList<Long> list) {
        final long sum = sumLong(list);
        double randomDouble = nextDouble();
        long random = Math.round(sum * randomDouble + 0.5);

        for (int i = 0; i < list.size(); i++) {
            long tempSum = sumLong(list.subList(0, i + 1));//+1 due to nature of subList.
            if (random <= tempSum) {
                return i;
            }
        }

//        //One should never get out of the for-loop.
//        String toPrint = "The input list is ";
//        for (long aList : list) {
//            toPrint += aList;
//            toPrint += ",";
//        }
//        System.out.println(toPrint);
//        System.out.println("ERROR: drawFrom not completed.");
        return -1;
    }

    private static long sumLong(List<Long> list) {
        long sum = 0;
        for (long i : list)
            sum = sum + i;
        return sum;
    }

    @SuppressWarnings("unchecked")
    public static <E> E randomElement(Collection<? extends E> coll) {
        if (coll.isEmpty()) {
            return null; // or throw IAE, if you prefer
        }
        int index = nextInt(coll.size());
        if (coll instanceof List) { // optimization
            return ((List<? extends E>) coll).get(index);
        } else {
            Iterator<? extends E> iter = coll.iterator();
            for (int i = 0; i < index; i++) {
                iter.next();
            }
            return iter.next();
        }
    }

    public static <V extends Enum<V>> V drawFromEnumeratedMap(Class<V> enumType, Map<V, Integer> map) {
        int sum = 0;
        for (V v : map.keySet()) {
            sum += map.get(v);
        }
        int i = nextBoundedInt(sum);
        sum = 0;
        V result = enumType.getEnumConstants()[0];
        for (V v : map.keySet()) {
            sum += map.get(v);
            if (i < sum) {
                result = v;
                break;
            }
        }
        return result;
    }

    public static int drawIndex(List<Integer> list) {
        return drawIndex(list, 0, list.size() - 1);
    }

    public static int drawIndex(List<Integer> list, int startIndex, int endIndex) {
        int result = -1;
        int sum = 0;
        for (int i = startIndex; i <= endIndex && i < list.size(); ++i) {
            sum += list.get(i);
        }
        int randomIndex = nextBoundedInt(sum);
        sum = 0;
        for (int i = startIndex; i <= endIndex && i < list.size(); ++i) {
            sum += list.get(i);
            if (randomIndex < sum) {
                result = i;
                break;
            }
        }
        return result;
    }
}
