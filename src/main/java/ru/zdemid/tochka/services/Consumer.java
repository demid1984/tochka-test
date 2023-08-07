package ru.zdemid.tochka.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.zdemid.tochka.model.Message;
import ru.zdemid.tochka.queue.Bus;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@Slf4j
@RequiredArgsConstructor
public class Consumer {

    private final Bus<Message> bus;
    private final int rateLimiter;
    private final AtomicInteger count;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public void consume() {
        Callable<Integer> task = () -> {
            Message message = bus.consume();
            //log.info("Consume the message: {}", message);
            count.incrementAndGet();
            return 0;
        };
        Collection<Callable<Integer>> tasks = IntStream.range(0, rateLimiter)
                .mapToObj(i -> task).toList();
        try {
            executorService.invokeAll(tasks, 1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
