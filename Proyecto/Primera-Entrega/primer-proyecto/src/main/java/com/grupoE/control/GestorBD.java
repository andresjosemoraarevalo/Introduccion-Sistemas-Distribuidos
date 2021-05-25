package com.grupoE.control;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import com.grupoE.entity.Peticion;
import com.grupoE.entity.Libro;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class GestorBD {
    private ZContext context;
    public static ZMQ.Socket client_dev;
    public static ZMQ.Socket client_pres;
    public static ZMQ.Socket client_rnv;
    private HashMap<Integer,Libro> libros = new HashMap<>();
    private BufferedReader br;

    public GestorBD(String opcion){
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
            client_dev = context.createSocket(SocketType.SUB);
            int portDEV = 8886;
            //Ata el socket a el puerto
            //Usando localhost
            client_dev.connect("tcp://"+ direccion + ":" + portDEV);
            //client_dev.connect("tcp://25.92.125.22:" + port);
            String filter = "1";
            client_dev.subscribe(filter.getBytes(Charset.forName("UTF-8")));

            //Crea socket tipo SUB
            client_pres = context.createSocket(SocketType.SUB);
            int portPRES = 9996;
            //Ata el socket a el puerto
            //Usando localhost
            client_pres.connect("tcp://"+ direccion + ":" + portPRES);
            //client_pres.connect("tcp://25.92.125.22:" + port);
            filter = "3";
            client_pres.subscribe(filter.getBytes(Charset.forName("UTF-8")));
        
            //Crea socket tipo SUB
            client_rnv = context.createSocket(SocketType.SUB);
            int portRNV = 9886;
            //Ata el socket a el puerto
            //Usando localhost
            client_rnv.connect("tcp://"+ direccion + ":" + portRNV);
            filter = "2";
            //client_rnv.connect("tcp://25.92.125.22:" + port);
            client_rnv.subscribe(filter.getBytes(Charset.forName("UTF-8")));
            
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
        GestorBD ar = new GestorBD(args[0]);
        // Envia las peticiones al servidor con el patrón requesr-reply
        ar.leerCambios();
        ar.cargarLibros();
        ar.persistirLibros();
    }
    public void leerCambios(){ 
        try{
            Thread hilo = new MsgDevolucion("proceso 1");
            Thread hilo2 = new MsgPrestamo("proceso 2");
            Thread hilo3 = new MsgRenovacion("proceso 2");
            
            hilo.start();
            hilo2.start();
            hilo3.start();

        } catch (Exception e){
            System.err.println("No se pudo recibir el mensaje" + e.getMessage());
            System.exit(-1);
        }
    }
    public void cargarLibros(){
        String actual = System.getProperty("user.dir");
        //SE DEBE CAMBIAR DEPENDIENDO SI ES WINDOWS O LINUX
        // - PARA LINUX
        //String PATH_CSV = actual+"/Primera-Entrega/primer-proyecto/src/DB/libros.csv"; // Si se va a leer peticiones
        String PATH_CSV = actual+"/Primera-Entrega/primer-proyecto/src/DB/libros.csv"; // Si se va a leer peticiones 2
        // - PARA WINDOWS
        //PATH_CSV.replace('/', '\\'); // QUITAR COMENTARIO
        String line = "";
        try{
            br = new BufferedReader(new FileReader(PATH_CSV));//se lee el archivo
            while((line = br.readLine()) != null){
                String[] peticionR = line.split(","); // Se separa la linea por comas
                Libro l = new Libro();
                l.setIdLibro(Integer.parseInt(peticionR[0].replace(" ", "")));
                l.setTitulo(peticionR[1].replace(" ", ""));
                l.setAutor(peticionR[2].replace(" ", ""));
                l.setISBN(peticionR[3].replace(" ", ""));
                l.setEditorial(peticionR[4].replace(" ", ""));
                l.setIdioma(peticionR[5].replace(" ", ""));
                // Da formato a la fecha
                DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MMMM/yyyy"); 
                // Obtiene la fecha de la petición y le da formato
                l.setCategoria(peticionR[6].replace(" ", ""));
                l.setEstado(Boolean.valueOf(peticionR[7].replace(" ", "")));
                l.setNumEjemplares(Integer.parseInt(peticionR[8].replace(" ", "")));
                l.setFechaPublicacion(LocalDate.parse(peticionR[9].replace(" ", ""), dateFormat));
                libros.put(l.getIdLibro(), l);
            }
        }catch(IOException e){
            System.err.println("No se pudo leer el archivo :(" + "\n" + e.getMessage());
            System.exit(-1);
        }
    }
    public void persistirLibros(){
        String actual = System.getProperty("user.dir");
        try (PrintWriter writer = new PrintWriter(new File(actual+"/Primera-Entrega/primer-proyecto/src/DB/libros.csv"))) {
            StringBuilder sb = new StringBuilder();
            for (Integer n : libros.keySet()) {
                sb.append(libros.get(n).toString());
                sb.append('\n');
            }
            writer.write(sb.toString());
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }
}

class MsgDevolucion extends Thread{
    public MsgDevolucion(String msg){
        super(msg);
    }
    public void run(){
        while(true){
            String str_dev = GestorBD.client_dev.recvStr(0).trim();
            String peticionStr = str_dev;
            // Separa la palabra por espacios
            StringTokenizer strTok = new StringTokenizer(peticionStr, " ");
            //Se obtiene topico
            Integer.parseInt(strTok.nextToken());
            // Se obtiene el ID del libro
            int idLibro = Integer.parseInt(strTok.nextToken());
            // Se obtiene el tipo de proceso
            int tipo = Integer.parseInt(strTok.nextToken());
            // Se obtiene la fecha del proceso
            String fecha = strTok.nextToken();
            //Se arma la petición
            Peticion peticionAux = new Peticion(idLibro,tipo,fecha);
            //Se muestra en consola para saber en cual petición va
            System.out.println("BD"+peticionAux.toString());
        }
    }
}
class MsgPrestamo extends Thread{
    public MsgPrestamo(String msg){
        super(msg);
    }
    public void run(){
        while(true){
            String str_pres = GestorBD.client_pres.recvStr(0).trim();
            String peticionStr = str_pres;
            // Separa la palabra por espacios
            StringTokenizer strTok = new StringTokenizer(peticionStr, " ");
            //Se obtiene topico
            Integer.parseInt(strTok.nextToken());
            // Se obtiene el ID del libro
            int idLibro = Integer.parseInt(strTok.nextToken());
            // Se obtiene el tipo de proceso
            int tipo = Integer.parseInt(strTok.nextToken());
            // Se obtiene la fecha del proceso
            String fecha = strTok.nextToken();
            //Se arma la petición
            Peticion peticionAux = new Peticion(idLibro,tipo,fecha);
            //Se muestra en consola para saber en cual petición va
            System.out.println("BD"+peticionAux.toString());
        }
    }
}
class MsgRenovacion extends Thread{
    public MsgRenovacion(String msg){
        super(msg);
    }
    public void run(){
        while(true){
            String str_rnv = GestorBD.client_rnv.recvStr(0).trim();
            String peticionStr = str_rnv;
            // Separa la palabra por espacios
            StringTokenizer strTok = new StringTokenizer(peticionStr, " ");
            //Se obtiene topico
            Integer.parseInt(strTok.nextToken());
            // Se obtiene el ID del libro
            int idLibro = Integer.parseInt(strTok.nextToken());
            // Se obtiene el tipo de proceso
            int tipo = Integer.parseInt(strTok.nextToken());
            // Se obtiene la fecha del proceso
            String fecha = strTok.nextToken();
            //Se arma la petición
            Peticion peticionAux = new Peticion(idLibro,tipo,fecha);
            //Se muestra en consola para saber en cual petición va
            System.out.println("BD"+peticionAux.toString());
        }
    }
}
