package com.github.satr.ask.handlers.lambdas;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.*;
import com.github.satr.ask.handlers.Intents;

import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class PrecioPuestoReservaVueloIntent implements IntentRequestHandler {

    @Override
    public boolean canHandle(HandlerInput handlerInput , IntentRequest intentRequest) {
        Slot precioMaximo = intentRequest.getIntent().getSlots().get("precioMaximo");

        return (handlerInput.matches(intentName(Intents.RESERVA_VUELO_INTENT))
                && precioMaximo.getValue() != null
                && precioMaximo.getConfirmationStatus() == SlotConfirmationStatus.DENIED
                && intentRequest.getIntent().getConfirmationStatus() == IntentConfirmationStatus.NONE);
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput, IntentRequest intentRequest) {
        Intent intent = intentRequest.getIntent();

        String speechText = "¿Qué precio, en euros, quieres establecer como máximo?";
        String repromptText = "Dime el precio en euros que quieres establecer como máximo.";

        return handlerInput.getResponseBuilder()
                .withSpeech(speechText)
                .withReprompt(repromptText)
                .addElicitSlotDirective("precioMaximo", intent)
                .build();
    }

}
