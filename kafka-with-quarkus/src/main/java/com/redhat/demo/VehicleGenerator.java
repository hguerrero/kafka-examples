package com.redhat.demo;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.Flowable;
import io.smallrye.reactive.messaging.kafka.KafkaRecord;
import io.vertx.core.json.Json;

@ApplicationScoped
public class VehicleGenerator {
    static Logger LOG = LoggerFactory.getLogger(VehicleGenerator.class);
    static AtomicLong ids = new AtomicLong();
    static Random randomizer = new Random();

    @Outgoing("uber")
    public Flowable<KafkaRecord<String,String>> generateVehicleData() {
        return Flowable.interval(500, TimeUnit.MILLISECONDS)
                .onBackpressureBuffer()
                .map(tick -> {
                    VehicleInfo v = new VehicleInfo();
                    LOG.info("Dispatching vehicle: {}", v);
                    return KafkaRecord.of(v.vehicleId.toString(), v.toString());
                });
    }

    private class VehicleInfo {
        public Long vehicleId;
        public Double pricePerMinute;
        public Integer availableSeats;
        public Boolean available = true;
        public VehicleInfo() {
            this.vehicleId = ids.getAndIncrement();
            this.pricePerMinute = 1 + randomizer.nextDouble() * 9;
            this.availableSeats = randomizer.nextInt(5);
            if (availableSeats == 0) available = false;
        }
        @Override
        public String toString() {
            return Json.encodePrettily(this);
        }
    }
}
