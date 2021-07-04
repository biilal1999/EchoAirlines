package com.github.satr.ask.handlers.lambdas;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.*;
import com.github.satr.ask.handlers.Intents;

import java.text.SimpleDateFormat;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class DeniedReservaVueloIntent implements IntentRequestHandler {

    public final SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
    public final SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
    public final SimpleDateFormat sdfMonth = new SimpleDateFormat("yyyy-MM");

    @Override
    public boolean canHandle(HandlerInput handlerInput , IntentRequest intentRequest) {
        Slot accion = intentRequest.getIntent().getSlots().get("accion");

        return (handlerInput.matches(intentName(Intents.RESERVA_VUELO_INTENT))
                && accion.getValue() != null
                && (accion.getConfirmationStatus() == SlotConfirmationStatus.DENIED
                    || intentRequest.getIntent().getConfirmationStatus() == IntentConfirmationStatus.DENIED));
    }


    @Override
    public Optional<Response> handle(HandlerInput handlerInput, IntentRequest intentRequest) {
        String speechText = "No ha confirmado la reserva. Hasta otra.";

        return handlerInput.getResponseBuilder()
                .withSpeech(speechText)
                .withReprompt(speechText)
                .build();

    }

}
