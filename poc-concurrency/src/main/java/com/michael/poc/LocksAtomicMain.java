package com.michael.poc;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Demonstra Locks e variáveis atômicas: controle de acesso a estado compartilhado.
 * https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/locks/package-summary.html
 * -race condition: incremento sem sincronização perde atualizações (++ não é atômico).
 * -synchronized: exclusão mútua mais simples, via monitor de um objeto.
 * -ReentrantLock: lock explícito; liberar sempre no finally.
 * -tryLock: tenta adquirir sem bloquear, útil para evitar deadlock.
 * -Condition: fila de espera num lock (await/signal); base do producer/consumer.
 * -ReentrantReadWriteLock: vários leitores ao mesmo tempo, escritor exclusivo.
 * -AtomicInteger: incremento atômico sem lock (usa CAS por baixo).
 * -compareAndSet: troca o valor só se ele ainda for o esperado; base do lock-free.
 */
public class LocksAtomicMain {

    private static final int THREADS = 4;
    private static final int INCREMENTS = 100_000;
    private static final int EXPECTED = THREADS * INCREMENTS;

    public static void main(String[] args) throws InterruptedException {

        // array de 1 posição: o lambda só captura variável final/effectively final, mas o
        // conteúdo do array pode mudar, é o jeito de ter um contador mutável compartilhado
        int[] unsafe = {0};
        // ++ é ler-somar-escrever; com várias threads esses passos se intercalam e somem updates
        runConcurrent(() -> unsafe[0]++);
        System.out.println("sem proteção: " + unsafe[0] + " (esperado " + EXPECTED + ")");

        // synchronized: exclusão mútua mais simples; o bloco roda atômico sob o monitor do objeto
        int[] sync = {0};
        Object monitor = new Object();
        runConcurrent(() -> {
            synchronized (monitor) {
                sync[0]++;
            }
        });
        System.out.println("synchronized: " + sync[0]);

        // ReentrantLock: mesmo efeito do synchronized, com lock explícito; unlock no finally pra não travar se der exceção
        int[] guarded = {0};
        ReentrantLock lock = new ReentrantLock();
        runConcurrent(() -> {
            lock.lock();
            try {
                guarded[0]++;
            } finally {
                lock.unlock();
            }
        });
        System.out.println("ReentrantLock: " + guarded[0]);

        // AtomicInteger chega no mesmo resultado sem lock, com CAS interno
        AtomicInteger atomic = new AtomicInteger();
        runConcurrent(atomic::incrementAndGet);
        System.out.println("AtomicInteger: " + atomic.get());

        // compareAndSet só atualiza se o valor atual bater com o esperado
        AtomicInteger cas = new AtomicInteger(10);
        System.out.println("CAS 10->20: " + cas.compareAndSet(10, 20) + " valor=" + cas.get());
        System.out.println("CAS 10->30: " + cas.compareAndSet(10, 30) + " valor=" + cas.get());

        // tryLock não bloqueia; com OUTRA thread segurando o lock, a tentativa falha na hora
        // (na mesma thread retornaria true por reentrância, daí a thread separada)
        ReentrantLock busy = new ReentrantLock();
        busy.lock();
        try {
            Thread contender = new Thread(() ->
                    System.out.println("tryLock com lock tomado por outra thread: " + busy.tryLock()));
            contender.start();
            contender.join();
        } finally {
            busy.unlock();
        }

        demoReadWrite();
        demoCondition();
    }

    // readLock é compartilhado (vários leitores juntos); writeLock é exclusivo.
    // O sleep segurando o lock é proposital aqui, só pra tornar a sobreposição visível.
    private static void demoReadWrite() throws InterruptedException {
        ReentrantReadWriteLock rw = new ReentrantReadWriteLock();
        int[] data = {0};
        AtomicInteger activeReaders = new AtomicInteger();
        AtomicInteger maxConcurrentReaders = new AtomicInteger();

        Runnable reader = () -> {
            rw.readLock().lock();
            try {
                int now = activeReaders.incrementAndGet();
                maxConcurrentReaders.accumulateAndGet(now, Math::max);
                System.out.println("  read  ativo (" + now + " leitor(es) juntos) viu " + data[0]);
                sleep(200);
                activeReaders.decrementAndGet();
            } finally {
                rw.readLock().unlock();
            }
        };
        Runnable writer = () -> {
            rw.writeLock().lock();
            try {
                // se chegou aqui o lock é exclusivo: nenhum leitor está ativo
                System.out.println("  WRITE exclusivo | leitores ativos agora: " + activeReaders.get());
                sleep(200);
                data[0]++;
            } finally {
                rw.writeLock().unlock();
            }
        };

        // 5 leitores e 1 escritor disputando; o escritor entra no meio da rajada
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            threads.add(new Thread(reader));
        }
        threads.add(2, new Thread(writer));
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println("máx de leitores simultâneos: " + maxConcurrentReaders.get() + " (>1 prova leitura concorrente)");
        System.out.println("valor final após escrita: " + data[0]);
    }

    // Condition: await libera o lock e dorme até um signal, bounded buffer entre producer e consumer
    private static void demoCondition() throws InterruptedException {
        ReentrantLock lock = new ReentrantLock();
        Condition notFull = lock.newCondition();
        Condition notEmpty = lock.newCondition();
        Queue<Integer> buffer = new ArrayDeque<>();
        int capacity = 3;
        int total = 5;

        Runnable producer = () -> {
            for (int i = 1; i <= total; i++) {
                lock.lock();
                try {
                    while (buffer.size() == capacity) { // while, não if: revalida a condição ao acordar
                        notFull.await();
                    }
                    buffer.add(i);
                    System.out.println("  produziu " + i + " (buffer=" + buffer.size() + ")");
                    notEmpty.signal();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                } finally {
                    lock.unlock();
                }
            }
        };
        Runnable consumer = () -> {
            for (int i = 0; i < total; i++) {
                lock.lock();
                try {
                    while (buffer.isEmpty()) {
                        notEmpty.await();
                    }
                    System.out.println("  consumiu " + buffer.poll() + " (buffer=" + buffer.size() + ")");
                    notFull.signal();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                } finally {
                    lock.unlock();
                }
            }
        };

        Thread p = new Thread(producer);
        Thread c = new Thread(consumer);
        p.start();
        c.start();
        p.join();
        c.join();
    }

    // Thread.sleep encapsulado pra não poluir os lambdas com try/catch de InterruptedException
    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // roda o mesmo trabalho em THREADS threads, cada uma INCREMENTS vezes, e espera todas
    private static void runConcurrent(Runnable work) throws InterruptedException {
        Thread[] threads = new Thread[THREADS];
        for (int i = 0; i < THREADS; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < INCREMENTS; j++) {
                    work.run();
                }
            });
        }
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
    }
}
