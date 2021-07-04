package com.github.satr.ask.handlers.lambdas;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.DialogState;
import com.amazon.ask.model.Intent;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import com.github.satr.ask.handlers.Intents;
import com.github.satr.ask.handlers.Modelo.Database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class StartedInProgressCreaUsuarioIntent implements IntentRequestHandler {

    @Override
    public boolean canHandle(HandlerInput handlerInput , IntentRequest intentRequest) {
        return handlerInput.matches(intentName(Intents.CREA_USUARIO_INTENT))
                && intentRequest.getDialogState() != DialogState.COMPLETED;

    }


    @Override
    public Optional<Response> handle(HandlerInput handlerInput, IntentRequest intentRequest) {
        Intent intent = intentRequest.getIntent();
        String nombre = intent.getSlots().get("nombre").getValue();
        String clave = intent.getSlots().get("clave").getValue();
        String tarjeta = intent.getSlots().get("tarjeta").getValue();
        String telefono = intent.getSlots().get("telefono").getValue();

        if (nombre != null && clave == null){
            Database bd = new Database();
            bd.abrirConexion();

            int num = 0;
            String consulta = "SELECT COUNT(*) AS total FROM users WHERE nombre="+ "'" + nombre + "'";
            ResultSet result = bd.ejecutarConsulta(consulta);

            try{
                while (result.next()){
                    num = result.getInt("total");
                }

            } catch (SQLException ex){
                ex.printStackTrace();
            }

            bd.cerrarConexion();

            if (num > 0){
                String speechText = "Ya hay un usuario llamado " + nombre + ". Dime otro nombre de usuario para crear tu perfil.";

                return handlerInput.getResponseBuilder()
                        .withSpeech(speechText)
                        .withReprompt(speechText)
                        .addElicitSlotDirective("nombre", intent)
                        .build();
            }
        }

        else if (tarjeta != null && telefono == null){
            //int numTarjeta = Integer.parseInt(tarjeta);

            String regex = "^(?:(?<visa>4[0-9]{12}(?:[0-9]{3})?)|" +
                    "(?<mastercard>5[1-5][0-9]{14})|" +
                    "(?<discover>6(?:011|5[0-9]{2})[0-9]{12})|" +
                    "(?<amex>3[47][0-9]{13})|" +
                    "(?<diners>3(?:0[0-5]|[68][0-9])?[0-9]{11})|" +
                    "(?<jcb>(?:2131|1800|35[0-9]{3})[0-9]{11}))$";

            //if (!tarjeta.matches(regex)){
            if (tarjeta.length() < 13 || tarjeta.length() > 16){
                String speechText = "Tarjeta de crédito inválida. Recuerde que debe tener entre 13 y 16 números. Pruebe de nuevo.";

                return handlerInput.getResponseBuilder()
                        .withSpeech(speechText)
                        .withReprompt(speechText)
                        .addElicitSlotDirective("tarjeta", intent)
                        .build();
            }
        }


        return handlerInput.getResponseBuilder()
                .addDelegateDirective(intentRequest.getIntent())
                .build();
    }

}
