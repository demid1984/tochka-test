package ru.zdemid.tochka.queue;

public interface Bus<T> {

    void publish(T message);

    T consume();

    int size();
}
