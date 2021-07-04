package com.github.satr.ask.handlers.lambdas;

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.*;
import com.github.satr.ask.handlers.Intents;
import com.github.satr.ask.handlers.Modelo.Database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class AccionCancelaReservaIntent implements IntentRequestHandler {

    public final SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
    public final SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");

    @Override
    public boolean canHandle(HandlerInput handlerInput , IntentRequest intentRequest) {
        Slot salida = intentRequest.getIntent().getSlots().get("salida");
        Slot accion = intentRequest.getIntent().getSlots().get("accion");

        return (handlerInput.matches(intentName(Intents.CANCELA_RESERVA_INTENT))
                && salida.getValue() != null
                && accion.getValue() == null);
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput, IntentRequest intentRequest) {
        Intent intent = intentRequest.getIntent();
        String origen = intent.getSlots().get("origen").getValue();
        String destino = intent.getSlots().get("destino").getValue();
        String fecha = intent.getSlots().get("fecha").getValue();
        String salida = intent.getSlots().get("salida").getValue();
        String alias = intent.getSlots().get("alias").getValue();
        String idCuenta = handlerInput.getRequestEnvelope().getSession().getUser().getUserId();
        AttributesManager attributesManager = handlerInput.getAttributesManager();
        Map<String, Object> attributes = attributesManager.getSessionAttributes();
        String speechText = "";

        Database bd = new Database();
        bd.abrirConexion();

        int idPerfil = -1;
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

        ResultSet rsReservas = bd.ejecutarConsulta("SELECT * FROM reservas WHERE " +
                "perfil_id=" + idPerfil + " AND titular_id=" + "'" + idTitular + "'");

        boolean encontrado = false;
        int id = -1;

        Date hoy = new Date();

        try {
            hoy = sdfDate.parse(sdfDate.format(hoy));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        try{

            while (rsReservas.next() && !encontrado){
                //ResultSet rsVuelo = bd.ejecutarConsulta("SELECT * FROM vuelos WHERE id=" + rsReservas.getInt("vuelo_id"));

                ResultSet rsVuelo = bd.ejecutarConsulta("SELECT * FROM vuelos WHERE " +
                        "id=" + rsReservas.getInt("vuelo_id") + " AND origen=" + "'" +
                        origen + "'" + " AND destino=" + "'" + destino + "'" + " AND " +
                        "fecha=" + "'" + fecha + "'");

                while (rsVuelo.next()){
                    /*if (rsVuelo.getString("origen").equals(origen) &&
                            rsVuelo.getString("destino").equals(destino) &&
                            sdfDate.format(rsVuelo.getDate("fecha")).equals(fecha) &&
                            sdfTime.format(rsVuelo.getTime("salida")).equals(salida)){*/

                    if (sdfTime.format(rsVuelo.getTime("salida")).equals(salida)){

                        Date anterior = new Date(rsVuelo.getDate("fecha").getTime() - 86400000);

                        // 86400000 son los milisegundos entre ayer y hoy

                        if (anterior.compareTo(hoy) > 0){
                            encontrado = true;
                            id = rsVuelo.getInt("id");
                        }
                    }
                }
            }

        } catch (SQLException ex){
            ex.printStackTrace();
        }


        if (encontrado){
            bd.cerrarConexion();
            speechText = "¿Estás seguro de que deseas cancelar la reserva para " + alias + "?";

            Slot updateSlotAccion = Slot.builder()
                    .withName("accion")
                    .withValue(Integer.toString(id))
                    .build();

            intent.getSlots().put("accion", updateSlotAccion);


            return handlerInput.getResponseBuilder()
                    .withSpeech(speechText)
                    .withReprompt(speechText)
                    .addConfirmSlotDirective("accion", intent)
                    .build();
        }

        else{
            bd.cerrarConexion();
            speechText = "La hora de salida no coincide, lo siento. Pruebe de nuevo si lo desea.";

            return handlerInput.getResponseBuilder()
                    .withSpeech(speechText)
                    .withReprompt(speechText)
                    .addElicitSlotDirective("salida", intent)
                    .build();
        }

    }

}
