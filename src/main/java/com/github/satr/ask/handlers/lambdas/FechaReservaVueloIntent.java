package com.github.satr.ask.handlers.lambdas;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.Intent;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.Slot;
import com.github.satr.ask.handlers.Intents;
import com.github.satr.ask.handlers.Modelo.Calendar;
import com.github.satr.ask.handlers.Modelo.Database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class FechaReservaVueloIntent implements IntentRequestHandler {

    public final SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
    public final SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
    public final SimpleDateFormat sdfMonth = new SimpleDateFormat("yyyy-MM");

    @Override
    public boolean canHandle(HandlerInput handlerInput , IntentRequest intentRequest) {
        Slot fecha = intentRequest.getIntent().getSlots().get("fecha");
        Slot accion = intentRequest.getIntent().getSlots().get("accion");

        return (handlerInput.matches(intentName(Intents.RESERVA_VUELO_INTENT))
                && fecha.getValue() == null && accion.getValue() == null);
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput, IntentRequest intentRequest) {
        Intent intent = intentRequest.getIntent();
        String origen = intent.getSlots().get("origen").getValue();
        String destino = intent.getSlots().get("destino").getValue();
        String mes = intent.getSlots().get("mes").getValue();
        String precioMaximo = intent.getSlots().get("precioMaximo").getValue();

        String speechText = "";
        Database bd = new Database();
        bd.abrirConexion();

        String consulta = "SELECT * FROM vuelos WHERE origen=" + "'" + origen + "'" +
                " and destino=" + "'" + destino + "'" + " AND precio<=" + "'" +
                precioMaximo + "'" + " AND billetes>0";

        consulta += " ORDER BY fecha";

        ResultSet rsAux = bd.ejecutarConsulta(consulta);
        int total = 0;
        int id = -1;

        Date hoy = new Date();

        try {
            hoy = sdfDate.parse(sdfDate.format(hoy));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        try{
            while (rsAux.next()){
                if (mes.equals(sdfMonth.format(rsAux.getDate("fecha")))){
                    if (rsAux.getDate("fecha").compareTo(hoy) > 0) {
                        total++;
                        id = rsAux.getInt("id");
                    }
                }
            }

        } catch (SQLException ex){
            ex.printStackTrace();
        }

        if (total == 0){
            bd.cerrarConexion();
            speechText = "No hay vuelos disponibles con estas caracter??sticas. Pruebe, si lo desea, " +
                    "otro mes para reservar.";

            return handlerInput.getResponseBuilder()
                    .withSpeech(speechText)
                    .withReprompt(speechText)
                    .addElicitSlotDirective("mes", intent)
                    .build();
        }

        else if (total == 1){
            rsAux = bd.ejecutarConsulta("SELECT * FROM vuelos WHERE id=" + id);
            speechText = "Tenemos un vuelo de ";
            String f = "";
            String t = "";

            try{
                while (rsAux.next()){
                    speechText += rsAux.getString("origen") + " a " +
                            rsAux.getString("destino") + " para el dia " +
                            sdfDate.format(rsAux.getDate("fecha")) + " a las " +
                            sdfTime.format(rsAux.getTime("salida")) + " por " +
                            rsAux.getFloat("precio") + " euros.";

                    f = sdfDate.format(rsAux.getDate("fecha"));
                    t = sdfTime.format(rsAux.getTime("salida"));

                }

            } catch (SQLException ex){
                ex.printStackTrace();
            }

            Calendar c = new Calendar();
            ArrayList<String> res = (ArrayList<String>) c.obtenerEventos(handlerInput, f);

            if (res.size() > 0){
                speechText += " Tenga en cuenta que ese mismo d??a tiene ";
            }

            for (int i = 0; i < res.size(); i++){
                speechText += res.get(i);

                if ((i + 1) < res.size()){
                    speechText += ", ";
                }

                else{
                    speechText += ".";
                }
            }

            speechText += " ??Desea reservarlo?";

            Slot updateSlotFecha = Slot.builder()
                    .withName("fecha")
                    .withValue(f)
                    .build();

            intent.getSlots().put("fecha", updateSlotFecha);

            Slot updateSlotSalida = Slot.builder()
                    .withName("salida")
                    .withValue(t)
                    .build();

            intent.getSlots().put("salida", updateSlotSalida);

            Slot updateSlotAccion = Slot.builder()
                    .withName("accion")
                    .withValue(Integer.toString(id))
                    .build();

            intent.getSlots().put("accion", updateSlotAccion);

            bd.cerrarConexion();

            return handlerInput.getResponseBuilder()
                    .withSpeech(speechText)
                    .withReprompt(speechText)
                    .addConfirmSlotDirective("accion", intent)
                    .build();

        }


        else{
            rsAux = bd.ejecutarConsulta(consulta);

            try{
                speechText = "Tenemos los siguientes vuelos disponibles.";

                hoy = new Date();

                try {
                    hoy = sdfDate.parse(sdfDate.format(hoy));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                while (rsAux.next()){
                    if (mes.equals(sdfMonth.format(rsAux.getDate("fecha")))){
                        if (rsAux.getDate("fecha").compareTo(hoy) > 0){
                            speechText += " Un vuelo el d??a " + sdfDate.format(rsAux.getDate("fecha")) +
                                    " a las " + sdfTime.format(rsAux.getTime("salida")) + " por " +
                                    rsAux.getFloat("precio") + " euros.";
                        }
                    }
                }

                speechText += " D??game el d??a concreto para el que quiere reservar.";

                bd.cerrarConexion();

            } catch (SQLException ex){
                ex.printStackTrace();
            }

            return handlerInput.getResponseBuilder()
                    .withSpeech(speechText)
                    .withReprompt(speechText)
                    .addElicitSlotDirective("fecha", intent)
                    .build();
        }

    }

}
