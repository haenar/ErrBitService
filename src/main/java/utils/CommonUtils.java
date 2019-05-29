package utils;

import com.sun.net.httpserver.HttpExchange;
import json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import sql.SqlOperations;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;

import java.util.*;

import static java.lang.Integer.parseInt;
import static java.lang.Thread.sleep;


public class CommonUtils {



    public static void serverResponse(String s, HttpExchange t) throws IOException {
        t.sendResponseHeaders(200, s.length());
        OutputStream os = t.getResponseBody();
        os.write(s.getBytes());
        os.close();
    }

    public static Queue workWithQueue(HashMap<String, String> map, Queue<List<String[]>> queue){
        String[] s = { map.get("problemTime"), map.get("url")};
        List<String[]> list = new ArrayList<String[]>();
        list.add(s);
        queue.add(list);
        if (queue.size() > 100) queue.poll();
        return queue;
    }

    public static String queueSynchronize (HashMap<String, String> map, Queue<List<String[]>> queue) {
        String date = map.get("problemTime");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        try {
            LocalDateTime dateFromFront = LocalDateTime.parse(date, formatter);
            sleep(2000);
            for (List<String[]> l : queue) {
                for (String[] s : l) {
                    LocalDateTime dateFromBack = LocalDateTime.parse(s[0], formatter);
                    Duration duration = Duration.between(dateFromFront, dateFromBack);
                    if (duration.toMillis() <= 800 && duration.toMillis() >= -800)
                        return s[1];
                }
            }
        } catch (Exception ex) {
            System.out.println("queueSynchronize - " + ex.getMessage());
        }

        return "-";
    }


    public static HashMap<String, String> jsonParse(String line) throws IOException {
        line = URLDecoder.decode(line, "UTF-8").replace("problem=", "");
        JSONObject obj = new JSONObject(line);
        String problemTime = obj.getString("last_notice_at");
        String problemMessage = obj.getString("message");
        String url = obj.getString("url").replace("example.com", "junglejobs.ru");
        String appName = obj.getString("app_name");
        String environment = obj.getString("environment");
        Integer problemCount = obj.getInt("notices_count");

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("problemTime", problemTime);
        map.put("problemMessage", problemMessage);
        map.put("problemCount", problemCount.toString());
        map.put("url", url);
        map.put("environment", environment);
        map.put("appName", appName);

        return map;
    }

    public static Boolean errorMessageFilter(HashMap<String, String> map){

        if (map.get("problemMessage").contains("\"status\":422") ||
                map.get("problemMessage").contains("\"status\":403"))
            return false;

        if (map.get("problemMessage").contains("\"status\":502") ||
                map.get("problemMessage").contains("\"status\":404") ||
                parseInt(map.get("problemCount")) <= 20) {
            return SqlOperations.checkThatMessageIsFiltered(map.get("problemMessage"));
        }

        return false;
    }



}
