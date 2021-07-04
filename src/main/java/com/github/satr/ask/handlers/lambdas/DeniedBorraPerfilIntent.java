package com.github.satr.ask.handlers.lambdas;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.*;
import com.github.satr.ask.handlers.Intents;
import com.github.satr.ask.handlers.Modelo.Database;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class DeniedBorraPerfilIntent implements IntentRequestHandler {

    @Override
    public boolean canHandle(HandlerInput handlerInput , IntentRequest intentRequest) {
        Slot alias = intentRequest.getIntent().getSlots().get("alias");

        return handlerInput.matches(intentName(Intents.BORRA_PERFIL_INTENT))
                && alias.getConfirmationStatus() == SlotConfirmationStatus.DENIED;
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput, IntentRequest intentRequest) {
        String speechText = "Gracias por no borrar el perfil. ¡Nos vemos!";

        return handlerInput.getResponseBuilder()
                .withSpeech(speechText)
                .withReprompt(speechText)
                .withSimpleCard("Borrado", speechText)
                .build();


    }

}
