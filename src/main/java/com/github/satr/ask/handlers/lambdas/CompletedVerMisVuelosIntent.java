package com.github.satr.ask.handlers.lambdas;

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.*;
import com.github.satr.ask.handlers.Intents;
import com.github.satr.ask.handlers.Modelo.Database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class CompletedVerMisVuelosIntent implements IntentRequestHandler {

    public final SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
    public final SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");

    @Override
    public boolean canHandle(HandlerInput handlerInput , IntentRequest intentRequest) {
        Slot alias = intentRequest.getIntent().getSlots().get("alias");

        return handlerInput.matches(intentName(Intents.VER_MIS_VUELOS_INTENT))
                && alias.getValue() != null
                && alias.getConfirmationStatus() != SlotConfirmationStatus.DENIED;
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput, IntentRequest intentRequest){
        String speechText = "";

        Map<String, Slot> slots = intentRequest.getIntent().getSlots();
        String fecha = slots.get("fecha").getValue();
        String alias = slots.get("alias").getValue();
        String idCuenta = handlerInput.getRequestEnvelope().getSession().getUser().getUserId();
        AttributesManager attributesManager = handlerInput.getAttributesManager();
        Map<String, Object> attributes = attributesManager.getSessionAttributes();

        Database bd = new Database();
        bd.abrirConexion();

        int idPerfil = -1;

        if (!alias.equals("nada")){
            ResultSet rsPerfil = bd.ejecutarConsulta("SELECT * FROM perfiles WHERE alias=" + "'" + alias + "'" +
                    " AND cuenta=" + "'" + idCuenta + "'");

            try {
                while (rsPerfil.next()) {
                    idPerfil = rsPerfil.getInt("id");
                }

                rsPerfil.close();

            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            if (idPerfil == -1) {
                speechText = "No tienes ningún perfil registrado con este alias. Pruebe con otro alias si lo desea.";

                return handlerInput.getResponseBuilder()
                        .withSpeech(speechText)
                        .withReprompt(speechText)
                        .addElicitSlotDirective("alias", intentRequest.getIntent())
                        .build();
            }

        }


        //else {

        /*int idTitular = -1;

        ResultSet rsTitular = bd.ejecutarConsulta("SELECT * FROM perfiles WHERE cuenta=" + "'" + idCuenta + "'" +
                " AND titular=1");

        try{
            while (rsTitular.next()){
                idTitular = rsTitular.getInt("id");
            }

            rsTitular.close();

        } catch (SQLException ex){
            ex.printStackTrace();
        }*/

        int idTitular = Integer.parseInt(attributes.get("idTitularCuenta").toString());

        ResultSet rsReservas = bd.ejecutarConsulta("SELECT * FROM reservas WHERE titular_id=" + idTitular);

        try {

            if (!rsReservas.next()){
                speechText = "No tienes ninguna reserva realizada en esta cuenta de Alexa.";
            }

            else{

                if (alias.equals("nada")){
                    rsReservas = bd.ejecutarConsulta("SELECT * FROM reservas WHERE titular_id=" + idTitular);
                    speechText = "Tienes las siguientes reservas hechas. ";
                }

                else{
                    rsReservas = bd.ejecutarConsulta("SELECT * FROM reservas WHERE titular_id=" + idTitular +
                            " AND perfil_id=" + idPerfil);

                    speechText = "Tienes las siguientes reservas hechas para " + alias + ".";
                }

                while (rsReservas.next()) {
                    ResultSet rsVuelo = bd.ejecutarConsulta("SELECT * FROM vuelos WHERE id=" + rsReservas.getInt("vuelo_id"));

                    while (rsVuelo.next()){
                        if (fecha.equals(sdfDate.format(rsVuelo.getDate("fecha")))
                                || fecha.equals("1999-08-15")){
                            /*String auxDNI = rsReservas.getString("DNI");
                               String cadenaDNI = auxDNI.charAt(0) +  " " + auxDNI.charAt(1) + " " + auxDNI.charAt(2) + " " + auxDNI.charAt(3) + " " +
                                       auxDNI.charAt(4) + " " + auxDNI.charAt(5) + " " + auxDNI.charAt(6) + auxDNI.charAt(7);*/

                            speechText += " Un vuelo de " + rsVuelo.getString("origen") + " a " + rsVuelo.getString("destino") +
                                    " el dia " + sdfDate.format(rsVuelo.getDate("fecha")) +
                                    " a las " + sdfTime.format(rsVuelo.getTime("salida"));

                            if (alias.equals("nada")){
                                int auxPerfil = rsReservas.getInt("perfil_id");
                                String nombre = "";
                                ResultSet rsAlias = bd.ejecutarConsulta("SELECT * FROM perfiles WHERE id=" + auxPerfil +
                                        " AND cuenta=" + "'" + idCuenta + "'");

                                while (rsAlias.next()){
                                    nombre = rsAlias.getString("alias");
                                }

                                speechText += " para " + nombre;
                            }

                            speechText += ".";

                        }
                    }
                }

                if (speechText.equals("Tienes las siguientes reservas hechas. ")){
                    speechText = "No tienes reservas con estas características.";
                }

                else if (speechText.equals("Tienes las siguientes reservas hechas para " + alias + ".")){
                    speechText = "No tienes reservas para " + alias + " con estas características.";
                }

                rsReservas.close();
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        //}

        bd.cerrarConexion();

        return handlerInput.getResponseBuilder()
                .withSpeech(speechText)
                .withSimpleCard("Consulta", speechText)
                .withReprompt(speechText)
                .build();
    }

}
