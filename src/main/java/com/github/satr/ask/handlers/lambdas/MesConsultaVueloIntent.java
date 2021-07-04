package com.github.satr.ask.handlers.lambdas;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.Intent;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.Slot;
import com.github.satr.ask.handlers.Intents;

import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class MesConsultaVueloIntent implements IntentRequestHandler {

    @Override
    public boolean canHandle(HandlerInput handlerInput, IntentRequest intentRequest){
        Slot mes = intentRequest.getIntent().getSlots().get("mes");
        Slot fecha = intentRequest.getIntent().getSlots().get("fecha");

        return (handlerInput.matches(intentName(Intents.CONSULTA_VUELO_INTENT))
                && mes.getValue() == null
                && fecha.getValue() == null);
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput, IntentRequest intentRequest) {
        Intent intent = intentRequest.getIntent();
        String speechText = "Dígame el mes para el que desea consultar.";

        return handlerInput.getResponseBuilder()
                .withSpeech(speechText)
                .withReprompt(speechText)
                .addElicitSlotDirective("mes", intent)
                .build();
    }

}
