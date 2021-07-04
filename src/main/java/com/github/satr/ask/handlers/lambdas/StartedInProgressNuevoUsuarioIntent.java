package com.github.satr.ask.handlers.lambdas;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.*;
import com.github.satr.ask.handlers.Intents;

import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class StartedInProgressNuevoUsuarioIntent implements IntentRequestHandler {

    @Override
    public boolean canHandle(HandlerInput handlerInput , IntentRequest intentRequest) {
        Slot confirmacion = intentRequest.getIntent().getSlots().get("confirmacion");

        return handlerInput.matches(intentName(Intents.CREA_USUARIO_INTENT))
                && confirmacion.getValue() == null
                && intentRequest.getDialogState() != DialogState.COMPLETED;
    }


    @Override
    public Optional<Response> handle(HandlerInput handlerInput, IntentRequest intentRequest) {
        Intent intent = intentRequest.getIntent();
        String tarjeta = intent.getSlots().get("tarjeta").getValue();
        String pais = intent.getSlots().get("pais").getValue();
        String ciudad = intent.getSlots().get("ciudad").getValue();
        String speechText = "";

        if (tarjeta != null){
            if (tarjeta.length() < 13 || tarjeta.length() > 16){
                speechText = "Tarjeta de crédito inválida. Pruebe de nuevo, por favor.";

                return handlerInput.getResponseBuilder()
                        .withSpeech(speechText)
                        .withReprompt(speechText)
                        .addElicitSlotDirective("tarjeta", intent)
                        .build();
            }

            else if (pais != null && ciudad != null){
                speechText = "¿Desea añadir una clave para las operaciones sobre vuelos que se hagan?";

                Slot updateSlot = Slot.builder()
                        .withName("confirmacion")
                        .withValue("si")
                        .build();

                intent.getSlots().put("confirmacion", updateSlot);

                return handlerInput.getResponseBuilder()
                        .withSpeech(speechText)
                        .withReprompt(speechText)
                        .addConfirmSlotDirective("confirmacion", intent)
                        .build();
            }
        }

        return handlerInput.getResponseBuilder()
                .addDelegateDirective(intent)
                .build();

    }

}
