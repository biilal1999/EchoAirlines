package com.github.satr.ask.handlers.lambdas;

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.dispatcher.request.handler.impl.PermissionChangedRequestHandler;
import com.amazon.ask.model.*;
import com.amazon.ask.model.services.ServiceException;
import com.amazon.ask.model.services.deviceAddress.ShortAddress;
import com.amazon.ask.model.services.ups.Error;
import com.amazon.ask.model.services.ups.UpsServiceClient;
import com.amazon.ask.request.Predicates;
//import com.amazonaws.samples.EmailSD;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.github.satr.ask.handlers.Intents;
import com.github.satr.ask.handlers.Modelo.*;
import com.google.api.client.googleapis.auth.oauth2.OAuth2Utils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
//mport org.json.JSONObject;

import static com.amazon.ask.request.Predicates.intentName;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
//import org.apache.http.client.fluent.Content;


public class CustomLaunchRequestHandler implements RequestHandler {

    @Override
    public boolean canHandle(HandlerInput handlerInput) {
        return handlerInput.matches(Predicates.requestType(LaunchRequest.class));
    }


    @Override
    public Optional<Response> handle(HandlerInput handlerInput) {
        String idCuenta = handlerInput.getRequestEnvelope().getSession().getUser().getUserId();
        Database bd = new Database();

        bd.abrirConexion();

        ResultSet rs = bd.ejecutarConsulta("SELECT COUNT(*) AS total FROM usuarios WHERE id=" + "'" + idCuenta + "'");
        int num = 0;

        try {
            while (rs.next()) {
                num = rs.getInt("total");
            }

        } catch (SQLException e){
            e.printStackTrace();
        }

        String speechText = "";

        if (num == 0){
            speechText = "Bienvenido, vamos a empezar a configurar su usuario para esta skill.";

            Intent updatedIntent = Intent.builder()
                    .withName(Intents.CREA_USUARIO_INTENT)
                    .build();

            handlerInput.getResponseBuilder()
                    .withSpeech(speechText)
                    .withReprompt(speechText)
                    .addDelegateDirective(updatedIntent)
                    .build();

        }

        else{

            speechText = "Hola. ¿Qué desea realizar?";

            AttributesManager attributesManager = handlerInput.getAttributesManager();
            Map <String, Object> attributes = attributesManager.getSessionAttributes();

            if (!attributes.containsKey("idTitularCuenta")){
                int idTitular = -1;
                ResultSet rsTitular = bd.ejecutarConsulta("SELECT * FROM perfiles WHERE cuenta=" + "'" + idCuenta + "'" +
                        " AND titular=1");

                try{
                    while (rsTitular.next()){
                        idTitular = rsTitular.getInt("id");
                    }

                    rsTitular.close();

                } catch (SQLException ex){
                    ex.printStackTrace();
                }

                attributes.put("idTitularCuenta", idTitular);
                attributesManager.setSessionAttributes(attributes);

                UpsServiceClient usc = handlerInput.getServiceClientFactory().getUpsService();
                String email = "";

                try{
                    email = usc.getProfileEmail();

                } catch (ServiceException e){
                    if (e.getStatusCode() == 403){
                        String p = "alexa::profile:email:read";
                        List<String> permisos = new ArrayList<String>();
                        permisos.add(p);

                        return handlerInput.getResponseBuilder()
                                .withSpeech("Debes dar permiso de acceso a tu email")
                                .withAskForPermissionsConsentCard(permisos)
                                .build();
                    }
                }


                attributes.put("correo", email);
                attributesManager.setSessionAttributes(attributes);

                /*String idDispositivo = handlerInput.getRequestEnvelope().getContext().getSystem().getDevice().getDeviceId();
                String apiAccessToken = handlerInput.getRequestEnvelope().getContext().getSystem().getApiAccessToken();

                String enlace = "https://api.eu.amazonalexa.com/v1/devices/" + idDispositivo + "/settings/address";
                URL url = null;
                HttpURLConnection conn = null;
                String ciudadDefecto = "";

                try {
                    url = new URL(enlace);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Authorization", "Bearer " + apiAccessToken);
                    conn.setRequestProperty("Content-Type", "application/json");

                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }

                    in.close();

                    JSONObject myResponse = new JSONObject(response.toString());
                    ciudadDefecto = myResponse.getString("city");

                    attributes.put("ciudad", ciudadDefecto);
                    attributesManager.setSessionAttributes(attributes);

                    //System.out.println(response.toString());

                } catch (ServiceException e){
                    if (e.getStatusCode() == 403){
                        String p = "read::alexa:device:all:address";
                        List<String> permisos = new ArrayList<String>();
                        permisos.add(p);

                        return handlerInput.getResponseBuilder()
                                .withSpeech("Debes conceder permiso para acceder a tu ubicación")
                                .withAskForPermissionsConsentCard(permisos)
                                .build();
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    String p = "read::alexa:device:all:address";
                    List<String> permisos = new ArrayList<String>();
                    permisos.add(p);

                    return handlerInput.getResponseBuilder()
                            .withSpeech("Debes conceder permiso para acceder a tu ubicación")
                            .withAskForPermissionsConsentCard(permisos)
                            .build();
                }*/


                ProfileData pd = new ProfileData();
                String ciudadDefecto = pd.obtenerUbicacion(handlerInput);

                if (ciudadDefecto == null){
                    String p = "read::alexa:device:all:address";
                    List<String> permisos = new ArrayList<String>();
                    permisos.add(p);

                    return handlerInput.getResponseBuilder()
                            .withSpeech("Debes conceder permiso para acceder a tu ubicación")
                            .withAskForPermissionsConsentCard(permisos)
                            .build();
                }

                attributes.put("ciudad", ciudadDefecto);
                attributesManager.setSessionAttributes(attributes);


                String atkn = handlerInput.getRequestEnvelope().getContext().getSystem().getUser().getAccessToken();

                if (atkn == null){
                    return handlerInput.getResponseBuilder()
                            .withSpeech("Inicia sesión desde la aplicación de Amazon Alexa en tu cuenta de Google para poder continuar.")
                            .withLinkAccountCard()
                            .build();
                }


                else{
                    attributes.put("secreto", atkn);
                    attributesManager.setSessionAttributes(attributes);
                }

                /*Calendar c = new Calendar();
                ArrayList<String> res = c.obtenerEventos(handlerInput, "2021-05-15");
                int tam = res.size();

                attributes.put("tam", tam);
                attributesManager.setSessionAttributes(attributes);*/

            }

        }

        // PARA OBTENER PERMISOS, NO SE PUEDE HACER DE ESA FORMA PORQUE LOS TIENE INCOMPLETOS, INCONSISTENTES
        // Y FALTAN EN LOS SCOPES

        // HAY QUE HACER SOLICITUDES Y VER SI DA ERROR 403

        /*Permissions perm = handlerInput.getRequestEnvelope().getSession().getUser().getPermissions();
        System.out.println(perm.getScopes().toString());*/

        /*String p = "alexa::profile:email:read";
        List<String> permisos = new ArrayList<String>();
        permisos.add(p);*/

        bd.cerrarConexion();

        return handlerInput.getResponseBuilder()
                .withSpeech(speechText)
                .withSimpleCard("Hola caracola", speechText)
                .withReprompt(speechText)
                //.withAskForPermissionsConsentCard(permisos)
                .build();
    }

}
