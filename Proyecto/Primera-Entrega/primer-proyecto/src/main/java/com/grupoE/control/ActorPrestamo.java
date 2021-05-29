package com.grupoE.control;

import java.time.format.DateTimeFormatter;
import java.util.StringTokenizer;

import com.grupoE.entity.Peticion;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class ActorPrestamo {
    private ZContext context;
    private ZMQ.Socket server;
    private ZMQ.Socket serverBD_local;
    private ZMQ.Socket serverBD_rep;
    private ZMQ.Socket publisher;
    
    
    public ActorPrestamo(String usrDir){
        try{
            String direccion;
            String direccion_repl;
            if(usrDir.equals("A")){
                //Usando Hamachi A
                direccion = "25.92.125.22";
                direccion_repl = "25.96.193.211";;
            }else if(usrDir.equals("B")){
                //Usando Hamachi B
                direccion = "25.96.193.211";
                direccion_repl = "25.92.125.22";
            }else{
                direccion = "localhost";
                direccion_repl = usrDir;
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
            
            //Crea socket tipo REQ
            serverBD_local = context.createSocket(SocketType.REQ);
            int portBD_local = 8889;
            //Ata el socket a el puerto
            //Usando localhost
            server.connect("tcp://"+ direccion + ":" + portBD_local);
            //client.connect("tcp://25.92.125.22:" + portBD_local);

            //Crea socket tipo REQ
            serverBD_rep = context.createSocket(SocketType.REQ);
            int portBD_rep = 8888;
            //Ata el socket a el puerto
            //Usando localhost
            server.connect("tcp://"+ direccion_repl + ":" + portBD_rep);
            //client.connect("tcp://25.92.125.22:" + port);

            //Crea socket tipo PUB
            publisher = context.createSocket(SocketType.PUB);
            int portPUB = 8887;
            //Ata el socket a el puerto
            //Usando el localhost abre el puerto TCP para todas las interfaces disponibles
            publisher.bind("tcp://*:" + portPUB); 
            //Usando hamachi
            //publisher.bind("tcp://25.93.151.39:"+portPUB);

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
                int dispo = verificarDisponibilidad(peticionAux);
                String msgSend = "false"; 
                if (dispo!= -1){
                    msgSend = "true";
                }
                //Se envía el mensaje
                server.send(msgSend);
                Thread.sleep(1000);
                //Recibe la respuesta del gestor de carga
                publicarRespuesta(peticionAux, dispo);
            }
        } catch (Exception e ){
            System.err.println("No se pudieron enviar las peticiones" + "\n" + e.getMessage());
            System.exit(-1);
        }
    }

    private int verificarDisponibilidad(Peticion peticion){
        try{
            String msgSend = crearMensajePeticion(peticion);  
            System.out.println("etro");
            serverBD_local.send(msgSend);
            String peticionStr = "";
            System.out.println("etro");
            while(true){
                System.out.println(peticionStr);
                peticionStr = serverBD_local.recvStr(0).trim(); 
                if(peticionStr.equals("false")){
                    serverBD_rep.send(msgSend);
                    while(true){
                        peticionStr = serverBD_rep.recvStr(0).trim(); 
                        if(peticionStr.equals("false")){
                            return -1;
                        }
                        if(peticionStr.equals("true")){
                            return 4;
                        }
                    }
                }
                if(peticionStr.equals("true")){
                    return 3;
                }
            }
        }catch (Exception e ){
            System.err.println("No se pudieron enviar las peticiones" + "\n" + e.getMessage());
            System.exit(-1);
        }
        return -1;
    }
    private void publicarRespuesta(Peticion peticion, int topico){
        String msgSend = crearMensajePeticion(peticion);
        publisher.send(topico + " " + msgSend);
    }
    private String crearMensajePeticion(Peticion peticion){
        // Da formato a la fecha
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MMMM/yyyy"); 
        // Obtiene la fecha de la petición y le da formato
        String date = peticion.getFecha().format(dateFormat).toString();
        // Arma el mensaje que se va a enviar
        String msgSend = String.format("%s %s %s",peticion.getIdLibro(), peticion.getTipo().getNumSolicitud(),date);
        return msgSend;
    }
}   
