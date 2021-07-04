package com.github.satr.ask.handlers.lambdas;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.*;
import com.github.satr.ask.handlers.Intents;

import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class PrecioDefectoConsultaVueloIntent implements IntentRequestHandler {

    @Override
    public boolean canHandle(HandlerInput handlerInput , IntentRequest intentRequest) {
        Slot precioMaximo = intentRequest.getIntent().getSlots().get("precioMaximo");

        return handlerInput.matches(intentName(Intents.CONSULTA_VUELO_INTENT))
                && precioMaximo.getValue() == null
                && precioMaximo.getConfirmationStatus() == SlotConfirmationStatus.NONE;
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput, IntentRequest intentRequest) {
        Intent intent = intentRequest.getIntent();

        String speechText = "No has especificado ningún precio máximo, así que no tendremos en cuenta " +
                "ninguno. ¿De acuerdo?";

        Slot updateSlot = Slot.builder()
                .withName("precioMaximo")
                .withValue("0")
                .build();

        intent.getSlots().put("precioMaximo", updateSlot);

        return handlerInput.getResponseBuilder()
                .withSpeech(speechText)
                .withReprompt(speechText)
                .addConfirmSlotDirective("precioMaximo", intent)
                .build();
    }

}
