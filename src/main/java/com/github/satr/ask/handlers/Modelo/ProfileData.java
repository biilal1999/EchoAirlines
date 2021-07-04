package com.github.satr.ask.handlers.Modelo;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.services.ServiceException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ProfileData {

    private String ubicacion;

    public ProfileData() {
        ubicacion = null;
    }

    public String obtenerUbicacion(HandlerInput handlerInput){
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

            return ciudadDefecto;

        } catch (ServiceException e){
            return null;

        } catch (MalformedURLException e) {
            e.printStackTrace();

        } catch (IOException e) {
            return null;
        }

        return ciudadDefecto;
    }
}
