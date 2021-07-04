package com.github.satr.ask.handlers.lambdas;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.Slot;
import com.github.satr.ask.handlers.Intents;

import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class CiudadConfiguraUsuarioIntent implements IntentRequestHandler {

    @Override
    public boolean canHandle(HandlerInput handlerInput , IntentRequest intentRequest) {
        Slot tipoDato = intentRequest.getIntent().getSlots().get("ciudad");

        return handlerInput.matches(intentName(Intents.CONFIGURA_USUARIO_INTENT))
                && (intentRequest.getIntent().getSlots().get("dato").getValue().equals("ciudad"))
                && tipoDato.getValue() == null;
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput, IntentRequest intentRequest) {
        String speechText = "¿En qué ciudad vives ahora?";
        String repromtText = "¿Cuál es la ciudad en la que vives ahora?";

        return handlerInput.getResponseBuilder()
                .withSpeech(speechText)
                .withReprompt(repromtText)
                .addElicitSlotDirective("ciudad", intentRequest.getIntent())
                .build();

    }
}
