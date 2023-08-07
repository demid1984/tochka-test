package ru.zdemid.tochka;

import ru.zdemid.tochka.model.Message;
import ru.zdemid.tochka.properties.AppProperties;
import ru.zdemid.tochka.properties.PropertyBinder;
import ru.zdemid.tochka.queue.Bus;
import ru.zdemid.tochka.queue.LocalBus;
import ru.zdemid.tochka.services.Consumer;
import ru.zdemid.tochka.services.Producer;

import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Application {

    public static void main(String[] args) {
        try {
            PropertyBinder<AppProperties> propertiesPropertyBinder = new PropertyBinder<>(AppProperties.class);
            AppProperties appProperties = propertiesPropertyBinder.bind(args);
            if (appProperties != null) {
                initApp(appProperties);
            }
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void initApp(AppProperties appProperties) {
        int consumerRate = Integer.parseInt(appProperties.getConsumerRateLimiter());
        int producerRate = Integer.parseInt(appProperties.getProducerRateLimiter());
        if (consumerRate > producerRate) {
            throw new IllegalArgumentException("Consumer should be slower then producer");
        }
        AtomicInteger consumeCount = new AtomicInteger(0);
        AtomicInteger produceCount = new AtomicInteger(0);
        Bus<Message> bus = new LocalBus<>();
        Producer producer = new Producer(bus, producerRate, produceCount);
        Consumer consumer = new Consumer(bus, consumerRate, consumeCount);
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);
        scheduledExecutorService.scheduleAtFixedRate(producer::publishMessage, 0, 1, TimeUnit.SECONDS);
        scheduledExecutorService.scheduleAtFixedRate(consumer::consume, 0, 1, TimeUnit.SECONDS);
        progressBar(bus, produceCount, consumeCount);
    }

    private static void progressBar(Bus<Message> bus, AtomicInteger produceCount, AtomicInteger consumeCount) {
        try {
            long start = Instant.now().toEpochMilli();
            while (!Thread.currentThread().isInterrupted()) {
                Thread.sleep(100);
                int consumerCountValue = consumeCount.get();
                int producerCountValue = produceCount.get();
                long milliDistance = Instant.now().toEpochMilli() - start;
                String progress = String.format("Producer %.2f/sec, count = %d; Consumer %.2f/sec, count = %d; Queue size = %d       ",
                        producerCountValue * 1000. / milliDistance,
                        producerCountValue,
                        consumerCountValue * 1000. / milliDistance,
                        consumerCountValue,
                        bus.size());
                System.out.print(progress);
                System.out.print("\r");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}