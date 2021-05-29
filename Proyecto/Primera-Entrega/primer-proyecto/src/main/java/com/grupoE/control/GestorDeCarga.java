package com.grupoE.control;

import java.time.format.DateTimeFormatter;
import java.util.StringTokenizer;

import com.grupoE.entity.Peticion;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class GestorDeCarga {
    private ZContext context;
    private ZMQ.Socket serverPS;
    private ZMQ.Socket clientAP;
    private ZMQ.Socket publisher;

    public GestorDeCarga(String opcion){
        try{
            String direccion;
            if(opcion.equals("A")){
                //Usando Hamachi A
                direccion = "25.92.125.22";
            }else if(opcion.equals("B")){
                //Usando Hamachi B
                direccion = "25.104.197.200";
            }else{
                direccion = opcion;
            }
            //Se establece un contexto ZeroMQ
            context = new ZContext();

            //Crea socket tipo REP
            serverPS = context.createSocket(SocketType.REP);
            int portPS = 5556;
            //Ata el socket a el puerto
            //Usando el localhost abre el puerto TCP para todas las interfaces disponibles
            serverPS.bind("tcp://"+ direccion + ":" + portPS); 
            //Usando hamachi
            //serverPS.bind("tcp://25.93.151.39:"+portPS);

            //Crea socket tipo REQ
            clientAP = context.createSocket(SocketType.REQ); 
            int portAP = 6666;
            //Ata el socket a el puerto
            //Usando el localhost abre el puerto TCP para todas las interfaces disponibles
            clientAP.connect("tcp://"+ direccion + ":" + portAP); 
            //Usando hamachi
            //clientAP.bind("tcp://25.93.151.39:"+portAP);

            //Crea socket tipo PUB
            publisher = context.createSocket(SocketType.PUB);
            int portPUB = 7776;
            //Ata el socket a el puerto
            //Usando el localhost abre el puerto TCP para todas las interfaces disponibles
            publisher.bind("tcp://"+ direccion + ":" + portPUB); 
            //Usando hamachi
            //publisher.bind("tcp://25.93.151.39:"+portPUB);

        } catch (Exception e){
            System.err.println("No se pudo inicializar el servidor");
            System.exit(-1);
        }
    }
    public static void main(String[] args) {
        if(args.length==0){ // Verifica que se ingrese los argumentos correctos
            System.out.println("Ingrese: java [path] [sede]");
            System.out.println("La sede puede ser A, B o la que desee (XXX.XXX.XXXX.XXXX)");
            System.exit(-1);
        }
        System.out.println("Inciando servidor...");
        GestorDeCarga gc = new GestorDeCarga(args[0]);
        gc.leerProcesosSolicitantes();
    }

    /**
     * Función que recibe los request de los procesos solicitantes y les envía respuesta
     */
    private void leerProcesosSolicitantes(){
        try{
            while(!Thread.currentThread().isInterrupted()){
                // Recibe el request del proceso solicitante
                String peticionStr = serverPS.recvStr(0).trim(); 
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
                 
                //Se procesa y se envía la petición hacia el proceso solicitante
                serverPS.send(this.procesarPeticion(peticionAux));
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
            publicarRespuesta(p,1);
            return "Devuelto";
        }else if(p.getTipo().getNumSolicitud() == 2){
            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MMMM/yyyy");
            publicarRespuesta(p,2);
            return "Nueva fecha de entrega " + p.getFecha().plusDays(7).format(dateFormat);
        }else if(p.getTipo().getNumSolicitud() == 3){
            return procesarPrestamo(p);
        }
        return null;
    }

    private String procesarPrestamo(Peticion peticion){
        String prestamoStr = "";
        try{
            String msgSend = crearMensajePeticion(peticion);
            clientAP.send(msgSend);
            prestamoStr = clientAP.recvStr(0).trim();
             
            //Recibe la respuesta del gestor de carga
            System.out.println(prestamoStr);
        } catch (Exception e){
            System.err.println("No se pudo recibir el mensaje " + e.getMessage());
            System.exit(-1);
        }
        return prestamoStr;
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