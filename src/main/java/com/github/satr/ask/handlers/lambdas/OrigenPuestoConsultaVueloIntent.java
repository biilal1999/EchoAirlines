package com.github.satr.ask.handlers.lambdas;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.*;
import com.github.satr.ask.handlers.Intents;
import com.github.satr.ask.handlers.Modelo.Database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class OrigenPuestoConsultaVueloIntent implements IntentRequestHandler {


    @Override
    public boolean canHandle(HandlerInput handlerInput , IntentRequest intentRequest) {
        Slot origen = intentRequest.getIntent().getSlots().get("origen");

        return handlerInput.matches(intentName(Intents.CONSULTA_VUELO_INTENT))
                && origen.getValue() != null && origen.getConfirmationStatus() == SlotConfirmationStatus.DENIED;
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput, IntentRequest intentRequest) {
        Intent intent = intentRequest.getIntent();

        String speechText = "¿Cuál quieres que sea el origen?";

        return handlerInput.getResponseBuilder()
                .withSpeech(speechText)
                .withReprompt(speechText)
                .addElicitSlotDirective("origen", intent)
                .build();
    }

}
