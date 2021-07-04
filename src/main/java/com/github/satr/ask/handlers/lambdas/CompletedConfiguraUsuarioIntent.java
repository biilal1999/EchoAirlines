package com.github.satr.ask.handlers.lambdas;

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.*;
import com.github.satr.ask.handlers.Intents;
import com.github.satr.ask.handlers.Modelo.Database;
import com.github.satr.ask.handlers.Modelo.SHA256;
//import jdk.internal.org.objectweb.asm.Handle;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import java.util.Map;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class CompletedConfiguraUsuarioIntent implements IntentRequestHandler {

    public final SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
    public final SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");

    @Override
    public boolean canHandle(HandlerInput handlerInput , IntentRequest intentRequest) {
        return handlerInput.matches(intentName(Intents.CONFIGURA_USUARIO_INTENT))
                && intentRequest.getDialogState() == DialogState.COMPLETED;
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput, IntentRequest intentRequest){
        Map<String, Slot> slots = intentRequest.getIntent().getSlots();
        String tipoDato = slots.get("dato").getValue();
        //String nick = slots.get("nick").getValue();
        String idCuenta = handlerInput.getRequestEnvelope().getSession().getUser().getUserId();
        String claveSeguridad = slots.get("claveSeguridad").getValue();
        String valor = "";
        String valorAux = "";
        AttributesManager attributesManager = handlerInput.getAttributesManager();
        Map<String, Object> attributes = attributesManager.getSessionAttributes();
        SHA256 h = new SHA256();


        if (tipoDato.equals("clave")){
            valorAux = slots.get("clave").getValue();
            valor = h.getHash(valorAux);
        }

        /*else if (tipoDato.equals("nombre")){
            valor = slots.get("nombre").getValue();
        }

        else if (tipoDato.equals("telefono")){
            valor = slots.get("telefono").getValue();
        }*/

        else if (tipoDato.equals("tarjeta")){
            //Intent intent = intentRequest.getIntent();
            valor = slots.get("tarjeta").getValue();

            if (valor.length() < 13 || valor.length() > 16){
                String speechText = "Tarjeta de crédito inválida. Recuerde que debe tener entre 13 y 16 números. Pruebe de nuevo.";

                return handlerInput.getResponseBuilder()
                        .withSpeech(speechText)
                        .withReprompt(speechText)
                        .addElicitSlotDirective("tarjeta", intentRequest.getIntent())
                        .build();
            }
        }

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

        // Compruebo el nombre

        String actualizacion = "UPDATE usuarios SET " + tipoDato + " = ?";

        actualizacion += " WHERE id=" + "'" + idCuenta + "'";

        try{
            PreparedStatement st = bd.ejecutarInsercion(actualizacion);
            st.setString(1, valor);

            st.execute();
            st.close();

        } catch (SQLException ex){
            ex.printStackTrace();
        }

        bd.cerrarConexion();

        String speechText = "";

        if (tipoDato.equals("tarjeta") || tipoDato.equals("clave")){
            speechText = "Se ha configurado correctamente tu " + tipoDato;
        }

        else{
            speechText = "Se ha configurado correctamente tu " + tipoDato + " a " + valor;
        }

        return handlerInput.getResponseBuilder()
                .withSpeech(speechText)
                .withReprompt(speechText)
                .build();
    }

}
