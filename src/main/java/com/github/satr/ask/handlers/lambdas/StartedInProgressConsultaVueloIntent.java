package com.github.satr.ask.handlers.lambdas;

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.*;
import com.github.satr.ask.handlers.Intents;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class StartedInProgressConsultaVueloIntent implements IntentRequestHandler {

    public final SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
    public final SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");

    @Override
    public boolean canHandle(HandlerInput handlerInput , IntentRequest intentRequest) {
        return handlerInput.matches(intentName(Intents.CONSULTA_VUELO_INTENT))
                && intentRequest.getDialogState() != DialogState.COMPLETED;
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput, IntentRequest intentRequest) {
        Intent intent = intentRequest.getIntent();
        String ciudadOrigen = intent.getSlots().get("origen").getValue();
        AttributesManager attributesManager = handlerInput.getAttributesManager();
        Map<String, Object> attributes = attributesManager.getSessionAttributes();
        String ciudadDefecto = "";

        if (ciudadOrigen == null){
            if (!attributes.containsKey("ciudad")){
                String p = "read::alexa:device:all:address";
                List<String> permisos = new ArrayList<String>();
                permisos.add(p);

                return handlerInput.getResponseBuilder()
                        .withSpeech("Debes conceder permiso para acceder a tu ubicación")
                        .withAskForPermissionsConsentCard(permisos)
                        .build();
            }

            else{
                ciudadDefecto = attributes.get("ciudad").toString();
                ciudadDefecto = ciudadDefecto.toLowerCase();

                Slot updateSlot = Slot.builder()
                        .withName("origen")
                        .withValue(ciudadDefecto)
                        .build();

                intent.getSlots().put("origen", updateSlot);
            }
        }

        return handlerInput.getResponseBuilder()
                .addDelegateDirective(intent)
                .build();

    }

}
