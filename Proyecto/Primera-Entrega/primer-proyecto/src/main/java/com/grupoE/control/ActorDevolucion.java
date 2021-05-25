package com.grupoE.control;

import java.nio.charset.Charset;
import java.time.format.DateTimeFormatter;
import java.util.StringTokenizer;

import com.grupoE.entity.Peticion;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class ActorDevolucion {
    private ZContext context;
    private ZMQ.Socket client;
    private ZMQ.Socket publisher;

    public ActorDevolucion(String opcion){
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
            //Crea socket tipo SUB
            client = context.createSocket(SocketType.SUB);
            int port = 7776;
            //Ata el socket a el puerto
            //Usando localhost
            client.connect("tcp://"+ direccion + ":" + port);
            String filter = "1";
            client.subscribe(filter.getBytes(Charset.forName("UTF-8")));
            //client.connect("tcp://25.92.125.22:" + port);

            //Crea socket tipo PUB
            publisher = context.createSocket(SocketType.PUB);
            int portPUB = 8886;
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
        ActorDevolucion ad = new ActorDevolucion(args[0]);
        // Envia las peticiones al servidor con el patrón requesr-reply
        ad.leerDevoluciones();
    }
    public void leerDevoluciones(){ 
        try{
            while(!Thread.currentThread().isInterrupted()){
                String peticionStr = client.recvStr(0).trim();
                // Separa la palabra por espacios
                StringTokenizer strTok = new StringTokenizer(peticionStr, " ");
                //Se obtiene topico
                int topico =  Integer.parseInt(strTok.nextToken());
                if(topico == 1){
                    // Se obtiene el ID del libro
                    int idLibro = Integer.parseInt(strTok.nextToken());
                    // Se obtiene el tipo de proceso
                    int tipo = Integer.parseInt(strTok.nextToken());
                    // Se obtiene la fecha del proceso
                    String fecha = strTok.nextToken();
                    //Se arma la petición
                    Peticion peticionAux = new Peticion(idLibro,tipo,fecha);
                    //Se muestra en consola para saber en cual petición va
                    System.out.println("Actor Devolucion"+peticionAux.toString());
                    publicarRespuesta(peticionAux, tipo);
                }
            }
        } catch (Exception e ){
            System.err.println("No se pudieron enviar las peticiones" + "\n" + e.getMessage());
            System.exit(-1);
        }
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
