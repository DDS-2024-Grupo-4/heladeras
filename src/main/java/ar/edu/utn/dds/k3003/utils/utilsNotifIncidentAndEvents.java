package ar.edu.utn.dds.k3003.utils;

import ar.edu.utn.dds.k3003.model.DTO.IncidenteDTO;

public class utilsNotifIncidentAndEvents {

    //#TODO crear aviso de fraude
    public static void notificarFraudeHeladeraEnTopic(IncidenteDTO incidenteDTO) {
        System.out.println("Sending notification: "+ incidenteDTO);
    }

    //#TODO crear aviso de temperatura
    public static void notificarExcesoTiempoTemperaturaMaximaEnTopic(IncidenteDTO incidenteDTO) {
        System.out.println("Sending notification: "+ incidenteDTO);
    }

    //#TODO crear aviso de heladera abierta
    public static void notificarFallaEnConexionEnTopic(IncidenteDTO incidenteDTO) {
        System.out.println("Sending notification: "+ incidenteDTO);
    }

    public static void notificarIncidenteAColaborador(Long colaboradorID, IncidenteDTO incidenteDTO) {
        //TODO: manejar el tipo de incidenteDTO, puede tener N o no dependiendo el tipoIncidente
        System.out.println("Sending notification to colaborador: " + colaboradorID);
        System.out.println("Sending notification: "+ incidenteDTO);
    }
}
