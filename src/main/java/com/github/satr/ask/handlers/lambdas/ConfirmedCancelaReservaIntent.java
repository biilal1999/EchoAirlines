package com.github.satr.ask.handlers.lambdas;

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.*;
import com.amazon.ask.model.services.ServiceException;
import com.amazon.ask.model.services.ups.UpsServiceClient;
import com.github.satr.ask.handlers.Intents;
import com.github.satr.ask.handlers.Modelo.Database;
import com.github.satr.ask.handlers.Modelo.Email;
import com.github.satr.ask.handlers.Modelo.SHA256;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.amazon.ask.request.Predicates.intentName;

public class ConfirmedCancelaReservaIntent implements IntentRequestHandler {

    public final SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
    public final SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");

    @Override
    public boolean canHandle(HandlerInput handlerInput , IntentRequest intentRequest) {
        Slot accion = intentRequest.getIntent().getSlots().get("accion");

        return (handlerInput.matches(intentName(Intents.CANCELA_RESERVA_INTENT))
                && accion.getValue() != null
                && accion.getConfirmationStatus() == SlotConfirmationStatus.CONFIRMED);
    }


    @Override
    public Optional<Response> handle(HandlerInput handlerInput, IntentRequest intentRequest) {
        Intent intent = intentRequest.getIntent();
        String origen = intent.getSlots().get("origen").getValue();
        String destino = intent.getSlots().get("destino").getValue();
        String fecha = intent.getSlots().get("fecha").getValue();
        String accion = intent.getSlots().get("accion").getValue();
        String salida = intent.getSlots().get("salida").getValue();
        String alias = intent.getSlots().get("alias").getValue();
        String idCuenta = handlerInput.getRequestEnvelope().getSession().getUser().getUserId();
        AttributesManager attributesManager = handlerInput.getAttributesManager();
        Map<String, Object> attributes = attributesManager.getSessionAttributes();

        String speechText = "";

        Database bd = new Database();
        bd.abrirConexion();

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

        String claveSeguridad = intent.getSlots().get("claveSeguridad").getValue();

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


        // Ahora a confirmar el token enviado al correo

        /*if (!attributes.containsKey("codigo")){
            // Aquí iba lo de abajo (lo del token)
        }*/


        String token = intent.getSlots().get("token").getValue();

        if (token == null){
            String resultadoToken = "";

            for (int i = 0; i < 4; i++){
                Random rnd = new Random();
                resultadoToken += String.valueOf(rnd.nextInt(9) + 1);
            }

            //String direccion = attributes.get("correo").toString();

            UpsServiceClient usc = handlerInput.getServiceClientFactory().getUpsService();
            String direccion = "";

            try{
                direccion = usc.getProfileEmail();

            } catch (ServiceException e){
                if (e.getStatusCode() == 403){
                    String p = "alexa::profile:email:read";
                    List<String> permisos = new ArrayList<String>();
                    permisos.add(p);

                    return handlerInput.getResponseBuilder()
                            .withSpeech("Debes dar permiso a tu email")
                            .withAskForPermissionsConsentCard(permisos)
                            .build();
                }
            }

            Email e = new Email(direccion, "CANCELACION");

            try{
                e.enviarConfirmacionReserva(resultadoToken);

            } catch (Exception ex){
                ex.printStackTrace();
            }

            /*try{
                String actualizacion = "UPDATE reservas SET token=? WHERE titular_id=" + idTitular + " " +
                        "AND vuelo_id=" + idenVuelo + " AND perfil_id=" + idPerfil;

                PreparedStatement st = bd.ejecutarInsercion(actualizacion);
                st.setString(1, resultadoToken);
                st.execute();
                st.close();

            } catch (SQLException ex){
                ex.printStackTrace();
            }*/

            if (attributes.containsKey("codigo")){
                attributes.remove("codigo");
            }

            attributes.put("codigo", resultadoToken);
            attributesManager.setSessionAttributes(attributes);

            String speech = "Hemos enviado un código de confirmación a su correo electrónico. Dígame el código enviado para" +
                    " cancelar la reserva, por favor.";

            return handlerInput.getResponseBuilder()
                    .withSpeech(speech)
                    .withReprompt(speech)
                    .withSimpleCard("Solicitud", speech)
                    .addElicitSlotDirective("token", intentRequest.getIntent())
                    .build();
        }


        else{

            if (!token.equals(attributes.get("codigo").toString())){
                if (!attributes.containsKey("confirmaciones")){
                    int confirmaciones = 1;
                    attributes.put("confirmaciones", confirmaciones);
                    attributesManager.setSessionAttributes(attributes);
                }

                else{

                    if (Integer.parseInt(attributes.get("confirmaciones").toString()) < 3){
                        int confirmaciones = Integer.parseInt(attributes.get("confirmaciones").toString()) + 1;
                        attributes.remove("confirmaciones");
                        attributes.put("confirmaciones", confirmaciones);
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

                String speech = "Código de confirmación incorrecto. Inténtelo de nuevo, por favor.";

                return handlerInput.getResponseBuilder()
                        .withSpeech(speech)
                        .withReprompt(speech)
                        .withSimpleCard("Solicitud", speech)
                        .addElicitSlotDirective("token", intentRequest.getIntent())
                        .build();
            }

            // Si coincide

            if (attributes.containsKey("confirmaciones")){
                attributes.remove("confirmaciones");
                attributesManager.setSessionAttributes(attributes);
            }

            /*String reseteo = "UPDATE reservas SET token=0 WHERE perfil_id=" + idPerfil + " AND " +
                    "titular_id=" + idTitular + " AND vuelo_id=" + idenVuelo + " AND token=?";

            try{
                PreparedStatement st = bd.ejecutarInsercion(reseteo);
                st.setString(1, attributes.get("codigo").toString());
                st.execute();
                st.close();

            } catch (SQLException ex){
                ex.printStackTrace();
            }*/


        }

        int idenVuelo = Integer.parseInt(accion);

        String borrado = "DELETE FROM reservas WHERE titular_id=? AND vuelo_id=? AND perfil_id=?";

        try{
            PreparedStatement st = bd.ejecutarInsercion(borrado);
            st.setInt(1, idTitular);
            st.setInt(2, idenVuelo);
            st.setInt(3, idPerfil);

            st.execute();
            st.close();

        } catch (SQLException ex){
            ex.printStackTrace();
        }

        String consulta = "SELECT * FROM vuelos WHERE id=" + idenVuelo;
        ResultSet rsVuelo = bd.ejecutarConsulta(consulta);
        int billetesVuelo = 0;

        try{
            while (rsVuelo.next()){
                billetesVuelo = rsVuelo.getInt("billetes");
            }

        } catch (SQLException ex){
            ex.printStackTrace();
        }

        int nuevoBilletes = billetesVuelo + 1;

        String actualizacion = "UPDATE vuelos SET billetes=? WHERE id=?";

        try{
            PreparedStatement st = bd.ejecutarInsercion(actualizacion);
            st.setInt(1, nuevoBilletes);
            st.setInt(2, idenVuelo);

            st.execute();
            st.close();

        } catch (SQLException ex){
            ex.printStackTrace();
        }

        /*String cadenaDNI = DNI.charAt(0) +  " " + DNI.charAt(1) + " " + DNI.charAt(2) + " " + DNI.charAt(3) + " " +
                DNI.charAt(4) + " " + DNI.charAt(5) + " " + DNI.charAt(6) + DNI.charAt(7);

        speechText = nombre + ", has cancelado la reserva del vuelo de " + origen + " a " + destino +
                " para el " + fecha + " a las " + salida + " para el DNI " + cadenaDNI;*/

        speechText = "Has cancelado la reserva del vuelo de " + origen + " a " + destino +
                " para el " + fecha + " a las " + salida + " para el alias " + alias;

        bd.cerrarConexion();

        return handlerInput.getResponseBuilder()
                .withSpeech(speechText)
                .withReprompt(speechText)
                .build();

    }

}
