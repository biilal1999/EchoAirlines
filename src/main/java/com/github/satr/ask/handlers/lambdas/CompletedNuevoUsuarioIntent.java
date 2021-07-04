package com.github.satr.ask.handlers.lambdas;

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.*;
import com.github.satr.ask.handlers.Intents;
import com.github.satr.ask.handlers.Modelo.Database;
import com.github.satr.ask.handlers.Modelo.SHA256;
import software.aws.rds.jdbc.shading.com.mysql.cj.x.protobuf.MysqlxPrepare;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class CompletedNuevoUsuarioIntent implements IntentRequestHandler {

    @Override
    public boolean canHandle(HandlerInput handlerInput, IntentRequest intentRequest) {
        //Slot ciudad = intentRequest.getIntent().getSlots().get("ciudad");
        Slot confirmacion = intentRequest.getIntent().getSlots().get("confirmacion");

        return (handlerInput.matches(intentName(Intents.CREA_USUARIO_INTENT))
                && confirmacion.getValue() != null);
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput, IntentRequest intentRequest) {
        Intent intent = intentRequest.getIntent();
        Map<String, Slot> slots = intent.getSlots();

        String clave = slots.get("clave").getValue();
        String pais = slots.get("pais").getValue();
        String ciudad = slots.get("ciudad").getValue();
        String tarjeta = slots.get("tarjeta").getValue();

        String idCuenta = handlerInput.getRequestEnvelope().getSession().getUser().getUserId();

        Database bd = new Database();
        bd.abrirConexion();

        if (clave == null){
            try{
                PreparedStatement st = bd.ejecutarInsercion("INSERT INTO usuarios (id, tarjeta, pais, ciudad) " +
                        "VALUES (?,?,?,?)");
                st.setString(1, idCuenta);
                st.setString(2, tarjeta);
                st.setString(3, pais);
                st.setString(4, ciudad);
                st.execute();

            } catch (SQLException ex){
                ex.printStackTrace();
            }
        }

        else{
            SHA256 h = new SHA256();
            String key = h.getHash(clave);

            try{
                PreparedStatement st = bd.ejecutarInsercion("INSERT INTO usuarios (id, tarjeta, pais, ciudad, clave) " +
                        "VALUES (?,?,?,?,?)");
                st.setString(1, idCuenta);
                st.setString(2, tarjeta);
                st.setString(3, pais);
                st.setString(4, ciudad);
                st.setString(5, key);
                st.execute();

            } catch (SQLException ex){
                ex.printStackTrace();
            }
        }

        bd.cerrarConexion();

        String speechText = "¡Genial! Ahora vamos a crear el primer perfil de cliente para esta skill.";

        Intent updateIntent = Intent.builder()
                .withName(Intents.CREA_PERFIL_INTENT)
                .build();

        return handlerInput.getResponseBuilder()
                .withSpeech(speechText)
                .addDelegateDirective(updateIntent)
                .build();
    }

}
