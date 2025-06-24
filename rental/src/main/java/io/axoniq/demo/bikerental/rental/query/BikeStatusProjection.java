package io.axoniq.demo.bikerental.rental.query;

import io.axoniq.demo.bikerental.coreapi.rental.*;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BikeStatusProjection {

    private final BikeStatusRepository bikeStatusRepository;

    public BikeStatusProjection(BikeStatusRepository bikeStatusRepository) {
        this.bikeStatusRepository = bikeStatusRepository;
    }

    @EventHandler
    public void handle(BikeRegisteredEvent event) {
        BikeStatus bikeStatus = new BikeStatus(event.bikeId(), event.bikeType(), event.location());
        bikeStatusRepository.save(bikeStatus);
    }

    @EventHandler
    public void handle(BikeRequestedEvent event) {
        bikeStatusRepository.findById(event.bikeId()).ifPresent(bikeStatus ->  {
            bikeStatus.requestedBy(event.renter());
            bikeStatusRepository.save(bikeStatus);
        });
    }

    @EventHandler
    public void handle(BikeInUseEvent event) {
        bikeStatusRepository.findById(event.bikeId()).ifPresent(bikeStatus -> {
            bikeStatus.rentedBy(event.renter());
            bikeStatusRepository.save(bikeStatus);
        });
    }

    @EventHandler
    public void handle(BikeReturnedEvent event) {
        bikeStatusRepository.findById(event.bikeId()).ifPresent(bikeStatus -> {
            bikeStatus.returnedAt(event.location());
            bikeStatusRepository.save(bikeStatus);
        });
    }

    @EventHandler
    public void handle(RequestRejectedEvent event) {
        bikeStatusRepository.findById(event.bikeId()).ifPresent(bikeStatus -> {
            bikeStatus.returnedAt(bikeStatus.getLocation());
            bikeStatusRepository.save(bikeStatus);
        });
    }

    @QueryHandler(queryName = "findAll")
    public List<BikeStatus> findAll() {
        return bikeStatusRepository.findAll();
    }

    @QueryHandler(queryName = "findOne")
    public BikeStatus findOne(String bikeId) {
        return bikeStatusRepository.findById(bikeId).orElse(null);
    }
}
