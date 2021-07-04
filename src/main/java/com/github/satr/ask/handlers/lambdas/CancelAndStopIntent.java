package com.github.satr.ask.handlers.lambdas;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.github.satr.ask.handlers.Intents;

import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class CancelAndStopIntent implements RequestHandler {
    @Override
    public boolean canHandle(HandlerInput handlerInput) {
        return handlerInput.matches(intentName(Intents.AMAZON_STOP).or(intentName(Intents.AMAZON_CANCEL)));
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput) {
        return handlerInput.getResponseBuilder()
                .withSpeech("Buen viaje, amigo")
                .withSimpleCard("Sesión cerrada", "Nos vemos pronto")
                .build();
    }

}
