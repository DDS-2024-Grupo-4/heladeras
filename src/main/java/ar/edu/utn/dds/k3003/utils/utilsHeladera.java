package ar.edu.utn.dds.k3003.utils;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.facades.dtos.HeladeraDTO;
import ar.edu.utn.dds.k3003.facades.dtos.TemperaturaDTO;
import ar.edu.utn.dds.k3003.model.DTO.ViandaDTO;
import ar.edu.utn.dds.k3003.model.Heladera;

import java.time.LocalDateTime;
import java.util.List;

public class utilsHeladera {

    public static void crearHeladeras(Fachada fachada){

        HeladeraDTO heladeraNuevaDTO1 = new HeladeraDTO(null, "Heladera1", null);
        HeladeraDTO heladeraNuevaDTO2 = new HeladeraDTO(null, "Heladera2", null);
        HeladeraDTO heladeraNuevaDTO3 = new HeladeraDTO(null, "Heladera3", null);

        //Guardp y obtengo sus nuevos IDs asignados
        HeladeraDTO heladeraRefrescadaDTO1 = fachada.agregar(heladeraNuevaDTO1);
        HeladeraDTO heladeraRefrescadaDTO2 = fachada.agregar(heladeraNuevaDTO2);
        HeladeraDTO heladeraRefrescadaDTO3 = fachada.agregar(heladeraNuevaDTO3);

        //Temperaturas de la heladera 1
        TemperaturaDTO H1temperatura1 = new TemperaturaDTO(32, heladeraRefrescadaDTO1.getId(), LocalDateTime.now());
        TemperaturaDTO H1temperatura2 = new TemperaturaDTO(36, heladeraRefrescadaDTO1.getId(), LocalDateTime.now());
        TemperaturaDTO H1temperatura3 = new TemperaturaDTO(42, heladeraRefrescadaDTO1.getId(), LocalDateTime.now());
        //Temperaturas de la heladera 2
        TemperaturaDTO H2temperatura1 = new TemperaturaDTO(56, heladeraRefrescadaDTO2.getId(), LocalDateTime.now());
        TemperaturaDTO H2temperatura2 = new TemperaturaDTO(66, heladeraRefrescadaDTO2.getId(), LocalDateTime.now());
        //Temperaturas de la heladera 3
        TemperaturaDTO H3temperatura1 = new TemperaturaDTO(87, heladeraRefrescadaDTO3.getId(), LocalDateTime.now());

        fachada.temperatura(H1temperatura1);
        fachada.temperatura(H1temperatura2);
        fachada.temperatura(H1temperatura3);
        fachada.temperatura(H2temperatura1);
        fachada.temperatura(H2temperatura2);
        fachada.temperatura(H3temperatura1);

    }
    public static void borrarTodo(Fachada fachada){
        List<Heladera> heladeras= fachada.obtenerTodasLasHeladeras();
        for(Heladera heladera: heladeras){
            fachada.eliminarHeladera(heladera.getHeladeraId());
        }
    }
}
