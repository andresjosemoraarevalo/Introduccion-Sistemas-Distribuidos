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
        }
    }
    public static void main(String[] args) {
        GestorDeCarga gc = new GestorDeCarga();
        gc.leerProcesosSolicitantes();
    }
    private void leerProcesosSolicitantes(){
        try{
            while(!Thread.currentThread().isInterrupted()){
                String peticionStr = server.recvStr(0).trim();
                //System.out.println(peticionStr);
                StringTokenizer strTok = new StringTokenizer(peticionStr, " ");
                int idLibro = Integer.parseInt(strTok.nextToken());
                int tipo = Integer.parseInt(strTok.nextToken());
                String fecha = strTok.nextToken();

                Peticion peticionAux = new Peticion(idLibro,tipo,fecha);

                System.out.println(peticionAux.toString());
                Thread.sleep(1000);
                
                server.send(this.procesarPeticion(peticionAux));
            }
    
        } catch (Exception e){
            System.err.println("No se pudo recibir el mensaje" + e.getMessage());
        }
    }

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