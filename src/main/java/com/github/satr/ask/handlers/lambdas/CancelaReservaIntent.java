package com.github.satr.ask.handlers.lambdas;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.*;
import com.github.satr.ask.handlers.Intents;
import com.github.satr.ask.handlers.Modelo.Database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;


public class CancelaReservaIntent implements RequestHandler {

    public final SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
    public final SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");

    @Override
    public boolean canHandle(HandlerInput handlerInput ) {
        return handlerInput.matches(intentName(Intents.CANCELA_RESERVA_INTENT));
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput) {
        String speechText = "";

        Request request = handlerInput.getRequestEnvelope().getRequest();
        IntentRequest intentRequest = (IntentRequest) request;
        Intent intent = intentRequest.getIntent();
        Map<String, Slot> slots = intent.getSlots();

        Database bd = new Database();
        bd.abrirConexion();

        String ciudadOrigen = slots.get("origen").getValue();
        String ciudadDestino = slots.get("destino").getValue();
        String fecha = slots.get("fecha").getValue();
        String horaSalida = slots.get("salida").getValue();

        ResultSet rsUsuario = bd.ejecutarConsulta("SELECT * FROM users");
        int idUsuario = -1;

        try {
            while (rsUsuario.next()) {
                idUsuario = rsUsuario.getInt("id");
            }

            rsUsuario.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }


        if (idUsuario == -1) {
            speechText = "No tienes ningun usuario configurado. Empieza creando uno.";
        }

        else{
            ResultSet rsReservas = bd.ejecutarConsulta("SELECT * FROM reservas WHERE usuario_id=" + idUsuario);

            try {

                if (!rsReservas.next()) {
                    speechText = "No tienes ninguna reserva realizada.";
                }

                else{
                    rsReservas = bd.ejecutarConsulta("SELECT * FROM reservas WHERE usuario_id=" + idUsuario);
                    boolean encontrada = false;
                    int idVuelo = -1;

                    while (rsReservas.next() && !encontrada){
                        ResultSet rsVuelo = bd.ejecutarConsulta("SELECT * FROM vuelos WHERE id=" + rsReservas.getInt("vuelo_id"));

                        while (rsVuelo.next()){
                            if (rsVuelo.getString("origen").equals(ciudadOrigen) &&
                                rsVuelo.getString("destino").equals(ciudadDestino) &&
                                    sdfDate.format(rsVuelo.getDate("fecha")).equals(fecha) &&
                                        sdfTime.format(rsVuelo.getTime("salida")).equals(horaSalida)){

                                encontrada = true;
                                idVuelo = rsVuelo.getInt("id");
                            }
                        }
                    }

                    rsReservas.close();

                    if (encontrada){
                        String borrado = "DELETE FROM reservas WHERE vuelo_id=? and usuario_id=?";

                        PreparedStatement st = bd.ejecutarInsercion(borrado);
                        st.setInt(1, idVuelo);
                        st.setInt(2, idUsuario);
                        st.execute();

                        speechText = "Se ha cancelado correctamente la reserva.";
                    }

                    else{
                        speechText += "No tiene ninguna reserva hecha con estas características.";
                    }
                }


            } catch (SQLException ex){
                ex.printStackTrace();
            }
        }

        bd.cerrarConexion();

        return handlerInput.getResponseBuilder()
                .withSpeech(speechText)
                .withSimpleCard("Consulta", speechText)
                .withReprompt(speechText)
                .build();

    }
}
