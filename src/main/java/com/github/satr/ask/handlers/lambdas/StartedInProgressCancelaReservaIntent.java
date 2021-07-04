package com.github.satr.ask.handlers.lambdas;

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.*;
import com.github.satr.ask.handlers.Intents;
import com.github.satr.ask.handlers.Modelo.Database;
import org.apache.commons.codec.binary.StringUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class StartedInProgressCancelaReservaIntent implements IntentRequestHandler {

    public final SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
    public final SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");

    @Override
    public boolean canHandle(HandlerInput handlerInput , IntentRequest intentRequest) {
        Slot accion = intentRequest.getIntent().getSlots().get("accion");
        Slot salida = intentRequest.getIntent().getSlots().get("salida");

        return (handlerInput.matches(intentName(Intents.CANCELA_RESERVA_INTENT))
                && intentRequest.getDialogState() != DialogState.COMPLETED
                && accion.getValue() == null
                && salida.getValue() == null);
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput, IntentRequest intentRequest) {
        Intent intent = intentRequest.getIntent();
        String origen = intent.getSlots().get("origen").getValue();
        String destino = intent.getSlots().get("destino").getValue();
        String fecha = intent.getSlots().get("fecha").getValue();
        String alias = intent.getSlots().get("alias").getValue();
        String idCuenta = handlerInput.getRequestEnvelope().getSession().getUser().getUserId();
        String speechText = "";

        AttributesManager attributesManager = handlerInput.getAttributesManager();
        Map<String, Object> attributes = attributesManager.getSessionAttributes();

        if (alias != null){
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


            if (idPerfil == -1) {
                speechText = "No hay ningún perfil registrado con este alias. Dígame un alias de perfil registrado, si lo desea.";

                bd.cerrarConexion();

                return handlerInput.getResponseBuilder()
                        .withSpeech(speechText)
                        //.withSimpleCard("Cancela", speechText)
                        .withReprompt(speechText)
                        .addElicitSlotDirective("alias", intent)
                        .build();

            }

            else{

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


                ResultSet rsReserva = bd.ejecutarConsulta("SELECT * FROM reservas WHERE titular_id=" + idTitular);
                ResultSet rsAlias = bd.ejecutarConsulta("SELECT * FROM reservas WHERE titular_id=" + idTitular +
                        " AND perfil_id=" + idPerfil);

                try {

                    if (!rsReserva.next()) {
                        speechText = "No tienes ninguna reserva realizada en esta cuenta de Alexa.";
                        bd.cerrarConexion();

                        return handlerInput.getResponseBuilder()
                                .withSpeech(speechText)
                                .withSimpleCard("Cancela", speechText)
                                .withReprompt(speechText)
                                .build();
                    }

                    else if (!rsAlias.next()){
                        speechText = "No tienes ninguna reserva realizada para este alias. Pruebe otro alias de nuevo.";
                        bd.cerrarConexion();

                        return handlerInput.getResponseBuilder()
                                .withSpeech(speechText)
                                .withSimpleCard("Cancela", speechText)
                                .withReprompt(speechText)
                                .addElicitSlotDirective("alias", intent)
                                .build();
                    }

                    else if (origen != null && destino != null && fecha != null){

                        System.out.println("Los resultados son: ");
                        System.out.println(origen);
                        System.out.println(destino);
                        System.out.println(fecha);
                        System.out.println(alias);
                        System.out.println("ID PERFIL: " + idPerfil);
                        System.out.println("ID TITULAR: " + idTitular);

                        int total = 0;
                        int idVuelo = -1;
                        String salida = "";
                        rsReserva = bd.ejecutarConsulta("SELECT * FROM reservas WHERE " +
                                "perfil_id=" + idPerfil + " AND titular_id=" + "'" + idTitular + "'");

                        Date hoy = new Date();

                        try {
                            hoy = sdfDate.parse(sdfDate.format(hoy));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        //Date ayer = new Date(hoy.getTime() - 86400000); // Milisegundos entre ayer y hoy

                        try{

                            while (rsReserva.next()){
                                ResultSet rsVuelo = bd.ejecutarConsulta("SELECT * FROM vuelos WHERE " +
                                        "id=" + rsReserva.getInt("vuelo_id") + " AND origen=" + "'" +
                                        origen + "'" + " AND destino=" + "'" + destino + "'" + " AND " +
                                        "fecha=" + "'" + fecha + "'");

                                while (rsVuelo.next()){
                                    /*if (origen.equals(rsVuelo.getString("origen")) &&
                                            destino.equals(rsVuelo.getString("destino")) &&
                                            fecha.equals(sdfDate.format(rsVuelo.getDate("fecha")))){*/

                                        Date anterior = new Date(rsVuelo.getDate("fecha").getTime() - 86400000);
                                        // 86400000 son los milisegundos entre ayer y hoy

                                        if (anterior.compareTo(hoy) > 0){
                                            total++;
                                            idVuelo = rsVuelo.getInt("id");
                                            salida = sdfTime.format(rsVuelo.getTime("salida"));
                                        }


                                   // }
                                }

                            }

                        } catch (SQLException ex){
                            ex.printStackTrace();
                        }

                        if (total == 0){
                            bd.cerrarConexion();
                            speechText = "No tienes reservas con estas características disponibles para cancelarlas.";

                            return handlerInput.getResponseBuilder()
                                    .withSpeech(speechText)
                                    .withReprompt(speechText)
                                    .build();
                        }

                        else if (total == 1){
                            bd.cerrarConexion();
                            speechText = "¿Estás seguro de que deseas cancelar esta reserva para " + alias + "?";

                            Slot updateSlotSalida = Slot.builder()
                                    .withName("salida")
                                    .withValue(salida)
                                    .build();

                            intent.getSlots().put("salida", updateSlotSalida);

                            Slot updateSlotAccion = Slot.builder()
                                    .withName("accion")
                                    .withValue(Integer.toString(idVuelo))
                                    .build();

                            intent.getSlots().put("accion", updateSlotAccion);

                            return handlerInput.getResponseBuilder()
                                    .withSpeech(speechText)
                                    .withReprompt(speechText)
                                    .addConfirmSlotDirective("accion", intent)
                                    .build();
                        }

                        else{
                            rsReserva = bd.ejecutarConsulta("SELECT * FROM reservas WHERE " +
                                    "perfil_id=" + idPerfil + " AND titular_id=" +
                                    "'" + idTitular + "'");

                            hoy = new Date();

                            try {
                                hoy = sdfDate.parse(sdfDate.format(hoy));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            speechText = "Tienes, para estas características, los siguientes vuelos " +
                                    "reservados para " + alias + ". ";

                            try{

                                while (rsReserva.next()){
                                   /* ResultSet rsVuelo = bd.ejecutarConsulta("SELECT * FROM vuelos WHERE " +
                                            "id=" + rsReserva.getInt("vuelo_id"));*/

                                    ResultSet rsVuelo = bd.ejecutarConsulta("SELECT * FROM vuelos WHERE " +
                                            "id=" + rsReserva.getInt("vuelo_id") + " AND origen=" + "'" +
                                            origen + "'" + " AND destino=" + "'" + destino + "'" + " AND " +
                                            "fecha=" + "'" + fecha + "'");

                                    while (rsVuelo.next()){
                                        /*if (origen.equals(rsVuelo.getString("origen")) &&
                                                destino.equals(rsVuelo.getString("destino")) &&
                                                fecha.equals(sdfDate.format(rsVuelo.getDate("fecha")))){*/

                                            Date anterior = new Date(rsVuelo.getDate("fecha").getTime() - 86400000);

                                            // 86400000 son los milisegundos entre ayer y hoy

                                            if (anterior.compareTo(hoy) > 0){
                                                speechText += "Un vuelo a las " + rsVuelo.getString("salida") + " . ";
                                            }
                                        //}
                                    }

                                }

                            } catch (SQLException ex){
                                ex.printStackTrace();
                            }

                            speechText += " Dígame la hora del vuelo que desea cancelar.";
                            String repromptText = "Dígame la hora del vuelo que desea cancelar.";

                            return handlerInput.getResponseBuilder()
                                    .withSpeech(speechText)
                                    .withReprompt(repromptText)
                                    .addElicitSlotDirective("salida", intent)
                                    .build();

                        }

                    }

                    else{
                        bd.cerrarConexion();

                        return handlerInput.getResponseBuilder()
                                .addDelegateDirective(intent)
                                .build();
                    }

                } catch (SQLException ex){
                    ex.printStackTrace();
                }
            }
        }


        return handlerInput.getResponseBuilder()
                .withSpeech(speechText)
                .withSimpleCard("Cancela", speechText)
                .withReprompt(speechText)
                .addDelegateDirective(intent)
                .build();

    }

}
