package com.grupoE.control;

import java.util.StringTokenizer;

import com.grupoE.entity.Peticion;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class ActorPrestamo {
    private ZContext context;
    private ZMQ.Socket server;

    public ActorPrestamo(String opcion){
        try{
            String direccion;
            if(opcion.equals("A")){
                //Usando Hamachi A
                direccion = "25.92.125.22";
            }else if(opcion.equals("B")){
                //Usando Hamachi B
                direccion = "25.96.193.211";
            }else{
                direccion = opcion;
            }
            //Se establece un contexto ZeroMQ
            context= new ZContext();
            //Crea socket tipo REQ
            server = context.createSocket(SocketType.REP);
            int port = 6666;
            //Ata el socket a el puerto
            //Usando localhost
            server.bind("tcp://"+ direccion + ":" + port);
            
            //client.connect("tcp://25.92.125.22:" + port);
        } catch (Exception e) {
            System.err.println("No se pudo conectar al servidor" + "\n" + e.getMessage());
            System.exit(-1);
        }
    }
    public static void main(String[] args) {
        if(args.length==0){ // Verifica que se ingrese los argumentos correctos
            System.out.println("Ingrese: java [path] [sede]");
            System.out.println("La sede puede ser A, B o la que desee (XXX.XXX.XXXX.XXXX)");
            System.exit(-1);
        }
        System.out.println("Conectando al servidor...");
        // Se crea el contexto, el socket y se ata a un puerto
        ActorPrestamo ap = new ActorPrestamo(args[0]);
        // Envia las peticiones al servidor con el patrón requesr-reply
        ap.leerPrestamos();
    }

    /** 
     * Lee las peticiones del archivo "peticiones.csv" y las envía al Gestor de Carga
    */
    public void leerPrestamos(){ 
        try{
            while(!Thread.currentThread().isInterrupted()){
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
                System.out.println("Actor Prestamo"+peticionAux.toString());
                String msgSend = "True"; 
                //Se envía el mensaje
                server.send(msgSend);
                Thread.sleep(1000);
                //Recibe la respuesta del gestor de carga
                System.out.println(msgSend);
            }
        } catch (Exception e ){
            System.err.println("No se pudieron enviar las peticiones" + "\n" + e.getMessage());
            System.exit(-1);
        }
    }
}   
