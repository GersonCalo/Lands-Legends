import java.io.*;
import java.net.*;

public class Server {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(12321);
        System.out.println("Servidor iniciado en puerto 12321");

        while (true) {
            try (Socket clientSocket = serverSocket.accept();
                 BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                System.out.println("Cliente conectado: " + clientSocket.getInetAddress());

                // Leer datos y validar
                String data = in.readLine();
                if (data == null || data.isEmpty()) {
                    System.out.println("Datos vacíos o nulos recibidos");
                    out.println("error|Datos inválidos");
                    continue;
                }

                System.out.println("Datos recibidos: " + data);

                // Procesar datos
                String[] parts = data.split("\\|");
                if (parts.length != 3) {
                    System.out.println("Formato incorrecto");
                    out.println("error|Formato incorrecto");
                    continue;
                }

                String email = parts[0];
                String puntuacion = parts[1];
                String fecha = parts[2];

                System.out.println("Email: " + email);
                System.out.println("Puntuación: " + puntuacion);
                System.out.println("Fecha: " + fecha);
                // LLamar a la api 
                String response = callApi(email, fecha, Integer.parseInt(puntuacion));

                // Respuesta al cliente
                //out.println("okey");
                System.out.println(response);
                if(response.equals("200")){
                    out.println("okey");
                }

            } catch (IOException e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }
    private static String callApi(String email, String fecha, int puntuacion) {
        try {
            System.out.println("Inicio llamada a la API");
            // Codificar el correo electrónico
            String encodedEmail = URLEncoder.encode(email, "UTF-8");
            // Asegúrate de que la fecha esté en el formato correcto
            String formattedDate = fecha; // Si ya viene en el formato correcto (ejemplo: "2025-02-03T09:17:26.997572")
    
            // Construir la URL correctamente sin usar {} para datePlayed
            String requestUrl = String.format("http://localhost:1111/game/insertGameRecord/%s/%s/%d",
                    encodedEmail, formattedDate, puntuacion);
    
            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Accept", "*/*"); // Agregar encabezado accept
    
            // No es necesario enviar un cuerpo vacío si todos los parámetros están en la URL
            connection.connect();  // Conectar a la API
    
            // Leer la respuesta
            int responseCode = connection.getResponseCode();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
    
            reader.close();
            return String.valueOf(responseCode);
    
        } catch (Exception e) {
            return "error|Error al llamar a la API: " + e.getMessage();
        }
    }
}