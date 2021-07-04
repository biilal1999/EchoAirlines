package com.github.satr.ask.handlers.lambdas;

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.Intent;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.Slot;
import com.amazon.ask.model.services.ServiceException;
import com.github.satr.ask.handlers.Intents;
import com.github.satr.ask.handlers.Modelo.Database;
import org.json.JSONObject;

import javax.xml.crypto.Data;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class OrigenDefectoConsultaVueloIntent implements IntentRequestHandler {

    @Override
    public boolean canHandle(HandlerInput handlerInput , IntentRequest intentRequest) {
        Slot origen = intentRequest.getIntent().getSlots().get("origen");

        return handlerInput.matches(intentName(Intents.CONSULTA_VUELO_INTENT))
                && origen.getValue() == null;
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput, IntentRequest intentRequest) {
        Intent intent = intentRequest.getIntent();
        String idCuenta = handlerInput.getRequestEnvelope().getSession().getUser().getUserId();

        AttributesManager attributesManager = handlerInput.getAttributesManager();
        Map <String, Object> attributes = attributesManager.getSessionAttributes();


        /*Database bd = new Database();
        bd.abrirConexion();

        AttributesManager attributesManager = handlerInput.getAttributesManager();
        Map<String, Object> attributes = attributesManager.getSessionAttributes();

        ResultSet rsCiudad = bd.ejecutarConsulta("SELECT * from usuarios WHERE id=" + "'" + idCuenta + "'");
        String ciudadDefecto = "";

        try{
            while(rsCiudad.next()){
                ciudadDefecto = rsCiudad.getString("ciudad");
            }

            rsCiudad.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        bd.cerrarConexion();*/

        String idDispositivo = handlerInput.getRequestEnvelope().getContext().getSystem().getDevice().getDeviceId();
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
        }

        String speechText = "No has especificado ningún origen. Tomaré " + ciudadDefecto +
                " como punto de partida. ¿De acuerdo?";

        ciudadDefecto = ciudadDefecto.toLowerCase();

        Slot updateSlot = Slot.builder()
                .withName("origen")
                .withValue(ciudadDefecto)
                .build();

        intent.getSlots().put("origen", updateSlot);

        return handlerInput.getResponseBuilder()
                .withSpeech(speechText)
                .withReprompt(speechText)
                .addConfirmSlotDirective("origen", intent)
                .build();
    }

}
