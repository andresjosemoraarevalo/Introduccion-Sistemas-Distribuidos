package com.grupoE.control;

import java.time.format.DateTimeFormatter;
import java.util.StringTokenizer;

import com.grupoE.entity.Peticion;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class GestorDeCarga {
    private ZContext context;
    private ZMQ.Socket server;

    public GestorDeCarga(){
        try{
            //Se establece un contexto ZeroMQ
            context = new ZContext();
            //Crea socket tipo REP
            server = context.createSocket(SocketType.REP);
            int port = 5556;
            //Ata el socket a el puerto
            //Usando el localhost
            server.bind("tcp://*:"+port);
            //Usando hamachi
            //server.bind("tcp://25.93.151.39:"+port);
        } catch (Exception e){
            System.err.println("No se pudo inicializar el servidor");
            System.exit(-1);
        }
    }
    public static void main(String[] args) {
        GestorDeCarga gc = new GestorDeCarga();
        gc.leerProcesosSolicitantes();
    }

    /**
     * Función que recibe los request de los procesos solicitantes y les envía respuesta
     */
    private void leerProcesosSolicitantes(){
        try{
            while(!Thread.currentThread().isInterrupted()){
                // Recibe el request del proceso solicitante
                String peticionStr = server.recvStr(0).trim(); 
                // Separa la palabra por espacios
                StringTokenizer strTok = new StringTokenizer(peticionStr, " ");
                // Se obtiene el ID del libro
                int idLibro = Integer.parseInt(strTok.nextToken());
                // Se obtiene el tipo de proceso
                int tipo = Integer.parseInt(strTok.nextToken());
                // Se obtiene la fecha del proceso
                String fecha = strTok.nextToken();
                //Se arma la petición
                Peticion peticionAux = new Peticion(idLibro,tipo,fecha);
                //Se muestra en consola para saber en cual petición va
                System.out.println(peticionAux.toString());
                Thread.sleep(1000);
                //Se procesa y se envía la petición hacia el proceso solicitante
                server.send(this.procesarPeticion(peticionAux));
            }
    
        } catch (Exception e){
            System.err.println("No se pudo recibir el mensaje" + e.getMessage());
            System.exit(-1);
        }
    }
    
    /**
     * 
     * @param p Peticion que recibe de el proceso solicitante
     * @return mensaje de confirmación o de error que da respuesta a la peticion
     */
    private String procesarPeticion(Peticion p){
        if(p.getTipo().getNumSolicitud() == 1){
            return "Devolucion exitosa";
        }else if(p.getTipo().getNumSolicitud() == 2){
            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MMMM/yyyy");
            return "Nueva fecha de entrega " + p.getFecha().plusDays(7).format(dateFormat);
        }else if(p.getTipo().getNumSolicitud() == 3){
            return "Solicitado";
        }
        return null;
    }
}