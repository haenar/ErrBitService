package errbitserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import jira.MyJiraClientFlow;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static java.lang.String.format;
import static telegram.SlackFlow.sendResulttoSlack;
import static telegram.TelegramFlow.*;
import telegram.SlackFlow.*;
import static utils.CommonUtils.*;


public class ErrBitServer {

    private static boolean serviceIsON = false;
    private static boolean jiraTicketON = false;
    private static String version = "4.04";
    private static final String HOST = "http://213.232.228.186:1111/";
    private static Queue<List<String[]>> queue = new LinkedList<List<String[]>>();
    private static String assigneeJJ = "n.kozlov";
    private static String assigneeSever = "a.stefanenkov";

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(1111), 5);
        server.createContext("/inputFront", new MyHandlerFront());
        server.createContext("/inputBack", new MyHandlerBack());
        server.createContext("/start", new MyHandlerStart());
        server.createContext("/stop", new MyHandlerStop());
        server.createContext("/state", new MyHandlerState());
        server.createContext("/newticket", new MyHandlerNewJira());
        server.createContext("/jira", new MyHandlerJira());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    static class MyHandlerFront implements HttpHandler  {
        public void handle(HttpExchange t) throws IOException {
                if (serviceIsON) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(t.getRequestBody()));
                    HashMap<String, String> map = jsonParse(br.readLine());
                    br.close();

                    if (errorMessageFilter(map)){
                        if (map.get("appName").toLowerCase().contains("jj prod"))
                            sendResulttoTelegram(map, jiraTicketON, HOST, queueSynchronize(map, queue), assigneeJJ);
                        else
                            sendResulttoSlack(map, jiraTicketON, HOST, queueSynchronize(map, queue), assigneeSever);
                        serverResponse("Message sending - OK", t);
                        if (jiraTicketON) serverResponse("Jira ticket creation - OK", t);
                    }
                    else serverResponse("Message sending - NOK", t);
                } else
                    serverResponse("Service is OFF", t);


        }
    }

    static class MyHandlerBack implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            if (serviceIsON) {
                BufferedReader br = new BufferedReader(new InputStreamReader(t.getRequestBody()));
                HashMap<String, String> map = jsonParse(br.readLine());
                br.close();
                if (map.get("appName").toLowerCase().contains("sever-backend-prod")) {
                    if (errorMessageFilter(map))
                        sendResulttoSlack(map, jiraTicketON, HOST, queueSynchronize(map, null), assigneeSever);
                }
                else
                    queue = workWithQueue(map, queue);
                serverResponse("Server Response - OK", t);
            } else
                serverResponse("Service is OFF", t);


        }
    }

    static class MyHandlerStart implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            serverResponse("Service is ON", t);
            serviceIsON = true;
        }
    }

    static class MyHandlerStop implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            serverResponse("Service is OFF", t);
            serviceIsON = false;
            jiraTicketON = false;
        }
    }

    static class MyHandlerState implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            serverResponse(format("<html><body>Service working: %s;<br>Jira ticket creation: %s;<br>Version: %s;<body></html>",
                    serviceIsON,
                    jiraTicketON,
                    version), t);
        }
    }

    static class MyHandlerJira implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            if (jiraTicketON) jiraTicketON = false;
            else jiraTicketON = true;
            serverResponse("Jira ticket creation: " + jiraTicketON, t);
        }
    }

    static class MyHandlerNewJira implements HttpHandler {
        public void handle(HttpExchange t) throws IOException  {
                String jiraText = URLDecoder.decode(t.getRequestURI().toString(), "UTF-8");
                jiraText = jiraText.split("\\?")[1].replace("|", "\\n").replace("=", ": ");

                MyJiraClientFlow j = new MyJiraClientFlow();
                String jiraTicket = j.jiraCreateIssue(jiraText, assigneeJJ, "New Ticket Creation From Errbit-Achtung Service");

                jiraTicket = "https://jjunglejobs.atlassian.net/browse/" + jiraTicket.split(",")[1].split("\"")[3];
                serverResponse(format("<html><body><script type=\"text/javascript\">window.location.replace('%s')</script><body></html>", jiraTicket), t);
        }
    }

}


