package com.github.satr.ask.handlers.lambdas;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.*;
import com.github.satr.ask.handlers.Intents;

import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class FechaPuestaVerMisVuelosIntent implements IntentRequestHandler {

    @Override
    public boolean canHandle(HandlerInput handlerInput , IntentRequest intentRequest) {
        Slot fecha = intentRequest.getIntent().getSlots().get("fecha");

        return handlerInput.matches(intentName(Intents.VER_MIS_VUELOS_INTENT))
                && fecha.getValue() != null
                && fecha.getConfirmationStatus() == SlotConfirmationStatus.DENIED;
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput, IntentRequest intentRequest) {
        Intent intent = intentRequest.getIntent();

        String speechText = "Dígame el día para el que desea realizar la consulta.";

        return handlerInput.getResponseBuilder()
                .withSpeech(speechText)
                .withReprompt(speechText)
                .addElicitSlotDirective("fecha", intent)
                .build();
    }

}
