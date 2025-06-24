package io.axoniq.demo.bikerental.rental.paymentsaga;

import io.axoniq.demo.bikerental.coreapi.payment.PaymentConfirmedEvent;
import io.axoniq.demo.bikerental.coreapi.payment.PaymentRejectedEvent;
import io.axoniq.demo.bikerental.coreapi.payment.PreparePaymentCommand;
import io.axoniq.demo.bikerental.coreapi.rental.ApproveRequestCommand;
import io.axoniq.demo.bikerental.coreapi.rental.BikeRequestedEvent;
import io.axoniq.demo.bikerental.coreapi.rental.RejectRequestCommand;
import io.axoniq.demo.bikerental.rental.command.Bike;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

import static org.axonframework.modelling.saga.SagaLifecycle.associateWith;

@Saga
public class PaymentSaga {

    @Autowired
    private transient CommandGateway commandGateway;

    private String bikeId;
    private String renter;

    @StartSaga
    @SagaEventHandler(associationProperty = "bikeId")
    public void handle(BikeRequestedEvent event) {
        this.bikeId = event.bikeId();
        this.renter = event.renter();

        associateWith("paymentReference", event.rentalReference());
        commandGateway.send(new PreparePaymentCommand(10, event.rentalReference()));
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "paymentReference")
    public void handle(PaymentConfirmedEvent event) {
        commandGateway.send(new ApproveRequestCommand(this.bikeId, this.renter));
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "paymentReference")
    public void handle(PaymentRejectedEvent event) {
        commandGateway.send(new RejectRequestCommand(this.bikeId, this.renter));
    }
}
