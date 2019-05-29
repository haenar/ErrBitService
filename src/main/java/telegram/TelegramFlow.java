package telegram;

import jira.MyJiraClientFlow;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;

import static java.lang.String.format;


public class TelegramFlow {
    String PROXY = "148.251.151.207";
    String USER="sockduser";
    String PASS="J$ngl1Bells";

    private void messageSend(String text){
        String url = format("https://api.telegram.org/bot643973718:AAFdUtXmpAVxpXADEHM8PSdNOdL7OZug9OQ/sendMessage?chat_id=-317464695&parse_mode=Markdown&text=%s",
                text);
        try {
            Authenticator.setDefault(new Authenticator(){
                protected PasswordAuthentication getPasswordAuthentication(){
                    PasswordAuthentication p = new PasswordAuthentication(USER, PASS.toCharArray());
                    return p;
                }
            });
            URL u = new URL(url);
            Proxy p = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(PROXY, 2024));
            HttpURLConnection connection = (HttpURLConnection)u.openConnection(p);
            connection.connect();
            connection.getResponseMessage();
            connection.disconnect();
        }
        catch (IOException ex) {
            System.out.println("Telegram messageSend - " + ex.getMessage() + " " + url);
        }
    }

    public static void sendResulttoTelegram(HashMap<String, String> map, Boolean jiraTicketON, String host, String urlErrorFromErrbitBackEnd, String assignee) {

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
            jiraTicket = j.jiraCreateIssue(jira, assignee, "JungleJobs - Frontend-production");
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

        if (urlErrorFromErrbitBackEnd.equals("-"))
            urlErrorFromErrbitBackEnd = "";

        String telegram = "\uD83D\uDE40*[ERRBIT-ACHTUNG]* " + map.get("appName").toUpperCase() +
                "%0A`Время:` " + map.get("problemTime") +
                "%0A`Количество ошибок:` " + map.get("problemCount") +
                "%0A`Проблема:` " + map.get("problemMessage")
                .replace("(", "|")
                .replace(")", "|")
                .replace("[", "‖")
                .replace("]", "‖") +
                "%0A`Фронт:` [ссылка](" + map.get("url") + ")" +
                "%0A`Ошибка backend:` [ссылка](" + urlErrorFromErrbitBackEnd + ")" +
                jiraTicket;

        TelegramFlow t = new TelegramFlow();
        t.messageSend(telegram);
    }
}

