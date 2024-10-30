package ar.edu.utn.dds.k3003.utils;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.facades.dtos.HeladeraDTO;
import ar.edu.utn.dds.k3003.model.Heladera;
import java.util.List;

public class utilsHeladera {

    public static void crearHeladeras(Fachada fachada){

            HeladeraDTO heladeraNuevaDTO1 = new HeladeraDTO(null, "Heladera1", null);
            HeladeraDTO heladeraNuevaDTO2 = new HeladeraDTO(null, "Heladera2", null);
            HeladeraDTO heladeraNuevaDTO3 = new HeladeraDTO(null, "Heladera3", null);

            //Guardp y obtengo sus nuevos IDs asignados
            fachada.agregar(heladeraNuevaDTO1);
            fachada.agregar(heladeraNuevaDTO2);
            fachada.agregar(heladeraNuevaDTO3);

        }
        public static void borrarTodo(Fachada fachada){
            List<Heladera> heladeras= fachada.obtenerTodasLasHeladeras();
        for(Heladera heladera: heladeras){
            fachada.eliminarHeladera(heladera.getHeladeraId());
        }
    }
}
