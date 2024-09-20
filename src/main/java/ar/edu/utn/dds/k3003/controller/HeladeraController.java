package ar.edu.utn.dds.k3003.controller;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.facades.dtos.HeladeraDTO;
import ar.edu.utn.dds.k3003.facades.dtos.RetiroDTO;
import ar.edu.utn.dds.k3003.facades.dtos.TemperaturaDTO;
import ar.edu.utn.dds.k3003.model.DTO.DepositoDTO;
import ar.edu.utn.dds.k3003.model.DTO.GetErrorHeladeraDTO;
import ar.edu.utn.dds.k3003.utils.utilsHeladera;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.jetbrains.annotations.Debug;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

public class HeladeraController{
    private final Fachada fachada;

    public HeladeraController(Fachada fachada){
        this.fachada = fachada;
    }

    public void agregar(@NotNull Context context){
        try{
            HeladeraDTO heladeraDTO = fachada.agregar(context.bodyAsClass(HeladeraDTO.class));
            context.json(heladeraDTO);
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
            var heladeraDTO = fachada.obtenerHeladera(heladeraId);
            context.json(heladeraDTO);
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

    public void depositarVianda(@NotNull Context context){
        try{
            DepositoDTO depositoDTO = context.bodyAsClass(DepositoDTO.class);
            if (!fachada.existeHeladera(depositoDTO.getHeladeraId())) {
                context.status(HttpStatus.NOT_FOUND);
                context.result("Heladera no encontrada :c");
            }
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

    public void registrarTemperatura(@NotNull Context context){
        try{
            fachada.temperatura(context.bodyAsClass(TemperaturaDTO.class));
            context.status(HttpStatus.OK);
            context.result("Temperatura registrada correctamente");
        }
        catch(NoSuchElementException e){
            context.result(e.getLocalizedMessage());
            context.status(HttpStatus.BAD_REQUEST);
            context.result("Error de solicitud");
        }
    }

    public void obtenerTemperaturas(@NotNull Context context){
        try{
            Integer heladeraId = Integer.valueOf(context.pathParam("heladeraId"));
            if (!fachada.existeHeladera(heladeraId)) {
                throw new NoSuchElementException();
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
    }
    public void crearHeladerasGenericas(Context context){
        try {
            utilsHeladera.crearHeladeras(this.fachada);
            context.status(201).result("Heladeras genericas creadas");
        } catch (Exception e) {
            context.status(500).result("Error de Servidor: " + e.getMessage());
        }
    }
    public void borrarTodo(Context context){
        try {
            utilsHeladera.borrarTodo(this.fachada);
            context.status(200).result("Todo borrado por aca :P");
        } catch (Exception e) {
            context.status(500).result("Error de Servidor: " + e.getMessage());
        }
    }
}
