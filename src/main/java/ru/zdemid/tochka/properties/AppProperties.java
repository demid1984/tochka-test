package ru.zdemid.tochka.properties;

import lombok.Data;

@Data
public class AppProperties {

    @Property(option = "c", longOption = "consumer-rate-limiter", description = "Consumer rate limiter value in second", required = true)
    private String consumerRateLimiter;
    @Property(option = "p", longOption = "producer-rate-limiter", description = "Producer rate limiter value in second", required = true)
    private String producerRateLimiter;

}
