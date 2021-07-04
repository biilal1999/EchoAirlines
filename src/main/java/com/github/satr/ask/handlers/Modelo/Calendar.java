package com.github.satr.ask.handlers.Modelo;

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.services.ServiceException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;

public class Calendar {
    public final SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");

    public Calendar() {
    }

    public Object obtenerEventos(HandlerInput handlerInput, String fecha){
        ArrayList<String> resultado = new ArrayList<>();
        AttributesManager attributesManager = handlerInput.getAttributesManager();
        Map<String, Object> attributes = attributesManager.getSessionAttributes();
        String atkn = handlerInput.getRequestEnvelope().getContext().getSystem().getUser().getAccessToken();

        if (atkn == null){
            return handlerInput.getResponseBuilder()
                    .withSpeech("Debes vincular con tu cuenta de Google para continuar")
                    .withLinkAccountCard()
                    .build();
        }

        String enlace = "https://www.googleapis.com/calendar/v3/users/me/calendarList";
        URL url = null;
        HttpURLConnection conn = null;
        String iden = "";

        try {
            url = new URL(enlace);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + atkn);
            conn.setRequestProperty("Content-Type", "application/json");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();

            JSONObject obj = new JSONObject(response.toString());

            JSONArray array = obj.getJSONArray("items");
            iden = array.getJSONObject(0).getString("id");
            attributes.put("veri", iden);
            attributesManager.setSessionAttributes(attributes);

        } catch (ServiceException e){
            if (e.getStatusCode() == 403){
                return handlerInput.getResponseBuilder()
                        .withSpeech("Vuelve a vincular con tu cuenta de Google y concede permisos a todo lo que se sollicite")
                        .withLinkAccountCard()
                        .build();
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            return handlerInput.getResponseBuilder()
                    .withSpeech("Vuelve a vincular con tu cuenta de Google y concede permisos a todo lo que se sollicite")
                    .withLinkAccountCard()
                    .build();
        }

        String link = "https://www.googleapis.com/calendar/v3/calendars/" + iden + "/events";

        try{
            url = new URL(link);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + atkn);
            conn.setRequestProperty("Content-Type", "application/json");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null){
                response.append(inputLine);
            }

            in.close();

            JSONObject obj = new JSONObject(response.toString());
            JSONArray array = obj.getJSONArray("items");

            for (int i = 0; i < array.length(); i++){
                String fechaInicio = array.getJSONObject(i).getJSONObject("start").getString("dateTime");
                String fechaFin = array.getJSONObject(i).getJSONObject("end").getString("dateTime");
                //System.out.println("aqui entro seguro");

                if (fechaInicio.contains(fecha)){
                    String tarea = array.getJSONObject(i).getString("summary");
                    resultado.add(tarea);
                    //System.out.println("entro al inicio");
                }

                else if (fechaFin.contains(fecha)){
                    String tarea = array.getJSONObject(i).getString("summary");
                    resultado.add(tarea);
                    //System.out.println("entro al final");
                }
            }

        } catch (ServiceException e){
            if (e.getStatusCode() == 403){
                return handlerInput.getResponseBuilder()
                        .withSpeech("Vuelve a vincular con tu cuenta de Google y concede permisos a todo lo que se sollicite")
                        .withLinkAccountCard()
                        .build();
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            return handlerInput.getResponseBuilder()
                    .withSpeech("Vuelve a vincular con tu cuenta de Google y concede permisos a todo lo que se sollicite")
                    .withLinkAccountCard()
                    .build();
        }

        return resultado;

    }

}
