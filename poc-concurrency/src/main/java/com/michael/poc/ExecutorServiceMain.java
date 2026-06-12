package com.michael.poc;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Demonstra o ExecutorService: gerência de pools de threads.
 * https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/ExecutorService.html
 * -newFixedThreadPool: pool com N threads fixas reutilizadas entre as tarefas.
 * -newSingleThreadExecutor: uma thread só, tarefas executadas em sequência.
 * -newCachedThreadPool: cria threads sob demanda e reaproveita as ociosas.
 * -newVirtualThreadPerTaskExecutor: uma virtual thread por tarefa (Java 21).
 * -newScheduledThreadPool: agenda tarefas para rodar após um delay.
 * -execute: dispara um Runnable, sem retorno.
 * -shutdown / shutdownNow: encerra o pool de forma ordenada ou abrupta.
 * -awaitTermination: bloqueia até o pool terminar ou estourar o timeout.
 * -try-with-resources: ExecutorService é AutoCloseable (Java 19+); o close() aguarda as tarefas.
 */
public class ExecutorServiceMain {

    public static void main(String[] args) throws InterruptedException {

        // newFixedThreadPool: 2 threads para 4 tarefas, então elas se revezam
        ExecutorService fixed = Executors.newFixedThreadPool(2);
        for (int i = 1; i <= 4; i++) {
            int task = i;
            fixed.execute(() -> System.out.println("fixed task " + task + " @ " + Thread.currentThread().getName()));
        }
        shutdownAndWait(fixed);

        // newSingleThreadExecutor: tudo na mesma thread, uma tarefa depois da outra
        ExecutorService single = Executors.newSingleThreadExecutor();
        single.execute(() -> System.out.println("single A @ " + Thread.currentThread().getName()));
        single.execute(() -> System.out.println("single B @ " + Thread.currentThread().getName()));
        shutdownAndWait(single);

        // newCachedThreadPool: abre uma thread por tarefa quando não há ociosa disponível
        ExecutorService cached = Executors.newCachedThreadPool();
        for (int i = 1; i <= 3; i++) {
            int task = i;
            cached.execute(() -> System.out.println("cached task " + task + " @ " + Thread.currentThread().getName()));
        }
        shutdownAndWait(cached);

        // newVirtualThreadPerTaskExecutor: try-with-resources fecha o pool aguardando as tarefas
        try (ExecutorService virtual = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 1; i <= 3; i++) {
                int task = i;
                virtual.execute(() -> System.out.println("virtual task " + task + " isVirtual=" + Thread.currentThread().isVirtual()));
            }
        }

        // newScheduledThreadPool: roda a tarefa só depois do delay informado
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(() -> System.out.println("scheduled: rodou após 300ms"), 300, TimeUnit.MILLISECONDS);
        shutdownAndWait(scheduler);
    }

    // shutdown ordenado: para de aceitar tarefas, espera as em andamento e força se estourar o tempo
    private static void shutdownAndWait(ExecutorService pool) throws InterruptedException {
        pool.shutdown();
        if (!pool.awaitTermination(2, TimeUnit.SECONDS)) {
            pool.shutdownNow();
        }
    }
}
