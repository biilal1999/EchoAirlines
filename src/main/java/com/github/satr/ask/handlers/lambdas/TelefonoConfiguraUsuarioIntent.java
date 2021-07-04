package com.github.satr.ask.handlers.lambdas;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.Slot;
import com.github.satr.ask.handlers.Intents;

import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class TelefonoConfiguraUsuarioIntent implements IntentRequestHandler {

    @Override
    public boolean canHandle(HandlerInput handlerInput , IntentRequest intentRequest) {
        Slot tipoDato = intentRequest.getIntent().getSlots().get("telefono");

        return handlerInput.matches(intentName(Intents.CONFIGURA_USUARIO_INTENT))
                && (intentRequest.getIntent().getSlots().get("dato").getValue().equals("telefono"))
                && tipoDato.getValue() == null;
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput, IntentRequest intentRequest) {
        String speechText = "¿Cuál quieres que sea tu nuevo número de teléfono de usuario?";
        String repromtText = "¿Cuál será tu nuevo número de teléfono de usuario?";

        return handlerInput.getResponseBuilder()
                .withSpeech(speechText)
                .withReprompt(repromtText)
                .addElicitSlotDirective("telefono", intentRequest.getIntent())
                .build();

    }
}
