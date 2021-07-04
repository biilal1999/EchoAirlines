package com.github.satr.ask.handlers.lambdas;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.github.satr.ask.handlers.Intents;

import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class HelpIntentHandler implements RequestHandler {
    @Override
    public boolean canHandle(HandlerInput handlerInput) {
        return handlerInput.matches(intentName((Intents.AMAZON_HELP)));
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput) {
        String speechText = "Solo tienes que decirme desde donde quieres viajar";
        return handlerInput.getResponseBuilder()
                .withSpeech(speechText)
                .withSimpleCard("Disponible", "salida del viaje")
                .withReprompt(speechText)
                .build();
    }


}
