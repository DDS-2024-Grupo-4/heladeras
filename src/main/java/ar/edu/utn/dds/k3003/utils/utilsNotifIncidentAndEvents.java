package ar.edu.utn.dds.k3003.utils;

import ar.edu.utn.dds.k3003.model.DTO.SuscripcionDTO;
import ar.edu.utn.dds.k3003.model.Incidente;


/***
 *
 * Clase que se encarga de creat tickets de fallas y enviar notificaciones
 * a los colaboradores necesarios haciendo uso de peticiones http
 * 
 * **/
public class utilsNotifIncidentAndEvents {

    public static void notificarFallaEnHeladeraTopic(Incidente incidente) {
        System.out.println("\n notification:\n" + incidente +"\n");
    }

    public static void notificarArregloDeHeladeraEnTopic(Integer heladeraId) {
        System.out.println("\n Heladera Arreglada: " + heladeraId+"\n");
    }

    //#TODO crear aviso de fraude
    public static void notificarFraudeHeladeraEnTopic(Incidente incidente) {
        System.out.println("\n notification: \n"+ incidente+"\n");
    }

    //#TODO crear aviso de temperatura
    public static void notificarExcesoTiempoTemperaturaMaximaEnTopic(Incidente incidente) {
        System.out.println("\n notification: \n"+ incidente + "\n");
    }

    //#TODO crear aviso de heladera abierta
    public static void notificarFallaEnConexionEnTopic(Incidente incidente) {
        System.out.println("\n notification: \n"+ incidente+"\n");
    }

    /**
     * Notifica a los colaboradores informandoles que quedan igual o menos cantidad de lo que setearon, o que ocurrio una falla en la heladera
     * **/
    public static void notificarAColaboradorDeSuSuscripcion(SuscripcionDTO suscripcion) {
        //TODO: manejar el tipo de incidenteDTO, puede tener N o no dependiendo el tipoIncidente
        System.out.println("Sending notification to colaborador: " + suscripcion.colaboradorId);
        System.out.println("Sending notification: "+ suscripcion);
    }
}
