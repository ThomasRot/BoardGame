package util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class U {
    public static void main(String[] args) {
        Log(getMaximum(List.of(6, 5, 9, 7, 3), i -> 1.0 / i));
        Log(all(List.of(1, 2, 3), i -> i > 0));
        Log(all(List.of(1, 2, 3), i -> i > 2));
        Log(addAll(new ArrayList<>(List.of(1, 2, 3)), new ArrayList<>(List.of(2, 3, 4))));
    }

    public static <T, R> List<R> map(List<T> input, Function<T, R> map) {
        List result = null;
        try {
            var clazz = input.getClass();
            result = clazz.getConstructor().newInstance();
            for (var t : input) {
                result.add(map.apply(t));
            }
        } catch (Exception e) {
            System.out.println("Bad usage");
        }
        return result;
    }

    public static <T> T reduce(List<T> input, BiFunction<T, T, T> combine) {
        if (input.isEmpty()) return null;
        T acc = input.get(0);
        for (int i = 1; i < input.size(); i++) {
            acc = combine.apply(acc, input.get(i));
        }
        return acc;
    }

    public static <T, R> R reduce(List<T> input, BiFunction<R, T, R> combine, R base) {
        if (input.isEmpty()) return null;
        R acc = base;
        for (int i = 0; i < input.size(); i++) {
            acc = combine.apply(acc, input.get(i));
        }
        return acc;
    }

    public static <T,R,S> List<S> zipMap(List<T> inputT, List<R> inputR, BiFunction<T,R,S> combine) {
        List result = null;
        try {
            var clazz = inputT.getClass();
            result = clazz.getConstructor().newInstance();
            for (int i = 0; i < Math.max(inputR.size(), inputT.size()); i++) {
                result.add(combine.apply(getOrNull(inputT, i), getOrNull(inputR, i)));
            }
        } catch (Exception e) {
            System.out.println("Bad usage");
        }
        return result;
    }

    public static <T,R> List<UPair<T,R>> zip(List<T> inputT, List<R> inputR) {
        return zipMap(inputT, inputR, UPair::of);
    }

    public static <T> List<T> filter(List<T> input, Predicate<T> pred) {
        List result = null;
        try {
            var clazz = input.getClass();
            result = clazz.getConstructor().newInstance();
            for (var t : input) {
                if (pred.test(t))
                    result.add(t);
            }
        } catch (Exception e) {
            System.out.println("Bad usage");
        }
        return result;
    }

    public static Comparable getMinimum(List<Comparable> input) {
        return getExtremum(true, input, c -> c);
    }

    public static Comparable getMaximum(List<Comparable> input) {
        return getExtremum(false, input, c -> c);
    }

    public static <T> T getMinimum(List<T> input, Function<T, Comparable> map) {
        return getExtremum(true, input, map);
    }

    public static <T> T getMaximum(List<T> input, Function<T, Comparable> map) {
        return getExtremum(false, input, map);
    }

    private static <T> T getExtremum(boolean isMinimum, List<T> input, Function<T, Comparable> map) {
        if (input.isEmpty()) return null;
        var bestT = input.get(0);
        for (int i = 1; i < input.size(); i++) {
            var curT = input.get(i);
            if (isMinimum ? map.apply(bestT).compareTo(map.apply(curT)) > 0 : map.apply(bestT).compareTo(map.apply(curT)) < 0) {
                bestT = curT;
            }
        }
        return bestT;
    }

    public static <T> T getAny(List<T> input, Predicate<T> pred) {
        for (var t : input) {
            if (pred.test(t)) return t;
        }
        return null;
    }

    public static <T> boolean all(List<T> input, Predicate<T> pred) {
        return !any(input, pred.negate());
    }

    public static <T> boolean any(List<T> input, Predicate<T> pred) {
        for (var t : input) {
            if (pred.test(t)) return true;
        }
        return false;
    }

    public static void Log(Object... objects) {
        int callersLineNumber = Thread.currentThread().getStackTrace()[2].getLineNumber();
        String className = Thread.currentThread().getStackTrace()[2].getClassName();
        System.out.println('[' + className + ":" + callersLineNumber + ']' + " " +
                join(new ArrayList<>(Arrays.asList(objects))));
    }

    public static <T> String join(List<T> input) {
        return join(input, " ; ");
    }

    public static <T> String join(List<T> input, String delimiter) {
        if (input == null || input.isEmpty()) return "";
        return mapReduceSkipNull(input, T::toString, (acc, s) -> acc + delimiter + s);
    }

    public static <T, R> R mapAdd(List<T> input, Function<T, R> map) {
        if (input.isEmpty()) return null;
        return mapAdd(input.subList(1, input.size()), map, map.apply(input.get(0)));
    }

    public static <T, R> R mapAdd(List<T> input, Function<T, R> map, R rBase) {
        if (input.isEmpty()) return null;
        try {
            Method mAdd = rBase.getClass().getMethod("add", rBase.getClass());
            var acc = rBase;

            BiFunction<R, R, R> add =
                    (a1, a2) -> {
                        if (a1 instanceof String) {
                            return (R) ((String) a1 + (String) a2);
                        }
                        if (a1 instanceof Double) {
                            return (R) Double.valueOf((Double) a1 + (Double) a2);
                        }
                        if (a1 instanceof Integer) {
                            return (R) Integer.valueOf((Integer) a1 + (Integer) a2);
                        }
                        try {
                            return (R) mAdd.invoke(a1, a2);
                        } catch (Exception e) {
                            return a1;
                        }
                    };
            return mapReduce(input, map, add);
        } catch (NoSuchMethodException e) {
            System.out.println("Bad usage");
            return null;
        }
    }

    public static <T, R> R addAll(List<T> input, R rBase) {
        if (input.isEmpty()) return null;
        Method[] methods = rBase.getClass().getMethods();
        Method mAdd = null;
        for (var m : methods) {
            if (m != null && m.getParameterCount() == 1 && m.getName().equals("add")) mAdd = m;
        }
        Method finalMAdd = mAdd;
        BiFunction<R, T, R> add =
                (a1, a2) -> {
                    try {
                        finalMAdd.invoke(a1, a2);
                        return a1;
                    } catch (Exception e) {
                        return a1;
                    }
                };
        return mapReduce(input, t -> t, add, rBase);
    }


    public static <T, R> R mapReduce(List<T> input, Function<T, R> map, BiFunction<R, R, R> reduce) {
        if (input.isEmpty()) return null;
        return mapReduceSkipNull(input.subList(1, input.size()), map, reduce, map.apply(input.get(0)));
    }

    public static <T, R, U> U mapReduce(List<T> input, Function<T, R> map, BiFunction<U, R, U>reduce, U base) {
        return mapConditionalReduce(input, t -> true, map, t -> 0, reduce, (u, r) -> u, base);
    }

    public static <T, R> R mapReduceSkipNull(List<T> input, Function<T, R> map, BiFunction<R, R, R>reduce) {
        return mapConditionalReduce(input.subList(1, input.size()), Objects::nonNull, map, t -> 0, reduce, (u, r) -> u, map.apply(input.get(0)));
    }

    public static <T, R, U> U mapReduceSkipNull(List<T> input, Function<T, R> map, BiFunction<U, R, U>reduce, U base) {
        return mapConditionalReduce(input, Objects::nonNull, map, t -> 0, reduce, (u, r) -> u, base);
    }

    public static <T, R, U> U mapConditionalReduce(List<T> input, Predicate<T> pred,
                                                      BiFunction<T, Boolean, R> map,
                                                      BiFunction<U, R, U> reduce,
                                                      U base) {
        return mapConditionalReduce(input, pred, (T t) -> map.apply(t, true), (T t) -> map.apply(t, false), reduce, reduce, base);
    }

    public static <T, R, S, U> U mapConditionalReduce(List<T> input, Predicate<T> pred,
                                                      Function<T, R> mapTrue,
                                                      Function<T, S> mapFalse,
                                                      BiFunction<U, R, U> reduceTrue,
                                                      BiFunction<U, S, U> reduceFalse,
                                                      U base) {
        var acc = base;
        for (var t : input) {
            acc = pred.test(t) ? reduceTrue.apply(acc, mapTrue.apply(t))
                    : reduceFalse.apply(acc, mapFalse.apply(t));
        }
        return acc;
    }

    public static <T, R, S, V> U mapConditionalReduce(List<T> input, Predicate<T> pred,
                                                      Function<T, R> mapTrue,
                                                      Function<T, S> mapFalse,
                                                      BiFunction<U, R, U> reduceTrue,
                                                      BiFunction<U, S, U> reduceFalse,
                                                      Supplier<U> baseSup) {
        return mapConditionalReduce(input, pred, mapTrue, mapFalse, reduceTrue, reduceFalse, baseSup.get());
    }



    public static <T> T getOrNull(List<T> inputT, int i) {
        return i < inputT.size() ? inputT.get(i) : null;
    }

    public static <T> Stream<UEnumerator<T>> enumerate(List<T> c) {
        return IntStream.range(0, c.size()).mapToObj(i -> UEnumerator.of(i, c.get(i)));
    }

}
