package com.michael.poc;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Demonstra Callable e Future: tarefas que devolvem resultado de forma assíncrona.
 * https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/Future.html
 * -Callable: como Runnable, mas retorna valor e pode lançar exceção checada.
 * -submit: agenda o Callable e devolve um Future na hora, sem bloquear.
 * -Future.get: bloqueia até o resultado ficar pronto.
 * -Future.get(timeout): desiste com TimeoutException se demorar demais.
 * -Future.isDone / isCancelled: consultam o estado da tarefa sem bloquear.
 * -Future.cancel: tenta interromper a tarefa antes de ela terminar.
 * -ExecutionException: empacota a exceção lançada dentro do Callable.
 * -invokeAll: roda vários Callable e devolve a lista de Future quando todos terminam.
 * -invokeAny: devolve o resultado do primeiro que terminar e cancela os demais.
 */
public class CallableFutureMain {

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        ExecutorService pool = Executors.newFixedThreadPool(3);

        // submit devolve o Future de imediato; get() é que bloqueia esperando o resultado
        Future<Integer> sum = pool.submit(() -> {
            Thread.sleep(200);
            return 2 + 3;
        });
        System.out.println("isDone antes do get? " + sum.isDone());
        System.out.println("get (bloqueia): " + sum.get());
        System.out.println("isDone depois? " + sum.isDone());

        // get(timeout): não espera além do limite
        Future<String> slow = pool.submit(() -> {
            Thread.sleep(1000);
            return "terminei";
        });
        try {
            slow.get(200, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            System.out.println("get(timeout): estourou, cancelando");
            slow.cancel(true); // true = interrompe a thread se a tarefa já estiver rodando
            System.out.println("isCancelled? " + slow.isCancelled());
        }

        // exceção dentro do Callable chega empacotada em ExecutionException no get()
        Future<Integer> boom = pool.submit(() -> Integer.parseInt("abc"));
        try {
            boom.get();
        } catch (ExecutionException e) {
            System.out.println("ExecutionException causada por: " + e.getCause());
        }

        // invokeAll: espera todos terminarem e devolve os Future na ordem de submissão
        List<Callable<String>> tasks = List.of(
                () -> task("A", 100),
                () -> task("B", 300),
                () -> task("C", 200)
        );
        List<Future<String>> results = pool.invokeAll(tasks);
        for (Future<String> result : results) {
            System.out.println("invokeAll -> " + result.get());
        }

        // invokeAny: devolve o resultado do primeiro a terminar e descarta o resto
        System.out.println("invokeAny -> " + pool.invokeAny(tasks));

        pool.shutdown();
        pool.awaitTermination(2, TimeUnit.SECONDS);
    }

    private static String task(String name, int millis) throws InterruptedException {
        Thread.sleep(millis);
        return name + " (" + millis + "ms) @ " + Thread.currentThread().getName();
    }
}
