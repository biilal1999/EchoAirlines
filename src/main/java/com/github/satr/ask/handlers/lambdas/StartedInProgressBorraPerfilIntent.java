package com.github.satr.ask.handlers.lambdas;

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.*;
import com.github.satr.ask.handlers.Intents;
import com.github.satr.ask.handlers.Modelo.Database;
import com.github.satr.ask.handlers.Modelo.SHA256;

import javax.xml.crypto.Data;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class StartedInProgressBorraPerfilIntent implements IntentRequestHandler {


    @Override
    public boolean canHandle(HandlerInput handlerInput , IntentRequest intentRequest) {
        Slot alias = intentRequest.getIntent().getSlots().get("alias");

        return handlerInput.matches(intentName(Intents.BORRA_PERFIL_INTENT))
                && alias.getConfirmationStatus() == SlotConfirmationStatus.NONE;
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput, IntentRequest intentRequest) {
        Intent intent = intentRequest.getIntent();
        String idCuenta = handlerInput.getRequestEnvelope().getSession().getUser().getUserId();
        String alias = intent.getSlots().get("alias").getValue();
        String claveSeguridad = intent.getSlots().get("claveSeguridad").getValue();
        String speechText = "";

        AttributesManager attributesManager = handlerInput.getAttributesManager();
        Map<String, Object> attributes = attributesManager.getSessionAttributes();


        if (alias != null){
            Database bd = new Database();
            bd.abrirConexion();

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

            // Tenemos ya el alias y vamos ahora a por la clave

            else{

                String consultaClave = "SELECT * FROM usuarios WHERE id=" + "'" + idCuenta + "'";
                ResultSet rsConsulta = bd.ejecutarConsulta(consultaClave);
                String cs = null;

                try{
                    while (rsConsulta.next()){
                        cs = rsConsulta.getString("clave");
                    }

                    rsConsulta.close();

                } catch (SQLException ex){
                    ex.printStackTrace();
                }

                if (cs != null){

                    if (claveSeguridad == null){
                        String speech = "Dígame su clave de seguridad para poder continuar con la operación.";
                        bd.cerrarConexion();

                        return handlerInput.getResponseBuilder()
                                .withSpeech(speech)
                                .withReprompt(speech)
                                .withSimpleCard("Solicitud", speech)
                                .addElicitSlotDirective("claveSeguridad", intentRequest.getIntent())
                                .build();
                    }

                    else{
                        SHA256 h = new SHA256();
                        String key = h.getHash(claveSeguridad);

                        consultaClave = "SELECT COUNT(*) AS total FROM usuarios WHERE id=" + "'" + idCuenta + "'";
                        consultaClave += " AND clave=" + "'" + key + "'";
                        rsConsulta = bd.ejecutarConsulta(consultaClave);
                        int num = 0;

                        try{
                            while (rsConsulta.next()){
                                num = rsConsulta.getInt("total");
                            }

                            rsConsulta.close();

                        } catch (SQLException ex){
                            ex.printStackTrace();
                        }

                        if (num == 0){
                            if (!attributes.containsKey("intentos")){
                                int intentos = 1;
                                attributes.put("intentos", intentos);
                                attributesManager.setSessionAttributes(attributes);
                            }

                            else{

                                if (Integer.parseInt(attributes.get("intentos").toString()) < 3){
                                    int intentos = Integer.parseInt(attributes.get("intentos").toString()) + 1;
                                    attributes.remove("intentos");
                                    attributes.put("intentos", intentos);
                                    attributesManager.setSessionAttributes(attributes);
                                }

                                else{
                                    String errorSpeech = "Demasiados intentos fallidos. Sesión finalizada.";

                                    return handlerInput.getResponseBuilder()
                                            .withSpeech(errorSpeech)
                                            .withReprompt(errorSpeech)
                                            .withSimpleCard("Fallo", errorSpeech)
                                            .withShouldEndSession(true)
                                            .build();
                                }

                            }

                            String speech = "Clave incorrecta. Inténtelo de nuevo, por favor.";

                            return handlerInput.getResponseBuilder()
                                    .withSpeech(speech)
                                    .withReprompt(speech)
                                    .withSimpleCard("Solicitud", speech)
                                    .addElicitSlotDirective("claveSeguridad", intentRequest.getIntent())
                                    .build();
                        }

                        if (attributes.containsKey("intentos")){
                            attributes.remove("intentos");
                            attributesManager.setSessionAttributes(attributes);
                        }

                    }

                }

                // Ya tenemos la clave, si hace falta

                int num = 0;
                ResultSet rsBorrado = bd.ejecutarConsulta("SELECT COUNT(*) AS total FROM reservas WHERE " +
                        "titular_id=" + idTitular + " AND perfil_id=" + idPerfil);

                try{
                    while (rsBorrado.next()){
                        num = rsBorrado.getInt("total");
                    }

                    rsBorrado.close();

                } catch (SQLException ex){
                    ex.printStackTrace();
                }


                if (num > 0){
                    speechText = "Lo siento. No puedes borrar un perfil que tiene una reserva realizada.";
                    bd.cerrarConexion();

                    return handlerInput.getResponseBuilder()
                            .withSpeech(speechText)
                            .withReprompt(speechText)
                            .withSimpleCard("Borrado", speechText)
                            .build();
                }

                else{
                    speechText = "¿Estás seguro de que deseas borrar el perfil de " + alias + "?";
                    bd.cerrarConexion();

                    return handlerInput.getResponseBuilder()
                            .withSpeech(speechText)
                            .withReprompt(speechText)
                            .addConfirmSlotDirective("alias", intent)
                            .build();
                }

            }

        }

        else {

            return handlerInput.getResponseBuilder()
                    .addDelegateDirective(intent)
                    .build();
        }

    }
}
