package com.github.satr.ask.handlers.lambdas;

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.*;
import com.github.satr.ask.handlers.Intents;
import com.github.satr.ask.handlers.Modelo.Database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class StartedInProgressConfiguraPerfilIntent implements IntentRequestHandler {

    @Override
    public boolean canHandle(HandlerInput handlerInput , IntentRequest intentRequest) {
        Slot telefono = intentRequest.getIntent().getSlots().get("telefono");

        return handlerInput.matches(intentName(Intents.CONFIGURA_PERFIL_INTENT))
                && intentRequest.getDialogState() != DialogState.COMPLETED
                && telefono.getValue() == null;
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput, IntentRequest intentRequest) {
        Intent intent = intentRequest.getIntent();
        String alias = intent.getSlots().get("alias").getValue();
        String DNI = intent.getSlots().get("DNI").getValue();
        String idCuenta = handlerInput.getRequestEnvelope().getSession().getUser().getUserId();
        String dato = intent.getSlots().get("dato").getValue();
        String speechText = "";
        AttributesManager attributesManager = handlerInput.getAttributesManager();
        Map<String, Object> attributes = attributesManager.getSessionAttributes();

        if (alias != null){
            Database bd = new Database();
            bd.abrirConexion();

            int id = -1;
            String consulta = "SELECT * FROM perfiles WHERE cuenta=" + "'" + idCuenta + "'" + " " +
                    "AND alias=" + "'" + alias + "'";

            ResultSet rs = bd.ejecutarConsulta(consulta);

            try{
                while (rs.next()){
                    id = rs.getInt("id");
                }

                rs.close();

            } catch (SQLException ex){
                ex.printStackTrace();
            }

            if (id == -1){
                speechText = "No hay ningún perfil registrado en esta cuenta con el alias de " + alias;
                speechText += ". Pruebe de nuevo, por favor.";

                bd.cerrarConexion();

                return handlerInput.getResponseBuilder()
                        .withSpeech(speechText)
                        .withReprompt("Dime de nuevo el alias del perfil")
                        .addElicitSlotDirective("alias", intent)
                        .build();

            }

            else{

                if (DNI != null){
                    id = -1;
                    consulta = "SELECT * FROM perfiles WHERE cuenta=" + "'" + idCuenta + "'" + " " +
                            "AND alias=" + "'" + alias + "'" + " AND DNI=" + "'" + DNI + "'";

                    rs = bd.ejecutarConsulta(consulta);

                    try{
                        while (rs.next()){
                            id = rs.getInt("id");
                        }

                        rs.close();

                    } catch (SQLException ex){
                        ex.printStackTrace();
                    }

                    bd.cerrarConexion();

                    if (id == -1){
                        speechText = "El DNI es incorrecto. Pruebe de nuevo, por favor";

                        return handlerInput.getResponseBuilder()
                                .withSpeech(speechText)
                                .withReprompt("Pruebe de nuevo con el DNI")
                                .addElicitSlotDirective("DNI", intent)
                                .build();
                    }

                    else{

                        if (dato == null){
                            return handlerInput.getResponseBuilder()
                                    .addDelegateDirective(intent)
                                    .build();
                        }

                        else{
                            speechText = "Dime tu nuevo número de teléfono.";

                            return handlerInput.getResponseBuilder()
                                    .withSpeech(speechText)
                                    .addElicitSlotDirective("telefono", intent)
                                    .build();
                        }

                    }

                }

                else{

                    return handlerInput.getResponseBuilder()
                            .addDelegateDirective(intent)
                            .build();
                }

            }


        }

        return handlerInput.getResponseBuilder()
                .addDelegateDirective(intent)
                .build();

    }

}
