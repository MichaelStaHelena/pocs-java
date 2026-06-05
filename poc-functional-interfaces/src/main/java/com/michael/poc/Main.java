package com.michael.poc;

import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Demonstra como usar as Functional Interfaces do Java.
 * https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/function/package-summary.html
 * -Function: transforma um valor de tipo A em tipo B.
 * -Predicate: teste booleano sobre um valor; combinável com and, or, negate.
 * -Supplier: provedor lazy sem argumento.
 * -Consumer: consome um valor sem retornar nada; encadeável com andThen.
 * -UnaryOperator: especialização de Function onde entrada e saída têm o mesmo tipo.
 * -BiFunction: combina dois valores de tipos diferentes em um resultado.
 * -BinaryOperator: especialização de BiFunction para dois valores do mesmo tipo.
 * -BiConsumer: consome dois valores sem retornar nada.
 * -Comparator: define ordenação entre dois objetos; encadeável com thenComparing.
 * -Function.andThen / compose: encadeamento de funções (esquerda→direita vs direita→esquerda).
 * -DiscountPolicy: exemplo de functional interface customizada com @FunctionalInterface.
 */
@Slf4j
public class Main {

    public static void main(String[] args) {
        exampleFunctionalInterface();
        examplePredicate();
        exampleFunctionCompose();
        exampleSupplier();
        exampleConsumer();
        examplePredicateChaining();
        exampleUnaryOperator();
        exampleBiFunction();
        exampleBinaryOperator();
        exampleStreams();
        exampleComparator();
        exampleBiConsumer();
        exampleOptionalChain();
    }
    private static void exampleFunctionalInterface() {
        DiscountPolicy semDesconto = preco -> preco;
        log.info("preço cheio: {}", semDesconto.apply(80_000.0));

        DiscountPolicy dezPorcento = preco -> preco * 0.90;
        log.info("10% de desconto: {}", dezPorcento.apply(80_000.0));

        DiscountPolicy metade = preco -> preco * 0.50;
        log.info("metade do preço: {}", metade.apply(80_000.0));
    }

    private static void examplePredicate() {
        Predicate<String> comecaComA = s -> s.startsWith("A");
        log.info("começa com A: {}", comecaComA.test("Astra"));
        log.info("negado: {}", comecaComA.negate().test("BMW"));

        List<String> mixed = List.of("audi", "", "bmw", "  ", "ferrari");
        mixed.stream()
                .filter(Predicate.not(String::isBlank))
                .forEach(log::info);
    }

    private static void exampleFunctionCompose() {
        Function<String, Integer> tamanho = String::length;
        Function<Integer, String> rotulo = n -> "tamanho: " + n;

        log.info(tamanho.andThen(rotulo).apply("Aventador"));  // esquerda para direita
        log.info(rotulo.compose(tamanho).apply("Ferrari"));    // direita para esquerda
    }

    private static void exampleSupplier() {
        // orElseGet só chama o supplier se o valor estiver ausente - lazy por natureza
        Supplier<String> usuarioPadrao = () -> "convidado";
        Optional<String> usuarioAtual = Optional.empty();
        log.info("usuário resolvido: {}", usuarioAtual.orElseGet(usuarioPadrao));
    }

    private static void exampleConsumer() {
        Consumer<String> registrar = s -> log.info("log: {}", s);
        Consumer<String> auditar = s -> log.info("auditoria: {}", s);

        Consumer<String> registrarEAuditar = registrar.andThen(auditar);
        registrarEAuditar.accept("user-login");
    }

    private static void examplePredicateChaining() {
        Predicate<String> comecaComA = s -> s.startsWith("A");
        Predicate<String> ehLongo = s -> s.length() >= 6;

        var carroValido = comecaComA.and(ehLongo);
        log.info("and - 'Audi': {}, 'Aventador': {}", carroValido.test("Audi"), carroValido.test("Aventador"));

        var qualquerUm = comecaComA.or(ehLongo);
        log.info("or  - 'BMW': {}, 'Audi': {}", qualquerUm.test("BMW"), qualquerUm.test("Audi"));
    }

    private static void exampleUnaryOperator() {
        // normalizando uma chave de configuração bruta
        UnaryOperator<String> normalizar = s -> s.trim().toLowerCase().replace(" ", "_");
        log.info("normalizado: {}", normalizar.apply("  Velocidade Máxima  "));
    }

    private static void exampleBiFunction() {
        BiFunction<String, Integer, String> formatarVersao = (nome, ver) -> nome + " v" + ver;
        Function<String, String> colchetes = s -> "[" + s + "]";
        log.info(formatarVersao.andThen(colchetes).apply("modulo-motor", 3));
    }

    private static void exampleBinaryOperator() {
        BinaryOperator<String> unirTags = (a, b) -> a + ", " + b;
        List<String> tags = List.of("esporte", "tração-4x4", "turbo", "luxo");
        tags.stream().reduce(unirTags).ifPresent(resultado -> log.info("tags: {}", resultado));
    }

    private static void exampleStreams() {
        Predicate<String> comecaComA = s -> s.startsWith("A");
        var carroValido = comecaComA.and(s -> s.length() >= 6);

        UnaryOperator<String> normalizar = s -> s.trim().toLowerCase().replace(" ", "_");

        Runnable iniciar = () -> log.info("executando pipeline...");
        iniciar.run();

        List<String> carros = List.of("Audi", "BMW", "Aston Martin", "Chevrolet");
        carros.stream()
                .filter(carroValido)
                .map(normalizar)
                .map(carro -> ">> " + carro)
                .forEach(log::info);
    }

    private static void exampleComparator() {
        List<String> carros = List.of("Audi", "BMW", "Aston Martin", "Chevrolet");

        Comparator<String> porTamanho = Comparator.comparingInt(String::length);
        carros.stream().sorted(porTamanho).forEach(log::info);

        // thenComparing desempata quando os tamanhos são iguais
        Comparator<String> porTamanhoDepoisAlfabetico = porTamanho.thenComparing(Comparator.naturalOrder());
        carros.stream().sorted(porTamanhoDepoisAlfabetico).forEach(log::info);
    }

    private static void exampleBiConsumer() {
        Map<String, Double> precos = Map.of("Audi", 80_000.0, "BMW", 95_000.0, "Chevrolet", 60_000.0);

        BiConsumer<String, Double> exibir = (modelo, preco) -> log.info("{}: R$ {}", modelo, preco);
        BiConsumer<String, Double> alertarCaro = (modelo, preco) -> {
            if (preco > 85_000.0) log.info("{} é caro!", modelo);
        };

        precos.forEach(exibir.andThen(alertarCaro));
    }

    private static void exampleOptionalChain() {
        Optional.of("  Aston Martin  ")
                .map(String::trim)
                .filter(s -> s.length() >= 6)
                .map(s -> "carro encontrado: " + s)
                .ifPresent(log::info);

        // valor ausente percorre toda a cadeia sem explodir
        Optional.<String>empty()
                .map(String::trim)
                .filter(s -> s.length() >= 6)
                .ifPresentOrElse(log::info, () -> log.info("nenhum carro encontrado"));
    }

    @FunctionalInterface
    interface DiscountPolicy {

        /** Retorna o preço final após aplicar o desconto. */
        double apply(double precoOriginal);
    }
}
