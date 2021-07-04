package com.github.satr.ask.handlers.lambdas;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.github.satr.ask.handlers.Intents;
import com.github.satr.ask.handlers.Modelo.Database;

import java.text.SimpleDateFormat;
import java.util.Optional;
import java.sql.*;

import static com.amazon.ask.request.Predicates.intentName;

public class VerMisVuelos implements RequestHandler {

    public final SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
    public final SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");

    @Override
    public boolean canHandle(HandlerInput handlerInput ) {
        return handlerInput.matches(intentName(Intents.VER_MIS_VUELOS_INTENT));
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput) {
        String speechText = "";

        Database bd = new Database();
        bd.abrirConexion();

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

        else {
            ResultSet rsReservas = bd.ejecutarConsulta("SELECT * FROM reservas WHERE usuario_id=" + idUsuario);

            try {

                if (!rsReservas.next()){
                    speechText = "No tienes ninguna reserva realizada.";
                }

                else{
                    rsReservas = bd.ejecutarConsulta("SELECT * FROM reservas WHERE usuario_id=" + idUsuario);
                    speechText = "Tienes las siguientes reservas hechas. ";

                    while (rsReservas.next()) {
                        ResultSet rsVuelo = bd.ejecutarConsulta("SELECT * FROM vuelos WHERE id=" + rsReservas.getInt("vuelo_id"));

                        while (rsVuelo.next()){
                            speechText += " Un vuelo de " + rsVuelo.getString("origen") + " a " + rsVuelo.getString("destino") +
                                    " el dia " + sdfDate.format(rsVuelo.getDate("fecha")) +
                                    " a las " + sdfTime.format(rsVuelo.getTime("salida")) + ".";
                        }

                    }

                    rsReservas.close();
                }

            } catch (SQLException ex) {
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

