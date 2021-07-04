package com.github.satr.ask.handlers.lambdas;

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.*;
import com.github.satr.ask.handlers.Intents;
import com.github.satr.ask.handlers.Modelo.Database;
import com.github.satr.ask.handlers.Modelo.SHA256;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class CompletedConfiguraPerfilIntent implements IntentRequestHandler {

    @Override
    public boolean canHandle(HandlerInput handlerInput , IntentRequest intentRequest) {
        Slot telefono = intentRequest.getIntent().getSlots().get("telefono");

        return handlerInput.matches(intentName(Intents.CONFIGURA_PERFIL_INTENT))
                && telefono.getValue() != null;
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput, IntentRequest intentRequest) {
        Intent intent = intentRequest.getIntent();
        String alias = intent.getSlots().get("alias").getValue();
        String DNI = intent.getSlots().get("DNI").getValue();
        String telefono = intent.getSlots().get("telefono").getValue();
        String idCuenta = handlerInput.getRequestEnvelope().getSession().getUser().getUserId();
        String claveSeguridad = intent.getSlots().get("claveSeguridad").getValue();
        String speechText = "";
        AttributesManager attributesManager = handlerInput.getAttributesManager();
        Map<String, Object> attributes = attributesManager.getSessionAttributes();
        SHA256 h = new SHA256();

        Database bd = new Database();
        bd.abrirConexion();

        String consulta = "SELECT * FROM usuarios WHERE id=" + "'" + idCuenta + "'";
        ResultSet rsConsulta = bd.ejecutarConsulta(consulta);
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

                return handlerInput.getResponseBuilder()
                        .withSpeech(speech)
                        .withReprompt(speech)
                        .withSimpleCard("Solicitud", speech)
                        .addElicitSlotDirective("claveSeguridad", intentRequest.getIntent())
                        .build();
            }

            else{
                String key = h.getHash(claveSeguridad);
                //key = claveSeguridad;
                consulta = "SELECT COUNT(*) AS total FROM usuarios WHERE id=" + "'" + idCuenta + "'";
                consulta += " AND clave=" + "'" + key + "'";
                rsConsulta = bd.ejecutarConsulta(consulta);
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


            }

        }

        String insercion = "UPDATE perfiles SET telefono=?";
        insercion += " WHERE cuenta=" + "'" + idCuenta + "'" + " AND DNI=" + "'" + DNI + "'";
        insercion += " AND alias=" + "'" + alias + "'";

        try{
            PreparedStatement st = bd.ejecutarInsercion(insercion);
            st.setString(1, telefono);
            st.execute();
            st.close();

        } catch (SQLException ex){
            ex.printStackTrace();
        }


        bd.cerrarConexion();

        speechText = "¡Perfil de " + alias + " configurado con éxito!";

        return handlerInput.getResponseBuilder()
                .withSpeech(speechText)
                .withReprompt(speechText)
                .withSimpleCard("Resultado", speechText)
                .build();

    }

}
