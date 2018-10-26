package utils;

import com.sun.net.httpserver.HttpExchange;
import jira.MyJiraClientFlow;
import json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import sql.SqlOperations;
import telegram.TelegramFlow;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;

import java.util.*;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
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
                    if (duration.toMillis() <= 500 && duration.toMillis() >= -500)
                        return s[1].replace("example.com", "junglejobs.ru");
                }
            }
        } catch (Exception ex) {
            System.out.println("queueSynchronize - " + ex.getMessage());
        }

        return "";
    }


    public static HashMap<String, String> jsonParse(String line) throws IOException {
        line = URLDecoder.decode(line, "UTF-8").replace("problem=", "");
        JSONObject obj = new JSONObject(line);
        String problemTime = obj.getString("last_notice_at");
        String problemMessage = obj.getString("message");
        String url = obj.getString("url");
        String appName = obj.getString("app_name");
        Integer problemCount = obj.getInt("notices_count");

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("problemTime", problemTime);
        map.put("problemMessage", problemMessage);
        map.put("problemCount", problemCount.toString());
        map.put("url", url);
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

    public static void sendResult(HashMap<String, String> map, Boolean jiraTicketON, String host, String urlErrorFromErrbitBackEnd) {
        String jira = "Время: " + map.get("problemTime") +
                "\\n" + "Количество ошибок: " + map.get("problemCount") +
                "\\n" + "Проблема: " + map.get("problemMessage")
                .replace("{", "")
                .replace("}", "")
                .replace("\"", "") +
                "\\n" + "Ошибка backend: " + urlErrorFromErrbitBackEnd;

        String jiraTicket = null;
        if (jiraTicketON) {
            MyJiraClientFlow j = new MyJiraClientFlow();
            jiraTicket = j.jiraCreateIssue("JungleJobs - Frontend-production", jira, "a.khivin");
            jiraTicket = "%0A`Jira ticket:` " + jiraTicket.split(",")[1].split("\"")[3];
            jiraTicket = "%0A" + format("[%s](https://jjunglejobs.atlassian.net/browse/%s)", jiraTicket, jiraTicket);
        } else {
            String url = format("%snewticket?%s", host, jira
                    .replace("\\n", "|")
                    .replace(": ", "=")
                    .replace("(", "|")
                    .replace(")", "|"));

            jiraTicket = "%0A" + format("[Создать тикет](%s)", url);
        }


        String telegram = "\uD83D\uDE40*[ERRBIT-ACHTUNG]* " + map.get("appName").toUpperCase() +
                "%0A`Время:` " + map.get("problemTime") +
                "%0A`Количество ошибок:` " + map.get("problemCount") +
                "%0A`Проблема:` " + map.get("problemMessage")
                            .replace("(", "|")
                            .replace(")", "|")
                            .replace("[", "‖")
                            .replace("]", "‖") +

                "%0A`Ошибка backend:` [ссылка](" + urlErrorFromErrbitBackEnd + ")" +
                jiraTicket;

        TelegramFlow t = new TelegramFlow();
        t.messageSend(telegram);
    }

}
