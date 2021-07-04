package com.github.satr.ask.handlers.lambdas;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.*;
import com.github.satr.ask.handlers.Intents;

import java.text.SimpleDateFormat;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class FechaDefectoVerMisVuelosIntent implements IntentRequestHandler {

    public final SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
    public final SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");

    @Override
    public boolean canHandle(HandlerInput handlerInput , IntentRequest intentRequest) {
        Slot fecha = intentRequest.getIntent().getSlots().get("fecha");

        return handlerInput.matches(intentName(Intents.VER_MIS_VUELOS_INTENT))
                && fecha.getValue() == null
                && fecha.getConfirmationStatus() == SlotConfirmationStatus.NONE;
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput, IntentRequest intentRequest) {
        Intent intent = intentRequest.getIntent();

        String speechText = "No has especificado ninguna fecha, así que buscaremos todos tus " +
                "vuelos sin tener en cuenta las fechas. ¿De acuerdo?";

        String f = "1999-08-15";

        Slot updateSlot = Slot.builder()
                .withName("fecha")
                .withValue(f)
                .build();

        intent.getSlots().put("fecha", updateSlot);

        return handlerInput.getResponseBuilder()
                .withSpeech(speechText)
                .withReprompt(speechText)
                .addConfirmSlotDirective("fecha", intent)
                .build();
    }

}
