package ar.edu.utn.dds.k3003.controller;

import ar.edu.utn.dds.k3003.Service.IncidenteService;
import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.model.DTO.HeladeraDtoPerso;
import ar.edu.utn.dds.k3003.model.DTO.IncidenteDTO;
import ar.edu.utn.dds.k3003.model.DTO.RetiroDTODay;
import ar.edu.utn.dds.k3003.model.DTO.SuscripcionDTO;
import ar.edu.utn.dds.k3003.model.DTO.ViandaDTO;
import ar.edu.utn.dds.k3003.model.Heladera;
import ar.edu.utn.dds.k3003.model.Incidente;
import ar.edu.utn.dds.k3003.model.TipoSuscripcion;
import ar.edu.utn.dds.k3003.utils.utilsMetrics;
import ar.edu.utn.dds.k3003.facades.dtos.HeladeraDTO;
import ar.edu.utn.dds.k3003.facades.dtos.RetiroDTO;
import ar.edu.utn.dds.k3003.facades.dtos.TemperaturaDTO;
import ar.edu.utn.dds.k3003.model.DTO.DepositoDTO;
import ar.edu.utn.dds.k3003.model.DTO.GetErrorHeladeraDTO;
import ar.edu.utn.dds.k3003.utils.utilsHeladera;
import ar.edu.utn.dds.k3003.utils.utilsPublisher;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import static ar.edu.utn.dds.k3003.model.TipoSuscripcion.*;

//TODO FALTA LA PARTE DE DESHABILITAR HELADERA Y QUE SI ESTA DESHABILITADA PASEMOS DE LARGO Y TIREMOS ERROR!
public class HeladeraController{
    private final Fachada fachada;
    private final IncidenteService incidenteService;

    public HeladeraController(Fachada fachada, IncidenteService incidenteService){
        this.fachada = fachada;
        this.incidenteService = incidenteService;
    }

    public void agregar(@NotNull Context context){
        try{
            HeladeraDTO heladeraDTO = context.bodyAsClass(HeladeraDTO.class);
            HeladeraDTO heladeraDTOdevuelta = fachada.agregar(heladeraDTO);
            context.json(heladeraDTOdevuelta);
            context.status(HttpStatus.OK);
        }
        catch(NoSuchElementException e){
            GetErrorHeladeraDTO errorHeladeraDTO =
                    new GetErrorHeladeraDTO(0,"Error Agregando Heladera");
            context.json(errorHeladeraDTO);
            context.status(HttpStatus.BAD_REQUEST);
        }
    }

    public void obtenerHeladera(@NotNull Context context){
        try {
            String heladeraIdParam = context.pathParam("heladeraId");
            Integer heladeraId = Integer.valueOf(heladeraIdParam);
            Heladera heladera = fachada.obtenerHeladera(heladeraId);
            HeladeraDtoPerso heladeraDtoPerso = new HeladeraDtoPerso(
                heladera.getHeladeraId(),
                heladera.getNombre(),
                heladera.cantidadDeViandas(),
                heladera.estaActiva()
            );
            context.json(heladeraDtoPerso);
            context.status(HttpStatus.OK);
        } catch (NoSuchElementException e) {
            context.status(HttpStatus.NOT_FOUND);
            context.result("Heladera no encontrada :c");
        } catch (Exception e) {
            e.printStackTrace();
            context.status(HttpStatus.INTERNAL_SERVER_ERROR);
            context.result("Error interno del servidor " + e.getMessage());
        }
    }

    public List<Heladera> obtenerTodasLasHeladeras(){
        return fachada.obtenerTodasLasHeladeras();
    }

    public void depositarVianda(@NotNull Context context){
        try{ 
            String heladeraIdParam = context.pathParam("heladeraId");
            Integer heladeraId = Integer.valueOf(heladeraIdParam);
            if(!fachada.heladeraHabilitada(heladeraId)){
                context.status(HttpStatus.FORBIDDEN);
                context.result("La heladera no está habilitada.");
                return;
            }
            String codigoQR = context.pathParam("qrVianda");
            DepositoDTO depositoDTO = new DepositoDTO(heladeraId, codigoQR);
            if (!fachada.existeHeladera(depositoDTO.getHeladeraId())) {
                context.status(HttpStatus.NOT_FOUND);
                context.result("Heladera no encontrada :c");
            }
            utilsMetrics.enviarNuevaAperuraDeHeladera();
            fachada.depositar(depositoDTO.getHeladeraId(), depositoDTO.getCodigoQR());
            context.status(HttpStatus.OK);
            context.result("Vianda depositada correctamente");
        }
        catch(NoSuchElementException e){
            context.result(e.getLocalizedMessage());
            context.status(HttpStatus.BAD_REQUEST);
            context.result("Error de solicitud");
        }
    }

    public void retirarVianda(@NotNull Context context){
        try{
            RetiroDTO retiroDTO = context.bodyAsClass(RetiroDTO.class);
            if (!fachada.existeHeladera(retiroDTO.getHeladeraId())) {
                context.status(HttpStatus.NOT_FOUND);
                context.result("Heladera no encontrada :c");
            }
            if(!fachada.heladeraHabilitada(retiroDTO.getHeladeraId())){
                context.status(HttpStatus.FORBIDDEN);
                context.result("La heladera no está habilitada.");
                return;
            }
            utilsMetrics.enviarNuevaAperuraDeHeladera();
            fachada.retirar(retiroDTO);
            context.status(HttpStatus.OK);
            context.result("Vianda retirada exitosamente");
        }
        catch(NoSuchElementException e){
            context.result(e.getLocalizedMessage());
            context.status(HttpStatus.BAD_REQUEST);
            context.result("Error de solicitud");
        }
    }

    public void registrarTemperaturaEnCola(@NotNull Context context){
        try {
            TemperaturaDTO temperaturaDTO = context.bodyAsClass(TemperaturaDTO.class);
            //validacion
            if (temperaturaDTO.getHeladeraId() == null || temperaturaDTO.getTemperatura() == null) {
                context.status(HttpStatus.BAD_REQUEST);
                context.result("Heladera ID y Temperatura son obligatorios.");
                return;
            }
            if(!fachada.heladeraHabilitada(temperaturaDTO.getHeladeraId())){
                context.status(HttpStatus.FORBIDDEN);
                context.result("La heladera no está habilitada.");
                return;
            }
            // Formato del mensaje
            String mensaje = String.format("Heladera %d - Temperatura %d°C",
                temperaturaDTO.getHeladeraId(),
                temperaturaDTO.getTemperatura());

            // Publicar el mensaje en la cola
            utilsPublisher.pushMessageQueue(mensaje);

            // Responder al cliente
            context.status(HttpStatus.OK);
            context.result("Temperatura registrada correctamente.");

        } catch (IOException e) {
            context.status(HttpStatus.INTERNAL_SERVER_ERROR);
            context.result("Error al procesar la solicitud: " + e.getMessage());
        } catch (Exception e) {
            context.status(HttpStatus.INTERNAL_SERVER_ERROR);
            context.result("Error inesperado: " + e.getMessage());
        }
    }

    public void registrarTemperatura(TemperaturaDTO temperaturaDTO) throws RuntimeException {
        try{
            if(!fachada.heladeraHabilitada(temperaturaDTO.getHeladeraId())){
                //throw new RuntimeException("La heladera " + temperaturaDTO.getHeladeraId() + " no está habilitada, imposible setearle temperatura");
            }
            fachada.temperatura(temperaturaDTO);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void obtenerTemperaturas(@NotNull Context context){
        try{
            Integer heladeraId = Integer.valueOf(context.pathParam("heladeraId"));
            if (!fachada.existeHeladera(heladeraId)) {
                throw new NoSuchElementException();
            }
            if(!fachada.heladeraHabilitada(heladeraId)){
                context.status(HttpStatus.FORBIDDEN);
                context.result("La heladera no está habilitada.");
                return;
            }
            List<TemperaturaDTO> temperaturas = fachada.obtenerTemperaturas(heladeraId);
            context.json(temperaturas);
            context.status(HttpStatus.OK);
        }
        catch(NoSuchElementException e){
            context.result(e.getLocalizedMessage());
            context.status(HttpStatus.BAD_REQUEST);
            context.result("Heladera no encontrada");
        }
        catch (RuntimeException e){
            context.result(e.getLocalizedMessage());
            context.status(HttpStatus.NOT_FOUND);
            context.result("Heladera sin temperaturas Seteadas");
        }
    }

    public void obtenerRetirosDelDia(@NotNull Context context){
        try{
            Integer heladeraId = Integer.valueOf(context.pathParam("heladeraId"));
            if (!fachada.existeHeladera(heladeraId)) {
                throw new NoSuchElementException();
            }
            List<RetiroDTODay> retirosDelDia = fachada.obtenerRetirosDelDia(heladeraId);
            context.json(retirosDelDia);
            context.status(HttpStatus.OK);
        }
        catch(NoSuchElementException e){
            context.result(e.getLocalizedMessage());
            context.status(HttpStatus.BAD_REQUEST);
            context.result("Heladera no encontrada");
        }
        catch (RuntimeException e){
            context.result(e.getLocalizedMessage());
            context.status(HttpStatus.NOT_FOUND);
            context.result("Heladera sin retiros en el dia de la fecha");
        }
    }

    public void viandasEnHeladera(@NotNull Context context){
        try{
            Integer heladeraId = Integer.valueOf(context.pathParam("heladeraId"));
            if (!fachada.existeHeladera(heladeraId)) {
                throw new NoSuchElementException();
            }
            List<String> viandasEnHeladera = fachada.viandasEnHeladera(heladeraId);
            context.json(viandasEnHeladera);
            context.status(HttpStatus.OK);
        }
        catch(NoSuchElementException e){
            context.result(e.getLocalizedMessage());
            context.status(HttpStatus.BAD_REQUEST);
            context.result("Heladera no encontrada");
        }
        catch (RuntimeException e){
            context.result(e.getLocalizedMessage());
            context.status(HttpStatus.NOT_FOUND);
            context.result("Heladera sin Viandas");
        }
    }

    public void cantidadViandasHastaLLenar(@NotNull Context context){
        try{
            Integer heladeraId = Integer.valueOf(context.pathParam("heladeraId"));
            if (!fachada.existeHeladera(heladeraId)) {
                throw new NoSuchElementException();
            }
            // Obtiene una lista de Integers (cantidad hasta llenar y máxima de viandas)
            List<Integer> viandasInfo = fachada.cantidadViandasHastaLLenarInfo(heladeraId);
            context.json(viandasInfo);
            context.status(HttpStatus.OK);
        }
        catch(NoSuchElementException e){
            context.result(e.getLocalizedMessage());
            context.status(HttpStatus.BAD_REQUEST);
            context.result("Heladera no encontrada");
        }
        catch (RuntimeException e){
            context.result(e.getLocalizedMessage());
            context.status(HttpStatus.NOT_FOUND);
            context.result("Heladera sin historial");
        }
    }

    public void obtenerHistorialIncidentes(@NotNull Context context){
        try{
            Integer heladeraId = Integer.valueOf(context.pathParam("heladeraId"));
            if (!fachada.existeHeladera(heladeraId)) {
                throw new NoSuchElementException();
            }
            List<Incidente> incidentesHistorial = fachada.obtenerIncidenteHistorial(heladeraId);
            context.json(incidentesHistorial);
            context.status(HttpStatus.OK);
        }
        catch(NoSuchElementException e){
            context.result(e.getLocalizedMessage());
            context.status(HttpStatus.BAD_REQUEST);
            context.result("Heladera no encontrada");
        }
        catch (RuntimeException e){
            context.result(e.getLocalizedMessage());
            context.status(HttpStatus.NOT_FOUND);
            context.result("Heladera sin historial");
        }
    }

    public void reportarFalla(Context context){
        try{
            Integer heladeraId = Integer.valueOf(context.pathParam("heladeraId"));
            if (!fachada.existeHeladera(heladeraId)) {
                throw new NoSuchElementException();
            }
            incidenteService.fallaEnHeladera(heladeraId);
            context.status(HttpStatus.OK);
        }
        catch(NoSuchElementException e){
            context.result(e.getLocalizedMessage());
            context.status(HttpStatus.BAD_REQUEST);
            context.result("Heladera no encontrada");
        }
    }

    public void arreglarFalla(Context context){
        try{
            Integer heladeraId = Integer.valueOf(context.pathParam("heladeraId"));
            if (!fachada.existeHeladera(heladeraId)) {
                throw new NoSuchElementException();
            }
            incidenteService.reparacionHeladera(heladeraId);
            context.status(HttpStatus.OK);
        }
        catch(NoSuchElementException e){
            context.result(e.getLocalizedMessage());
            context.status(HttpStatus.BAD_REQUEST);
            context.result("Heladera no encontrada");
        }
    }

    public void registrarSuscripcion(Context context){
        SuscripcionDTO suscripcionDTO = context.bodyAsClass(SuscripcionDTO.class);
        Set<TipoSuscripcion> tiposValidosSuscripciones = Set.of(ViandasDisponibles,FaltanteViandas,HeladeraDesperfecto);
        if (suscripcionDTO.tipoSuscripcion == null || suscripcionDTO.heladeraId == null || suscripcionDTO.colaboradorId == null) {
            context.status(HttpStatus.BAD_REQUEST);
            context.result("Heladera ID, Colaborador ID y Tipo de Suscripcion son obligatorios.");
        }
        else if (!fachada.existeHeladera(suscripcionDTO.heladeraId)) {
            context.status(HttpStatus.NOT_FOUND);
            context.result("Heladera no encontrada :c");
        }
        else if (!tiposValidosSuscripciones.contains(suscripcionDTO.tipoSuscripcion)){
            context.status(HttpStatus.BAD_REQUEST);
            context.result("Tipo de Suscripcion invalido.");
        }
        else{
            fachada.suscribirse(suscripcionDTO);
            context.status(HttpStatus.OK);
            context.result("Suscripcion realizada correctamente.");
        }
    }

    public void obtenerSuscripciones(@NotNull Context context){
        try{
            Integer heladeraId = Integer.valueOf(context.pathParam("heladeraId"));
            if (!fachada.existeHeladera(heladeraId)) {
                throw new NoSuchElementException();
            }
            List<SuscripcionDTO> suscripciones = fachada.obtenerSuscripciones(heladeraId);
            context.json(suscripciones);
            context.status(HttpStatus.OK);
        }
        catch(NoSuchElementException e){
            context.result(e.getLocalizedMessage());
            context.status(HttpStatus.BAD_REQUEST);
            context.result("Heladera no encontrada");
        }
    }

    public void eliminarSuscripcion(Context context){
        SuscripcionDTO suscripcionDTO = context.bodyAsClass(SuscripcionDTO.class);
        Set<String> tiposValidosSuscripciones = Set.of("ViandasDisponibles", "FaltanteViandas", "HeladeraDesperfecto");
        if (suscripcionDTO.tipoSuscripcion == null || suscripcionDTO.heladeraId == null || suscripcionDTO.colaboradorId == null) {
            context.status(HttpStatus.BAD_REQUEST);
            context.result("Heladera ID, Colaborador ID y Tipo de Suscripcion son obligatorios.");
        }
        else if (!fachada.existeHeladera(suscripcionDTO.heladeraId)) {
            context.status(HttpStatus.NOT_FOUND);
            context.result("Heladera no encontrada :c");
        }
        else if (!tiposValidosSuscripciones.contains(suscripcionDTO.tipoSuscripcion)){
            context.status(HttpStatus.BAD_REQUEST);
            context.result("Tipo de Suscripcion invalido.");
        }
        else{
            fachada.eliminarSuscripcion(suscripcionDTO);
            context.status(HttpStatus.OK);
            context.result("Suscripcion Eliminada correctamente.");
        }
    }

    public void crearHeladerasGenericas(Context context){
        System.out.println(this.fachada);
        try {
            utilsHeladera.crearHeladeras(this.fachada);
            context.status(201).result("Heladeras genericas creadas");
        } catch (Exception e) {
            context.status(500).result("Error de Servidor: " + e.getMessage());
        }
    }
    public void borrarTodo(Context context){
        try {
            fachada.eliminarTablasDirectamenteYResetearIDs();
            context.status(200).result("Borrado Completo");
        } catch (Exception e) {
            context.status(500).result("Error de Servidor: " + e.getMessage());
        }
    }

}
