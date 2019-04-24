package jira;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import com.sun.jersey.core.util.Base64;
import org.mortbay.util.MultiPartWriter;

import java.io.*;
import java.lang.String;


public class MyJiraClientFlow {
    String token = "YS52ZXJzaGluaW5AanVuZ2xlam9icy5ydTplT2UzOW5hWk93aHQ4dUJucmJldjk4ODg=";
    String jiraUrl = "https://jjunglejobs.atlassian.net";

    public String jiraCreateIssue(String description, String assignee){
        String result = "";

        String summary = "JungleJobs - Frontend-production. " + description.split("message:")[1].split("Ошибка")[0];

        ClientConfig cc = new DefaultClientConfig();
        cc.getClasses().add(MultiPartWriter.class);
        Client client = Client.create(cc);

        WebResource webResource = client.resource(jiraUrl + "/rest/api/latest/issue");

        String data = "{\"fields\":{\"project\":{\"key\":\"SUP\"},\"summary\":\""
                + summary + "\",\"description\": \""
                + description + "\",\"assignee\":{\"name\":\""
                + assignee + "\"},\"labels\":[\"support\"],\"issuetype\":{\"name\":\"Bug\"}}}";


        ClientResponse response = webResource.header("Authorization", "Basic " + token).type("application/json").accept("application/json").post(ClientResponse.class, data);
        BufferedReader inputStream = new BufferedReader(new InputStreamReader(response.getEntityInputStream()));
        String line;

        try {
            while ((line = inputStream.readLine()) != null)
                result = line;
        } catch (Exception ex) {}

        return result;
    }
}
