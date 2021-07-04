package com.github.satr.ask.handlers.lambdas;

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.*;
import com.github.satr.ask.handlers.Intents;
import com.github.satr.ask.handlers.Modelo.Database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class ConfirmedBorraPerfilIntent implements IntentRequestHandler {

    @Override
    public boolean canHandle(HandlerInput handlerInput , IntentRequest intentRequest) {
        Slot alias = intentRequest.getIntent().getSlots().get("alias");

        return handlerInput.matches(intentName(Intents.BORRA_PERFIL_INTENT))
                && alias.getConfirmationStatus() == SlotConfirmationStatus.CONFIRMED;
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput, IntentRequest intentRequest) {
        Intent intent = intentRequest.getIntent();
        String idCuenta = handlerInput.getRequestEnvelope().getSession().getUser().getUserId();
        String alias = intent.getSlots().get("alias").getValue();
        String speechText = "";
        AttributesManager attributesManager = handlerInput.getAttributesManager();
        Map<String, Object> attributes = attributesManager.getSessionAttributes();

        Database bd = new Database();
        bd.abrirConexion();

        String borrado = "DELETE FROM perfiles WHERE cuenta=? AND alias=?";

        try{
            PreparedStatement st = bd.ejecutarInsercion(borrado);
            st.setString(1, idCuenta);
            st.setString(2, alias);

            st.execute();
            st.close();

        } catch (SQLException ex){
            ex.printStackTrace();
        }

        bd.cerrarConexion();

        speechText = "Perfil de " + alias + " eliminado con éxito. Hasta pronto.";

        return handlerInput.getResponseBuilder()
                .withSpeech(speechText)
                .withReprompt(speechText)
                .withSimpleCard("Borrado", speechText)
                .build();


    }


}
