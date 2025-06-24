package io.axoniq.demo.bikerental.rental.command;

import io.axoniq.demo.bikerental.coreapi.rental.*;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

import java.util.UUID;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Aggregate
public class Bike {
    @AggregateIdentifier
    private String bikeId;

    private RentalStatus status;
    private String currentRenter;

    public Bike() {
        // Required by Axon Framework
    }

    @CommandHandler
    public Bike(RegisterBikeCommand command) {
        apply(new BikeRegisteredEvent(command.bikeId(), command.bikeType(), command.location()));
    }

    @CommandHandler
    public String handle(RequestBikeCommand command) { // works on an existing aggregate
        if (this.status != RentalStatus.AVAILABLE) {
            throw new IllegalStateException("Bike is not available for rent");
        }

        String rentalReference = UUID.randomUUID().toString();
        apply(new BikeRequestedEvent(
                command.bikeId(),
                command.renter(),
                rentalReference
        ));
        return rentalReference;
    }

    @CommandHandler
    public void handle(ApproveRequestCommand command) {
        if (this.status != RentalStatus.REQUESTED || !this.currentRenter.equals(command.renter())) {
            return;
        }
        apply(new BikeInUseEvent(command.bikeId(), command.renter()));
    }

    @CommandHandler
    public void handle(RejectRequestCommand command) {
        if (this.status != RentalStatus.REQUESTED || !this.currentRenter.equals(command.renter())) {
            return;
        }
        apply(new RequestRejectedEvent(command.bikeId()));
    }

    @CommandHandler
    public void handle(ReturnBikeCommand command) {
        if (this.status != RentalStatus.RENTED) {
            return;
        }
        apply(new BikeReturnedEvent(command.bikeId(), command.location()));
    }

    @EventSourcingHandler
    public void on(BikeRegisteredEvent event) {
        this.bikeId = event.bikeId();
        this.status = RentalStatus.AVAILABLE;
    }

    @EventSourcingHandler
    public void on(BikeRequestedEvent event) {
        this.currentRenter = event.renter();
        this.status = RentalStatus.REQUESTED;
    }

    @EventSourcingHandler
    public void on(BikeInUseEvent event) {
        this.status = RentalStatus.RENTED;
        this.currentRenter = event.renter();
    }

    @EventSourcingHandler
    public void on(RequestRejectedEvent event) {
        this.status = RentalStatus.AVAILABLE;
        this.currentRenter = null;
    }

    @EventSourcingHandler
    public void on(BikeReturnedEvent event) {
        this.status = RentalStatus.AVAILABLE;
        this.currentRenter = null;
    }
}
