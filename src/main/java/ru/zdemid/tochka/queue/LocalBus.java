package ru.zdemid.tochka.queue;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class LocalBus<T> implements Bus<T> {

    private final Queue<T> queue = new LinkedBlockingQueue<>();

    @Override
    public void publish(T message) {
        queue.add(message);
    }

    @Override
    public T consume() {
        return queue.poll();
    }

    public int size() {
        return queue.size();
    }
}
