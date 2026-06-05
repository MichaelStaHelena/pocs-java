package com.michael.poc;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Demonstra como usar a Stream API do Java.
 * https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/stream/package-summary.html
 * -map: transforma cada elemento do stream.
 * -filter: mantém apenas elementos que satisfazem a condição.
 * -Predicate: filtro reutilizável e combinável (and, or, negate).
 * -Supplier: provedor lazy, não recebe argumento e retorna um valor.
 * -Consumer: consome um elemento sem retornar nada (efeito colateral).
 * -UnaryOperator: transforma um elemento mantendo o mesmo tipo.
 * -BinaryOperator: combina dois valores do mesmo tipo; base do reduce.
 * -distinct: remove duplicatas.
 * -sorted: ordena com ordem natural ou Comparator personalizado.
 * -limit / skip: fatia o stream por quantidade.
 * -count / min / max: agregações terminais simples.
 * -findFirst: curto-circuita no primeiro elemento que passa no filtro.
 * -anyMatch / allMatch / noneMatch: testes booleanos sobre o stream.
 * -flatMap: achata streams aninhados em um único stream.
 * -mapToInt: stream primitivo com suporte a sum, average, etc.
 * -takeWhile / dropWhile: fatiamento condicional sequencial (Java 9+).
 * -Stream.iterate: gera sequências infinitas de forma lazy.
 * -collect: materializa o stream (toSet, joining, groupingBy, partitioningBy, toMap).
 */
public class Main {

    public static void main(String[] args) {

        // map: transforma cada elemento
        List<String> cars = List.of("Civic", "Mustang", "Corolla", "Porsche");
        List<String> upper = cars.stream()
                .map(String::toUpperCase)
                .toList();
        System.out.println("map: " + upper);

        // filter: mantém apenas elementos que atendem à condição
        int[] numbers = {3, 7, 10, 15, 20};
        List<Integer> evens = Arrays.stream(numbers)
                .filter(n -> n % 2 == 0)
                .boxed()
                .toList();
        System.out.println("filter evens: " + evens);

        // map + filter encadeados
        List<Integer> prices = List.of(10, 20, 30);
        List<Integer> withFee = prices.stream()
                .map(price -> price + 2)
                .toList();
        System.out.println("map +fee: " + withFee);
        List<String> cities = List.of("Rome", "Paris", "Oslo", "Berlin");
        List<String> longCities = cities.stream()
                .filter(c -> c.length() > 4)
                .toList();
        System.out.println("filter len>4: " + longCities);


        // Predicate: filtro reutilizável e combinável
        Predicate<Integer> passing   = score -> score >= 50;
        Predicate<Integer> excellent = score -> score >= 85;
        List<Integer> scores = List.of(45, 67, 80, 52, 91, 30);
        System.out.println("Predicate passing: " + scores.stream().filter(passing).toList());
        System.out.println("Predicate excel.: " + scores.stream().filter(passing.and(excellent)).toList());

        // Supplier: provedor lazy
        Supplier<List<String>> carNames = () -> List.of("Civic", "Supra", "Mustang", "Miata");
        System.out.println("Supplier: " + carNames.get());

        // Consumer: efeito colateral por elemento
        Consumer<String> print = s -> System.out.println("  -> " + s);
        carNames.get().forEach(print);

        // UnaryOperator: mesmo tipo de entrada e saída
        UnaryOperator<String> emphasize = s -> s.toUpperCase() + "!";
        List<String> emphasized = carNames.get().stream().map(emphasize).toList();
        System.out.println("UnaryOperator: " + emphasized);

        // BinaryOperator: combina dois valores do mesmo tipo (usado no reduce)
        BinaryOperator<String> longest = (a, b) -> a.length() >= b.length() ? a : b;
        carNames.get().stream().reduce(longest)
                .ifPresent(car -> System.out.println("BinaryOp longest: " + car));

        BinaryOperator<Integer> add = Integer::sum;
        int total = List.of(1, 2, 3, 4, 5).stream().reduce(0, add);
        System.out.println("BinaryOp sum: " + total);

        List<String> carsDup = List.of("Civic", "Mustang", "Corolla", "Civic", "Porsche", "Mustang", "Supra");
        List<Integer> nums   = List.of(5, 3, 8, 1, 9, 2, 7, 4, 6);

        // distinct: remove duplicatas
        List<String> unique = carsDup.stream().distinct().toList();
        System.out.println("distinct: " + unique);

        // sorted: ordem natural e com Comparator personalizado
        List<String> sorted   = carsDup.stream().distinct().sorted().toList();
        List<String> byLength = carsDup.stream().distinct()
                .sorted(Comparator.comparingInt(String::length))
                .toList();
        System.out.println("sorted: " + sorted);
        System.out.println("sorted by len: " + byLength);

        // limit / skip: fatiamento do stream por quantidade
        System.out.println("limit 3: " + nums.stream().limit(3).toList());
        System.out.println("skip 3: " + nums.stream().skip(3).toList());

        // count, min, max: agregações terminais simples
        long count            = carsDup.stream().distinct().count();
        Optional<Integer> min = nums.stream().min(Comparator.naturalOrder());
        Optional<Integer> max = nums.stream().max(Comparator.naturalOrder());
        System.out.println("count unique: " + count);
        System.out.println("min: " + min.get());
        System.out.println("max: " + max.get());

        // findFirst: curto-circuita no primeiro match
        Optional<Integer> firstOver5 = nums.stream().filter(n -> n > 5).findFirst();
        System.out.println("findFirst >5: " + firstOver5.get());

        // anyMatch / allMatch / noneMatch: testes booleanos sobre o stream
        System.out.println("anyMatch >8: " + nums.stream().anyMatch(n -> n > 8));
        System.out.println("allMatch >0: " + nums.stream().allMatch(n -> n > 0));
        System.out.println("noneMatch <0: " + nums.stream().noneMatch(n -> n < 0));

        // flatMap: achata streams aninhados em um único stream
        List<List<Integer>> nested = List.of(List.of(1, 2), List.of(3, 4), List.of(5, 6));
        List<Integer> flat = nested.stream().flatMap(List::stream).toList();
        System.out.println("flatMap: " + flat);

        // mapToInt: stream primitivo com suporte a operações matemáticas
        int sum    = nums.stream().mapToInt(Integer::intValue).sum();
        double avg = nums.stream().mapToInt(Integer::intValue).average().getAsDouble();
        System.out.println("sum: " + sum);
        System.out.println("average: " + avg);

        // takeWhile / dropWhile: fatiamento condicional sequencial (Java 9+)
        List<Integer> taken   = Stream.of(1, 2, 3, 4, 5, 6).takeWhile(n -> n < 4).toList();
        List<Integer> dropped = Stream.of(1, 2, 3, 4, 5, 6).dropWhile(n -> n < 4).toList();
        System.out.println("takeWhile <4: " + taken);
        System.out.println("dropWhile <4: " + dropped);

        // Stream.iterate: gera sequência infinita de forma lazy
        List<Integer> squares = Stream.iterate(1, n -> n + 1).limit(5)
                .map(n -> n * n)
                .toList();
        System.out.println("squares 1-5: " + squares);

        // collect toSet: materializa em conjunto (sem duplicatas)
        Set<String> carSet = carsDup.stream().collect(Collectors.toSet());
        System.out.println("toSet size: " + carSet.size());

        // collect joining: concatena elementos em uma string
        String joined = carsDup.stream().distinct().collect(Collectors.joining(", "));
        System.out.println("joining: " + joined);

        // collect groupingBy: agrupa elementos por chave
        Map<Integer, List<String>> byLen = carsDup.stream()
                .distinct()
                .collect(Collectors.groupingBy(String::length));
        System.out.println("groupingBy len: " + byLen);

        // collect partitioningBy: divide em dois grupos (true/false)
        Map<Boolean, List<Integer>> partitioned = nums.stream()
                .collect(Collectors.partitioningBy(n -> n % 2 == 0));
        System.out.println("partition even: " + partitioned.get(true));
        System.out.println("partition odd: " + partitioned.get(false));

        // collect toMap: constrói um mapa de lookup
        Map<String, Integer> carLengths = carsDup.stream()
                .distinct()
                .collect(Collectors.toMap(s -> s, String::length));
        System.out.println("toMap: " + carLengths);
    }
}
