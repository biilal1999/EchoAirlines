package com.github.satr.ask.handlers.lambdas;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.*;
import com.github.satr.ask.handlers.Intents;
import com.github.satr.ask.handlers.Modelo.Database;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Optional;
import java.util.Date;
import java.sql.*;

import static com.amazon.ask.request.Predicates.intentName;

public class ReservaVueloIntent implements RequestHandler {

    public final SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
    public final SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");

    @Override
    public boolean canHandle(HandlerInput handlerInput ) {
        return handlerInput.matches(intentName(Intents.RESERVA_VUELO_INTENT));
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput) {

        String speechText = "";

        Request request = handlerInput.getRequestEnvelope().getRequest();
        IntentRequest intentRequest = (IntentRequest) request;
        Intent intent = intentRequest.getIntent();
        Map<String, Slot> slots = intent.getSlots();

        String ciudadOrigen = slots.get("origen").getValue();
        String ciudadDestino = slots.get("destino").getValue();
        String fecha = slots.get("fecha").getValue();
        String horaSalida = slots.get("salida").getValue();

        String consulta = "SELECT * FROM vuelos WHERE origen=" + "'" + ciudadOrigen + "'" + " and " +
                "destino=" + "'" + ciudadDestino + "'" + " and " + "fecha=" + "'" + fecha + "'" +
                " and " + "salida=" + "'" + horaSalida + "'";


        Database bd = new Database();
        bd.abrirConexion();

        ResultSet aux = bd.ejecutarConsulta(consulta);

        try {
            if (!aux.next()) {
                speechText = "Lo siento. No hay ningún vuelo con esas características.";
            }

            else {
                int idUsuario = 0;
                ResultSet rsUsuario = bd.ejecutarConsulta("SELECT * from users");

                try{
                    while(rsUsuario.next()){
                        idUsuario = rsUsuario.getInt("id");
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                }

                rsUsuario.close();

                aux = bd.ejecutarConsulta(consulta);
                int idVuelo = 0;
                Date fechaReserva = null;
                int precioVuelo = 0;

                while (aux.next()){
                    idVuelo = aux.getInt("id");
                    fechaReserva = new Date();
                    precioVuelo = aux.getInt("precio");
                }

                speechText = "Confirmada la reserva del vuelo de " + ciudadOrigen + " a " + ciudadDestino +
                        " para el día " + fecha + " a las "  + horaSalida + ". Buen viaje.";

                aux.close();

                String insercion = "INSERT INTO reservas (usuario_id, vuelo_id, precio) VALUES (?,?,?)";

                PreparedStatement st = bd.ejecutarInsercion(insercion);
                st.setInt(1, idUsuario);
                st.setInt(2, idVuelo);
                //st.setDate(3, (java.sql.Date) fechaReserva);
                st.setInt(3, precioVuelo);
                st.execute();
            }

        } catch(SQLException ex){
            ex.printStackTrace();
        }

        bd.cerrarConexion();


        return handlerInput.getResponseBuilder()
                .withSpeech(speechText)
                .withSimpleCard("Hola caracola", speechText)
                .withReprompt(speechText)
                .build();
    }

}
