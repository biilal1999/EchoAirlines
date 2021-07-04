package com.github.satr.ask.handlers.lambdas;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.*;
import com.github.satr.ask.handlers.Intents;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Optional;
import java.sql.*;

import static com.amazon.ask.request.Predicates.intentName;
import com.github.satr.ask.handlers.Modelo.Database;
//import com.sun.org.apache.xpath.internal.operations.Bool;
//import sun.java2d.pipe.SpanShapeRenderer;

public class ConsultaVueloIntent implements RequestHandler {

    public final SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
    public final SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");

    @Override
    public boolean canHandle(HandlerInput handlerInput ) {
        return handlerInput.matches(intentName(Intents.CONSULTA_VUELO_INTENT));
    }


    @Override
    public Optional<Response> handle(HandlerInput handlerInput) {
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

        String ciudadDefecto = "";
        String and = " and ";
        boolean hayAnterior = false;
        String consulta = "SELECT * from vuelos WHERE ";

        if (ciudadOrigen != null){
            hayAnterior = true;
            consulta += "origen=" + "'" + ciudadOrigen + "'";
        }

        else if (ciudadDestino == null){
            ResultSet rsCiudad = bd.ejecutarConsulta("SELECT * from users");

            try{
                while(rsCiudad.next()){
                    ciudadDefecto = rsCiudad.getString("ciudad");
                }

                rsCiudad.close();

            } catch (SQLException e) {
                e.printStackTrace();
            }

            consulta += "origen=" + "'" + ciudadDefecto + "'";
            hayAnterior = true;
        }

        if (ciudadDestino != null){
            if (hayAnterior){
                consulta += and;
            }

            consulta += "destino=" + "'" + ciudadDestino + "'";
            hayAnterior = true;
        }

        if (fecha != null){
            if (hayAnterior){
                consulta += and;
            }

            consulta += "fecha" + "'" + fecha + "'";
            hayAnterior = true;
        }

        if (horaSalida != null){
            if (hayAnterior){
                consulta += and;
            }

            consulta += "salida" + "'" + horaSalida + "'";
            hayAnterior = true;
        }

        ResultSet resultado = bd.ejecutarConsulta(consulta);
        String speechText = "";

        try{
            if (!resultado.next()) {
                speechText = "No se han encontrado vuelos con estas condiciones. Lo siento.";
                speechText += consulta;
            }

            else{
                speechText = "Están disponibles los siguientes vuelos.";

                //speechText = consulta;

                resultado = bd.ejecutarConsulta(consulta);

                while (resultado.next()){
                    speechText += " Un vuelo de " + resultado.getString("origen") + " a " + resultado.getString("destino")
                            + " el día " + sdfDate.format(resultado.getDate("fecha")) + " a las " +
                            sdfTime.format(resultado.getTime("salida")) + ".";
                }

                resultado.close();
            }

            } catch (SQLException e) {
            e.printStackTrace();
        }

        bd.cerrarConexion();

        //speechText = "El vuelo de " + ciudadOrigen + " a " + ciudadDestino + " sale el día " + fecha + " a las " + horaSalida;

        /*Database bd = new Database();

        bd.abrirConexion();

        ResultSet rs = bd.ejecutarConsulta("SELECT COUNT(*) AS total FROM vuelos WHERE inicio=" + "'" + ciudadOrigen + "'");
        int num = 0;

        try {
            while (rs.next()) {
                num = rs.getInt("total");
            }

        } catch (SQLException e){
            e.printStackTrace();
        }

        String speechText = "";


        if (num == 0){
            speechText = "Lo siento. No hay ningún vuelo con " + ciudadOrigen + " como punto de partida.";
        }

        else{
            speechText = "Hay " + num + " vuelos que salen desde " + ciudadOrigen;
        }

        //String speechText = ciudadOrigen + " es una ciudad muy bonita, pero " + ciudadDestino + " aún más.";

        bd.cerrarConexion();*/

        return handlerInput.getResponseBuilder()
                .withSpeech(speechText)
                .withSimpleCard("Consulta", speechText)
                .withReprompt(speechText)
                .build();
    }
}
