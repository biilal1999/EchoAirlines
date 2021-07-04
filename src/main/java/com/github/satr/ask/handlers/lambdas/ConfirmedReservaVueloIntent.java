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

public class ConfirmedReservaVueloIntent implements IntentRequestHandler {

    public final SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
    public final SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
    public final SimpleDateFormat sdfMonth = new SimpleDateFormat("yyyy-MM");

    @Override
    public boolean canHandle(HandlerInput handlerInput , IntentRequest intentRequest) {
        Slot accion = intentRequest.getIntent().getSlots().get("accion");

        return (handlerInput.matches(intentName(Intents.RESERVA_VUELO_INTENT))
                && accion.getValue() != null
                && (accion.getConfirmationStatus() == SlotConfirmationStatus.CONFIRMED
                    || intentRequest.getIntent().getConfirmationStatus() == IntentConfirmationStatus.CONFIRMED));
    }


    @Override
    public Optional<Response> handle(HandlerInput handlerInput, IntentRequest intentRequest) {
        System.out.println("heeey");
        Intent intent = intentRequest.getIntent();
        String origen = intent.getSlots().get("origen").getValue();
        String destino = intent.getSlots().get("destino").getValue();
        String mes = intent.getSlots().get("mes").getValue();
        String fecha = intent.getSlots().get("fecha").getValue();
        String precioMaximo = intent.getSlots().get("precioMaximo").getValue();
        String salida = intent.getSlots().get("salida").getValue();
        String accion = intent.getSlots().get("accion").getValue();
        String alias = intent.getSlots().get("alias").getValue();
        String idCuenta = handlerInput.getRequestEnvelope().getSession().getUser().getUserId();
        AttributesManager attributesManager = handlerInput.getAttributesManager();
        Map<String, Object> attributes = attributesManager.getSessionAttributes();

        String speechText = "";

        if (alias == null){
            speechText = "Para continuar con la reserva, dime el alias de la persona que va a viajar.";

            return handlerInput.getResponseBuilder()
                    .withSpeech(speechText)
                    .withReprompt(speechText)
                    .addElicitSlotDirective("alias", intent)
                    .build();
        }

        Database bd = new Database();
        bd.abrirConexion();

        int idPerfil = -1;
        int idenVuelo = Integer.parseInt(accion);
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

        if (idPerfil == -1){
            speechText = "No tienes ningún perfil registrado como " + alias +
                    ". Dígame de nuevo un alias de perfil ya " +
                    "registrado, si desea completar la reserva. También puede crear un perfil nuevo " +
                    "con este alias";

            return handlerInput.getResponseBuilder()
                    .withSpeech(speechText)
                    .withReprompt(speechText)
                    .addElicitSlotDirective("alias", intent)
                    .build();
        }

        else{
            String consulta = "SELECT COUNT(*) AS total FROM reservas WHERE vuelo_id=" + idenVuelo;
            consulta += " AND titular_id=" + idTitular + " AND perfil_id=" + idPerfil;
            ResultSet rsRes = bd.ejecutarConsulta(consulta);
            int num = 0;

            try{
                while (rsRes.next()){
                    num = rsRes.getInt("total");
                }

                rsRes.close();

            } catch (SQLException ex){
                ex.printStackTrace();
            }

            if (num > 0){
                speechText = "Ya hay una reserva para este perfil de usuario con este vuelo. Pruebe con otro alias si lo desea.";

                return handlerInput.getResponseBuilder()
                        .withSpeech(speechText)
                        .withReprompt(speechText)
                        .addElicitSlotDirective("alias", intent)
                        .build();
            }

        }

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

            Email e = new Email(direccion, "CONFIRMACION");

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
                    " confirmar la reserva, por favor.";

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

        // Confirmada la reserva

        double pReserva = 0.0;
        int billetesVuelo = 0;
        Date today = new Date();
        String hoy = sdfDate.format(today);

        ResultSet rsRes = bd.ejecutarConsulta("SELECT * FROM vuelos WHERE id=" + idenVuelo);

        try{
            while (rsRes.next()){
                pReserva = rsRes.getFloat("precio");
                billetesVuelo = rsRes.getInt("billetes");
            }

        } catch (SQLException ex){
            ex.printStackTrace();
        }

        String insercion = "INSERT INTO reservas (titular_id, vuelo_id, perfil_id, fecha_reserva, precio) VALUES (?,?,?,?,?)";

        try{
            PreparedStatement st = bd.ejecutarInsercion(insercion);
            st.setInt(1, idTitular);
            st.setInt(2, idenVuelo);
            st.setInt(3, idPerfil);
            st.setString(4, hoy);
            st.setFloat(5, (float) pReserva);

            st.execute();
            st.close();

        } catch (SQLException ex){
            ex.printStackTrace();
        }


        // Insertada la reserva la reserva

        int nuevoBilletes = billetesVuelo - 1;

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

        speechText = "Has completado la reserva del vuelo de " + origen + " a " + destino +
                " del " + fecha + " a las " + salida + " para " + alias;

        speechText += ". Muchas gracias.";

        if (attributes.containsKey("codigo")){
            attributes.remove("codigo");
            attributesManager.setSessionAttributes(attributes);
        }

        bd.cerrarConexion();

        return handlerInput.getResponseBuilder()
                .withSpeech(speechText)
                .withReprompt(speechText)
                .build();

    }

}
