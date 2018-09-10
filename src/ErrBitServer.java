import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;
import telegram.TelegramFlow;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.HashMap;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;

public class ErrBitServer {

    private static boolean serviceIsON = false;
    private static String version = "1.01";

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(1111), 0);
        server.createContext("/work", new MyHandler());
        server.createContext("/start", new MyHandlerStart());
        server.createContext("/stop", new MyHandlerStop());
        server.createContext("/state", new MyHandlerState());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if (serviceIsON) {
                BufferedReader br = new BufferedReader(new InputStreamReader(t.getRequestBody()));
                analyseString(br.readLine());
                br.close();
                serverResponse("Telegram sending - OK", t);
            }
            else
                serverResponse("Service is OFF", t);
        }
    }

    static class MyHandlerStart implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            serverResponse("Service is ON", t);
            serviceIsON = true;
        }
    }

    static class MyHandlerStop implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            serverResponse("Service is OFF", t);
            serviceIsON = false;
        }
    }

    static class MyHandlerState implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            serverResponse(format("<html><body>Service working: %s;<br>Version: %s;<body></html>",
                    serviceIsON,
                    version), t);
        }
    }


    private static void serverResponse(String s, HttpExchange t) throws IOException{
        t.sendResponseHeaders(200, s.length());
        OutputStream os = t.getResponseBody();
        os.write(s.getBytes());
        os.close();
    }


    private static HashMap<String,String> jsonParse (String line) throws IOException{
        line = URLDecoder.decode(line, "UTF-8").replace("problem=", "");
        JSONObject obj = new JSONObject(line);
        String problemTime = obj.getString("last_notice_at");
        String problemMessage = obj.getString("message");
        Integer problemCount = obj.getInt("notices_count");

        HashMap<String,String> map = new HashMap<>();
        map.put("problemTime", problemTime);
        map.put("problemMessage", problemMessage);
        map.put("problemCount", problemCount.toString());

        return map;
    }

    private static void analyseString(String jsonString)throws IOException {
        HashMap<String,String> map;
        map = jsonParse(jsonString);

        if (map.get("problemMessage").contains("502") ||
            map.get("problemMessage").contains("404") ||
            parseInt(map.get("problemCount")) <= 20)
                sendResult(map);
    }

    private static void sendResult(HashMap<String,String> map)
    {
        String s = "\uD83D\uDE40*[ERRBIT-ACHTUNG]* Frontend-production" +
                    "%0A`Время:` " + map.get("problemTime") +
                    "%0A`Количество ошибок:` " +  map.get("problemCount") +
                    "%0A`Проблема:` " + map.get("problemMessage");
        TelegramFlow t = new TelegramFlow();
        t.messageSend(s);
    }
}


