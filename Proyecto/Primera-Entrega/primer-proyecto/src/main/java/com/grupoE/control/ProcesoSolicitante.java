package com.grupoE.control;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

import com.grupoE.entity.TipoPeticion;
import com.grupoE.entity.Peticion;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class ProcesoSolicitante {
    private ZContext context;
    private ZMQ.Socket client;
    private BufferedReader br;

    public ProcesoSolicitante(String opcion){
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
            client = context.createSocket(SocketType.REQ);
            int port = 5556;
            //Ata el socket a el puerto
            //Usando localhost
            client.connect("tcp://"+ direccion + ":" + port);
            
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
        ProcesoSolicitante ps = new ProcesoSolicitante(args[0]);
        // Envia las peticiones al servidor con el patrón requesr-reply 
        ps.leerPeticiones();// Lee las peticiones del archivo
    }

    /** 
     * Lee las peticiones del archivo "peticiones.csv" y las envía al Gestor de Carga
    */
    public void enviarPeticiones(Peticion peticion){ 
        try{
            // Da formato a la fecha
            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MMMM/yyyy"); 
            // Obtiene la fecha de la petición y le da formato
            String date = peticion.getFecha().format(dateFormat).toString();
            // Arma el mensaje que se va a enviar
            String msgSend = String.format("%s %s %s",peticion.getIdLibro(), peticion.getTipo().getNumSolicitud(), date);
            //Se envía el mensaje
            client.send(msgSend);
            Thread.sleep(1000);
            //Recibe la respuesta del gestor de carga
            String message = client.recvStr(0).trim();
            System.out.println(message);
        } catch (Exception e ){
            System.err.println("No se pudieron enviar las peticiones" + "\n" + e.getMessage());
            System.exit(-1);
        }
    }
    
    /**
     * Función que retorna una lista de peticiones que son leídas de un archivo
     * @return Lista de Peticiones dado un archivo .CSV
     */
    public void leerPeticiones(){
        String actual = System.getProperty("user.dir");
        //SE DEBE CAMBIAR DEPENDIENDO SI ES WINDOWS O LINUX
        // - PARA LINUX
        //String PATH_CSV = actual+"/src/main/java/com/grupoE/peticiones/peticiones.csv"; // Si se va a leer peticiones
        String PATH_CSV = actual+"/primer-proyecto/src/main/java/com/grupoE/peticiones/peticiones_2.csv"; // Si se va a leer peticiones 2
        // - PARA WINDOWS
        //PATH_CSV.replace('/', '\\'); // QUITAR COMENTARIO
        String line = "";
        try{
            br = new BufferedReader(new FileReader(PATH_CSV));//se lee el archivo
            while((line = br.readLine()) != null){
                String[] peticionR = line.split(","); // Se separa la linea por comas
                Peticion p = new Peticion();
                TipoPeticion tipo = p.buscarPeticion(Integer.parseInt(peticionR[0]));//dado un numero se busca que tipo de petición es 
                if(tipo!=null){//si el tipo es correcto
                    p.setTipo(tipo);
                    p.setIdLibro(Integer.parseInt(peticionR[1].substring(1)));//se obtiene de la linea el id del libro
                    enviarPeticiones(p);
                }
            }
        }catch(IOException e){
            System.err.println("No se pudo leer el archivo :(" + "\n" + e.getMessage());
            System.exit(-1);
        }
    }
}