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

    String username = "a.vershinin@junglejobs.ru";
    String password = "Bezenchuk01";
    String jiraUrl = "https://jjunglejobs.atlassian.net";

    public String jiraCreateIssue(String summary, String description, String assignee) throws  IOException{
        String result = "";

        ClientConfig cc = new DefaultClientConfig();
        cc.getClasses().add(MultiPartWriter.class);
        Client client = Client.create(cc);

        WebResource webResource = client.resource(jiraUrl + "/rest/api/latest/issue");

        String data = "{\"fields\":{\"project\":{\"key\":\"EBS\"},\"summary\":\""
                + summary + "\",\"description\": \""
                + description + "\",\"assignee\":{\"name\":\""
                + assignee + "\"},\"labels\":[\"support\"],\"issuetype\":{\"name\":\"Bug\"}}}";

        String auth = new String(Base64.encode((username + ":" + password).getBytes()));
        ClientResponse response = webResource.header("Authorization", "Basic " + auth).type("application/json").accept("application/json").post(ClientResponse.class, data);
        BufferedReader inputStream = new BufferedReader(new InputStreamReader(response.getEntityInputStream()));
        String line;
        while ((line = inputStream.readLine()) != null)
            result = line;

        return result;
    }
}
