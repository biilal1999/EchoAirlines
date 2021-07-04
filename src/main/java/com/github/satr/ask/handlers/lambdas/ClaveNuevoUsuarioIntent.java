package com.github.satr.ask.handlers.lambdas;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.*;
import com.github.satr.ask.handlers.Intents;

import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class ClaveNuevoUsuarioIntent implements IntentRequestHandler {

    @Override
    public boolean canHandle(HandlerInput handlerInput , IntentRequest intentRequest) {
        Slot clave = intentRequest.getIntent().getSlots().get("clave");
        Slot confirmacion = intentRequest.getIntent().getSlots().get("confirmacion");

        return (handlerInput.matches(intentName(Intents.CREA_USUARIO_INTENT))
                && (clave.getValue() == null)
                && (confirmacion.getValue() != null)
                && (confirmacion.getConfirmationStatus() == SlotConfirmationStatus.CONFIRMED));
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput, IntentRequest intentRequest) {
        Intent intent = intentRequest.getIntent();
        String speechText = "Dime una clave de cuatro dígitos para la cuenta de usuario.";

        return handlerInput.getResponseBuilder()
                .withSpeech(speechText)
                .withReprompt(speechText)
                .addElicitSlotDirective("clave", intent)
                .build();
    }

}
