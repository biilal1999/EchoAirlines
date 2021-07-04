package com.github.satr.ask.handlers;

import com.amazon.ask.Skill;
import com.amazon.ask.SkillStreamHandler;
import com.amazon.ask.Skills;
import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Intent;
import com.github.satr.ask.handlers.Modelo.Database;
import com.github.satr.ask.handlers.lambdas.*;

import static com.amazon.ask.request.Predicates.intentName;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

public class MainStreamHandler extends SkillStreamHandler{

    private static Skill getSkill() {
        return Skills.standard()
                .addRequestHandlers(
                        new CancelAndStopIntent(),
                        new HelpIntentHandler(),
                        new CustomLaunchRequestHandler(),
                        new SessionEndedRequestHandler(),
                        new RepeatIntentHandler(),
                        new FechaDefectoVerMisVuelosIntent(),
                        new FechaPuestaVerMisVuelosIntent(),
                        new AliasDefectoVerMisVuelosIntent(),
                        new AliasPuestoVerMisVuelosIntent(),
                        new CompletedVerMisVuelosIntent(),
                        new ProcessCreaPerfilIntent(),
                        new StartedInProgressReservaVueloIntent(),
                        new PrecioDefectoReservaVueloIntent(),
                        new PrecioPuestoReservaVueloIntent(),
                        new MesReservaVueloIntent(),
                        new FechaReservaVueloIntent(),
                        new SalidaReservaVueloIntent(),
                        new AccionReservaVueloIntent(),
                        new ConfirmedReservaVueloIntent(),
                        new DeniedReservaVueloIntent(),
                        new StartedInProgressConsultaVueloIntent(),
                        //new OrigenDefectoConsultaVueloIntent(),
                        //new OrigenPuestoConsultaVueloIntent(),
                        new MesConsultaVueloIntent(),
                        new FechaDefectoConsultaVueloIntent(),
                        new FechaPuestaConsultaVueloIntent(),
                        new PrecioDefectoConsultaVueloIntent(),
                        new PrecioPuestoConsultaVueloIntent(),
                        new CompletedConsultaVueloIntent(),
                        new StartedInProgressCancelaReservaIntent(),
                        new AccionCancelaReservaIntent(),
                        new ConfirmedCancelaReservaIntent(),
                        new DeniedCancelaReservaIntent(),
                        new StartedInProgressNuevoUsuarioIntent(),
                        new ClaveNuevoUsuarioIntent(),
                        new CompletedNuevoUsuarioIntent(),
                        new StartedInProgressConfiguraPerfilIntent(),
                        new CompletedConfiguraPerfilIntent(),
                        new StartedInProgressConfiguraUsuarioIntent(),
                        new ClaveConfiguraUsuarioIntent(),
                        new TarjetaConfiguraUsuarioIntent(),
                        new PaisConfiguraUsuarioIntent(),
                        new PaisCiudadConfiguraUsuarioIntent(),
                        new CiudadConfiguraUsuarioIntent(),
                        new CompletedConfiguraUsuarioIntent(),
                        new StartedInProgressBorraPerfilIntent(),
                        new ConfirmedBorraPerfilIntent(),
                        new DeniedBorraPerfilIntent(),
                        new NoIntentHandler())
                .withSkillId(System.getenv("SKILL_ID"))
                .build();
    }

    public MainStreamHandler() { super(getSkill()); }

}
