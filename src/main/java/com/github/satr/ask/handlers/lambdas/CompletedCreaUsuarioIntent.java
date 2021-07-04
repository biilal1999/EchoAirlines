package com.github.satr.ask.handlers.lambdas;

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.*;
import com.github.satr.ask.handlers.Intents;
import com.github.satr.ask.handlers.Modelo.Database;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class CompletedCreaUsuarioIntent implements IntentRequestHandler {

    @Override
    public boolean canHandle(HandlerInput handlerInput, IntentRequest intentRequest) {
        return handlerInput.matches(intentName(Intents.CREA_USUARIO_INTENT))
                && intentRequest.getDialogState() == DialogState.COMPLETED;
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput, IntentRequest intentRequest) {
        Intent intent = intentRequest.getIntent();
        Map<String, Slot> slots = intent.getSlots();

        String nombre = slots.get("nombre").getValue();
        String clave = slots.get("clave").getValue();
        String telefono = slots.get("telefono").getValue();
        String pais = slots.get("pais").getValue();
        String ciudad = slots.get("ciudad").getValue();
        String tarjeta = slots.get("tarjeta").getValue();

        Database bd = new Database();
        bd.abrirConexion();

        try{
            PreparedStatement st = bd.ejecutarInsercion("INSERT INTO users (nombre, clave, telefono, pais, ciudad, tarjeta) VALUES (?,?,?,?,?,?)");
            st.setString(1, nombre);
            st.setString(2, clave);
            st.setString(3, telefono);
            st.setString(4, pais);
            st.setString(5, ciudad);
            st.setString(6, tarjeta);
            st.execute();

        } catch (SQLException ex){
            ex.printStackTrace();
        }

        bd.cerrarConexion();

        String speechText = "Te damos la bienvenida, " + nombre;

        return handlerInput.getResponseBuilder()
                .withSpeech(speechText)
                .withSimpleCard("Consulta", speechText)
                .withReprompt(speechText)
                .build();
    }
}
