package com.github.satr.ask.handlers.lambdas;

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.*;
import com.github.satr.ask.handlers.Intents;
import com.github.satr.ask.handlers.Modelo.Database;
import com.github.satr.ask.handlers.Modelo.SHA256;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class ProcessCreaPerfilIntent implements IntentRequestHandler {

    public final SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
    public final SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");

    @Override
    public boolean canHandle(HandlerInput handlerInput , IntentRequest intentRequest) {
        return handlerInput.matches(intentName(Intents.CREA_PERFIL_INTENT));
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput, IntentRequest intentRequest){
        Intent intent = intentRequest.getIntent();
        String nombre = intent.getSlots().get("nombre").getValue();
        String DNI = intent.getSlots().get("DNI").getValue();
        String alias = intent.getSlots().get("alias").getValue();
        String telefono = intent.getSlots().get("telefono").getValue();
        String fechaNacimiento = intent.getSlots().get("fechaNacimiento").getValue();
        String claveSeguridad = intent.getSlots().get("claveSeguridad").getValue();
        String speechText = "";
        String idCuenta = handlerInput.getRequestEnvelope().getSession().getUser().getUserId();
        AttributesManager attributesManager = handlerInput.getAttributesManager();
        Map<String, Object> attributes = attributesManager.getSessionAttributes();
        SHA256 h = new SHA256();

        if (DNI != null && alias == null){
            if (DNI.length() != 8){
                speechText = "DNI invalido. Recuerde que debe tener 8 letras. Pruebe de nuevo.";

                return handlerInput.getResponseBuilder()
                        .withSpeech(speechText)
                        .withReprompt(speechText)
                        .addElicitSlotDirective("DNI", intent)
                        .build();
            }
        }

        else if (alias != null && telefono == null){
            Database bd = new Database();
            bd.abrirConexion();

            ResultSet auxAlias = bd.ejecutarConsulta("SELECT COUNT(*) AS total FROM perfiles " +
                    "WHERE cuenta=" + "'" + idCuenta + "'" + " AND alias=" + "'" + alias + "'");

            int num = 0;

            try{
                while (auxAlias.next()){
                    num = auxAlias.getInt("total");
                }

                auxAlias.close();

            } catch (SQLException ex){
                ex.printStackTrace();
            }

            if (num > 0){
                speechText = "Ya existe este alias para este perfil en tu Alexa. Pruebe otro, por favor.";

                return handlerInput.getResponseBuilder()
                        .withSpeech(speechText)
                        .withReprompt(speechText)
                        .addElicitSlotDirective("alias", intent)
                        .build();
            }
        }

        else if (nombre != null && alias != null && DNI != null && telefono != null && fechaNacimiento != null){
            Database bd = new Database();
            bd.abrirConexion();

            String consulta = "SELECT * FROM usuarios WHERE id=" + "'" + idCuenta + "'";
            ResultSet rsConsulta = bd.ejecutarConsulta(consulta);
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
                    String key = h.getHash(claveSeguridad);
                    //key = claveSeguridad;
                    consulta = "SELECT COUNT(*) AS total FROM usuarios WHERE id=" + "'" + idCuenta + "'";
                    consulta += " AND clave=" + "'" + key + "'";
                    rsConsulta = bd.ejecutarConsulta(consulta);
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

                }

            }

            ResultSet auxUsuarios = bd.ejecutarConsulta("SELECT COUNT(*) AS total FROM perfiles " +
                    "WHERE cuenta=" + "'" + idCuenta + "'");

            int num = 0;

            try{
                while (auxUsuarios.next()){
                    num = auxUsuarios.getInt("total");
                }

            } catch (SQLException ex){
                ex.printStackTrace();
            }

            if (num == 0){
                String insercion = "INSERT INTO perfiles (nombre, cuenta, DNI, telefono, fecha_nacimiento, titular, alias) " +
                        "VALUES (?,?,?,?,?,?,?)";

                int titular = 1;
                PreparedStatement st = bd.ejecutarInsercion(insercion);

                try{
                    st.setString(1, nombre);
                    st.setString(2, idCuenta);
                    st.setString(3, DNI);
                    st.setString(4, telefono);
                    st.setString(5, fechaNacimiento);
                    st.setInt(6, titular);
                    st.setString(7, alias);
                    st.execute();

                } catch (SQLException ex){
                    ex.printStackTrace();
                }

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
                }

            }

            else{
                String insercion = "INSERT INTO perfiles (nombre, cuenta, DNI, telefono, fecha_nacimiento, alias) " +
                        "VALUES (?,?,?,?,?,?)";

                PreparedStatement st = bd.ejecutarInsercion(insercion);

                try{
                    st.setString(1, nombre);
                    st.setString(2, idCuenta);
                    st.setString(3, DNI);
                    st.setString(4, telefono);
                    st.setString(5, fechaNacimiento);
                    st.setString(6, alias);
                    st.execute();

                } catch (SQLException ex){
                    ex.printStackTrace();
                }
            }

            bd.cerrarConexion();

            speechText = "Bienvenido, " + nombre + ". Ya tienes disponible tu perfil.";

            return handlerInput.getResponseBuilder()
                    .withSpeech(speechText)
                    .withReprompt(speechText)
                    .build();
        }


        return handlerInput.getResponseBuilder()
                .addDelegateDirective(intent)
                .build();

        /*return handlerInput.getResponseBuilder()
                .withSpeech(intent.getName())
                .build();*/
    }

}
