package com.github.satr.ask.handlers.lambdas;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.*;
import com.github.satr.ask.handlers.Intents;
import com.github.satr.ask.handlers.Modelo.Database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class AuxReservaVueloIntent implements IntentRequestHandler {

    @Override
    public boolean canHandle(HandlerInput handlerInput , IntentRequest intentRequest) {
        Slot mes = intentRequest.getIntent().getSlots().get("mes");
        Slot accion = intentRequest.getIntent().getSlots().get("accion");

        return (handlerInput.matches(intentName(Intents.RESERVA_VUELO_INTENT))
                && mes.getValue() == null && accion.getValue() == null);
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput, IntentRequest intentRequest) {
        Intent intent = intentRequest.getIntent();

        Database bd = new Database();
        bd.abrirConexion();

        String speechText = "";
        ResultSet rsVuelos = null;

        String origen = intent.getSlots().get("origen").getValue();
        String destino = intent.getSlots().get("destino").getValue();
        String precioMaximo = intent.getSlots().get("precioMaximo").getValue();

        try{
            String consulta = "SELECT * FROM vuelos WHERE origen=" + "'" + origen + "'" +
                    " and destino=" + "'" + destino + "'" + " and precio<=" + "'" +
                    precioMaximo + "'";

            rsVuelos = bd.ejecutarConsulta(consulta);

            if (!rsVuelos.next()){
                speechText = "No hay vuelos disponibles con estas características. Lo siento.";
            }

        } catch (SQLException ex){
            ex.printStackTrace();
        }

        bd.cerrarConexion();

        if (speechText.equals("No hay vuelos disponibles con estas características. Lo siento.")){
            return handlerInput.getResponseBuilder()
                    .withSpeech(speechText)
                    .withReprompt(speechText)
                    .build();
        }

        else{
            speechText = "Dígame el mes en el que quiere viajar.";

            return handlerInput.getResponseBuilder()
                    .withSpeech(speechText)
                    .withReprompt(speechText)
                    .addElicitSlotDirective("mes", intent)
                    .build();
        }

    }

}
