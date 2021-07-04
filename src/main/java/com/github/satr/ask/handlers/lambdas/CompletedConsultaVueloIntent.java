package com.github.satr.ask.handlers.lambdas;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.*;
import com.github.satr.ask.handlers.Intents;
import com.github.satr.ask.handlers.Modelo.Calendar;
import com.github.satr.ask.handlers.Modelo.Database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class CompletedConsultaVueloIntent implements IntentRequestHandler {

    public final SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
    public final SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
    public final SimpleDateFormat sdfMonth = new SimpleDateFormat("yyyy-MM");

    @Override
    public boolean canHandle(HandlerInput handlerInput , IntentRequest intentRequest) {
        return handlerInput.matches(intentName(Intents.CONSULTA_VUELO_INTENT))
                && intentRequest.getDialogState() == DialogState.COMPLETED;
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput, IntentRequest intentRequest){
        Map<String, Slot> slots = intentRequest.getIntent().getSlots();
        String ciudadOrigen = slots.get("origen").getValue();
        String ciudadDestino = slots.get("destino").getValue();
        String fecha = slots.get("fecha").getValue();
        String mes = slots.get("mes").getValue();
        String precioMaximo = slots.get("precioMaximo").getValue();

        boolean soloMes = true;
        String consulta = "SELECT * FROM vuelos WHERE origen=" + "'" + ciudadOrigen + "'" +
                " and destino=" + "'" + ciudadDestino + "'";

        if (!fecha.equals("1999-08-15")){
            consulta += " and fecha=" + "'" + fecha + "'";
            soloMes = false;
        }

        if (!precioMaximo.equals("0")){
            //float pMaximo = Float.parseFloat(precioMaximo);
            consulta += " and precio<=" + "'" + precioMaximo + "'";
        }

        consulta += " ORDER BY fecha";

        Database bd = new Database();
        bd.abrirConexion();

        ResultSet resultado = bd.ejecutarConsulta(consulta);
        String speechText = "";

        try{
            if (!resultado.next()) {
                speechText = "No se han encontrado vuelos con estas condiciones. Lo siento.";
            }

            else{
                speechText = "Están disponibles los siguientes vuelos.";

                //speechText = consulta;

                Date hoy = new Date();
                hoy = sdfDate.parse(sdfDate.format(hoy));
                resultado = bd.ejecutarConsulta(consulta);
                int contador = 0;
                int iden = -1;
                String f = "";
                String s = "";

                while (resultado.next()){
                    if (resultado.getInt("billetes") > 0) {
                        if (resultado.getDate("fecha").compareTo(hoy) > 0){
                            if ((!soloMes) || (soloMes && mes.equals(sdfMonth.format(resultado.getDate("fecha"))))) {
                                contador++;
                                iden = resultado.getInt("id");
                                speechText += " Un vuelo de " + resultado.getString("origen") + " a " + resultado.getString("destino")
                                        + " el día " + sdfDate.format(resultado.getDate("fecha")) + " a las " +
                                        sdfTime.format(resultado.getTime("salida")) + " por " +
                                        resultado.getFloat("precio") + " euros.";

                                f = sdfDate.format(resultado.getDate("fecha"));
                                s = sdfTime.format(resultado.getTime("salida"));
                            }
                        }
                    }
                }

                resultado.close();

                if (contador == 0){
                    speechText = "No se han encontrado vuelos con estas condiciones. Lo siento.";
                }

                else if (contador == 1){
                    Calendar c = new Calendar();
                    Object o = c.obtenerEventos(handlerInput, f);
                    ArrayList<String> res = new ArrayList<>();

                    if (o instanceof java.util.Optional){
                        return handlerInput.getResponseBuilder()
                                .withSpeech("Vuelve a vincular con tu cuenta de Google y concede permisos a todo lo que se sollicite")
                                .withLinkAccountCard()
                                .build();
                    }

                    else{
                        res = (ArrayList<String>) o;
                    }

                    if (res.size() > 0){
                        speechText += " Tenga en cuenta que ese mismo día tiene ";
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


                    speechText += " ¿Desea reservarlo?";

                    Slot updateOrigen = Slot.builder()
                            .withName("origen")
                            .withValue(ciudadOrigen)
                            .build();

                    Intent updateIntentReserva = Intent.builder()
                            .withName(Intents.RESERVA_VUELO_INTENT)
                            .putSlotsItem("origen", updateOrigen)
                            .build();


                    Slot updateDestino = Slot.builder()
                            .withName("destino")
                            .withValue(ciudadDestino)
                            .build();

                    updateIntentReserva.getSlots().put("destino", updateDestino);

                    Slot updatePrecioMaximo = Slot.builder()
                            .withName("precioMaximo")
                            .withValue(precioMaximo)
                            .build();

                    updateIntentReserva.getSlots().put("precioMaximo", updatePrecioMaximo);

                    Slot updateFecha = Slot.builder()
                            .withName("fecha")
                            .withValue(f)
                            .build();

                    updateIntentReserva.getSlots().put("fecha", updateFecha);

                    Slot updateSalida = Slot.builder()
                            .withName("salida")
                            .withValue(s)
                            .build();

                    updateIntentReserva.getSlots().put("salida", updateSalida);

                    Slot updateAccion = Slot.builder()
                            .withName("accion")
                            .withValue(Integer.toString(iden))
                            .build();

                    updateIntentReserva.getSlots().put("accion", updateAccion);

                    return handlerInput.getResponseBuilder()
                            .withSpeech(speechText)
                            .addConfirmIntentDirective(updateIntentReserva)
                            .build();

                }


/*
                if (contador == 1){
                    /*Calendar c = new Calendar();
                    ArrayList<String> res = c.obtenerEventos(handlerInput, f);

                    if (res.size() > 0){
                        speechText += " Tenga en cuenta que ese mismo día tiene ";
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

                    speechText += " ¿Desea reservarlo?";

                    if (slots.get("destino").getConfirmationStatus() == SlotConfirmationStatus.NONE){
                        System.out.println("Solo una vez debo entrar aquí");
                        return handlerInput.getResponseBuilder()
                                .withSpeech(speechText)
                                .withReprompt("¿Desea reservarlo?")
                                .addConfirmSlotDirective("destino", intentRequest.getIntent())
                                .build();
                    }

                    else{

                        Slot updateOrigen = Slot.builder()
                                .withName("origen")
                                .withValue(ciudadOrigen)
                                .build();

                        Intent updateIntentReserva = Intent.builder()
                                .withName(Intents.RESERVA_VUELO_INTENT)
                                .putSlotsItem("origen", updateOrigen)
                                .build();

                        System.out.println(updateIntentReserva.getName());
                        System.out.println(updateOrigen.getName());
                        System.out.println(updateOrigen.getValue());
                        System.out.println(updateIntentReserva.getSlots().toString());

                       // updateIntentReserva.getSlots().put("origen", updateOrigen);

                        Slot updateDestino = Slot.builder()
                                .withName("destino")
                                .withValue(ciudadDestino)
                                .build();

                        updateIntentReserva.getSlots().put("destino", updateDestino);

                        Slot updatePrecioMaximo = Slot.builder()
                                .withName("precioMaximo")
                                .withValue(precioMaximo)
                                .build();

                        updateIntentReserva.getSlots().put("precioMaximo", updatePrecioMaximo);

                        Slot updateFecha = Slot.builder()
                                .withName("fecha")
                                .withValue(f)
                                .build();

                        updateIntentReserva.getSlots().put("fecha", updateFecha);

                        Slot updateSalida = Slot.builder()
                                .withName("salida")
                                .withValue(s)
                                .build();

                        updateIntentReserva.getSlots().put("salida", updateSalida);

                        System.out.println("estamos aquí");
                        System.out.println(updateIntentReserva.getSlots().toString());


                        if (slots.get("destino").getConfirmationStatus() == SlotConfirmationStatus.CONFIRMED){
                            System.out.println("El valor es " + iden);
                            Slot updateAccion = Slot.builder()
                                    .withName("accion")
                                    .withValue(Integer.toString(iden))
                                    .withConfirmationStatus(SlotConfirmationStatus.CONFIRMED)
                                    .build();

                            updateIntentReserva.getSlots().put("accion", updateAccion);
                            System.out.println(updateAccion.getConfirmationStatus().toString());

                            System.out.println("confirmamos");
                        }

                        else if (slots.get("destino").getConfirmationStatus() == SlotConfirmationStatus.DENIED){
                            Slot updateAccion = Slot.builder()
                                    .withName("accion")
                                    .withValue(Integer.toString(iden))
                                    .withConfirmationStatus(SlotConfirmationStatus.DENIED)
                                    .build();

                            updateIntentReserva.getSlots().put("accion", updateAccion);

                            System.out.println("denegamos");
                        }

                        System.out.println("Hemos llegado al final");
                        System.out.println(updateIntentReserva.getName());
                        System.out.println(updateIntentReserva.getSlots().toString());

                        return handlerInput.getResponseBuilder()
                                .withSpeech("Vamos a proceder a realizar la reserva")
                                .addDelegateDirective(updateIntentReserva)
                                .build();
                    }
                }*/
            }

        } catch (SQLException | ParseException e) {
            e.printStackTrace();
        }

        bd.cerrarConexion();

        return handlerInput.getResponseBuilder()
                .withSpeech(speechText)
                .withReprompt(speechText)
                .build();
    }

}
