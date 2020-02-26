package telegram;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import jira.MyJiraClientFlow;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import static java.lang.String.format;


public class SlackFlow {

    public static boolean sendResulttoSlack(HashMap<String, String> map, Boolean jiraTicketON, String host, String urlErrorFromErrbitBackEnd, String assignee) {
        String url = "https://hooks.slack.com/services/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";
        String requestJson = "";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("charset", "utf-8");
        headers.add("Content-Encoding", "utf-8");
        headers.add("Transfer-Encoding", "utf-8");
        headers.add("Accept-Charset", "utf-8");

        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        if (map.get("appName").contains("sever-public-api"))
            if (!map.get("environment").contains("prod"))
                return false;

        try {
            String jira = "Время: " + map.get("problemTime") +
                    "\\n" + "Количество ошибок: " + map.get("problemCount") +
                    "\\n" + "Проблема: " + map.get("problemMessage")
                    .replace("{", "")
                    .replace("}", "")
                    .replace("\"", "") +
                    "\\n" + "Фронт: " + map.get("url") +
                    "\\n" + "Ошибка backend: " + urlErrorFromErrbitBackEnd;


            String jiraTicket = null;
            if (jiraTicketON) {
                MyJiraClientFlow j = new MyJiraClientFlow();
                jiraTicket = j.jiraCreateIssue(jira, assignee, "SEVER. " + map.get("appName").toUpperCase());
                jiraTicket = "\n`Jira ticket:` " + jiraTicket.split(",")[1].split("\"")[3];
                jiraTicket = "\n" + format("[%s](https://jjunglejobs.atlassian.net/browse/%s)", jiraTicket, jiraTicket);
            } else {
                String urlJira = format("%snewticket?%s", host, jira
                        .replace("\\n", "|")
                        .replace(": ", "=")
                        .replace("(", "|")
                        .replace(")", "|"));

                jiraTicket = "\n" + format("[Создать тикет](%s)", urlJira);
            }

            String s = "\uD83D\uDE40*[ERRBIT-ACHTUNG]* " + map.get("appName").toUpperCase() +
                    "\n" + "`Количество ошибок:` " + map.get("problemCount") +
                    "\n" + "`Проблема:` " + map.get("problemMessage") +
                    "\n" + "`Фронт:` " + map.get("url") +
                    "\n" + "`Ошибка backend:` " + urlErrorFromErrbitBackEnd +
                    jiraTicket;

            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            JsonFactory factory = new JsonFactory();
            JsonGenerator jsn = factory.createGenerator(bout);
            jsn.writeStartObject();
            jsn.writeBooleanField("mrkdwn", true);
            jsn.writeStringField("text", s );
            jsn.writeEndObject();
            requestJson = bout.toString();
            jsn.close();
            bout.close();
            requestJson = bout.toString();
        }
        catch (Exception e) {

        };

        ResponseEntity<String> response = postRequestSend(url, headers, requestJson);
        boolean flag = false;
        if (response.getStatusCode().toString().equals("200"))
            flag = true;

        return flag;
    }

    private static ResponseEntity<String> postRequestSend(String url, HttpHeaders headers, String requestJson) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            HttpEntity<String> entity = new HttpEntity<String>(requestJson, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            return response;
        }
        catch (Exception ex){
            System.out.println("Exception: " + ex.toString() + "  " + requestJson + "  " + url);
        }

        return null;
    }



}

