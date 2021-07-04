package com.github.satr.ask.handlers.lambdas;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
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

public class StartedInProgressConfiguraUsuarioIntent implements IntentRequestHandler {

    public final SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
    public final SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");

    @Override
    public boolean canHandle(HandlerInput handlerInput , IntentRequest intentRequest) {
        return handlerInput.matches(intentName(Intents.CONFIGURA_USUARIO_INTENT))
                && intentRequest.getDialogState() != DialogState.COMPLETED;
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput, IntentRequest intentRequest) {
        Intent intent = intentRequest.getIntent();
        //String nick = intent.getSlots().get("nick").getValue();
        //String dato = intent.getSlots().get("dato").getValue();
        //String idCuenta = handlerInput.getRequestEnvelope().getSession().getUser().getUserId();

        /*if (nick != null){
            Database bd = new Database();
            bd.abrirConexion();

            String consulta = "SELECT COUNT(*) AS total FROM perfiles WHERE nombre=" + "'" + nick + "'";
            consulta += " AND cuenta=" + "'" + idCuenta + "'";

            int num = 0;
            ResultSet rs = bd.ejecutarConsulta(consulta);

            try{
                while (rs.next()){
                    num = rs.getInt("total");
                }

            } catch (SQLException ex){
                ex.printStackTrace();
            }

            bd.cerrarConexion();

            if (num == 0){
                String speechText = "No tienes ningún perfil registrado llamado " + nick + ". Prueba de nuevo.";

                return handlerInput.getResponseBuilder()
                        .withSpeech(speechText)
                        .withReprompt(speechText)
                        .addElicitSlotDirective("nick", intent)
                        .build();
            }

        }*/

        return handlerInput.getResponseBuilder()
                .addDelegateDirective(intent)
                .build();

    }

}