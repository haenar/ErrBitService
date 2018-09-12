package jira;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import java.io.*;
import java.lang.String;

import static java.lang.String.format;


public class MyJiraClientFlow {

    String username = "a.vershinin@junglejobs.ru";
    String password = "Bezenchuk01";
    String jiraUrl = "https://jjunglejobs.atlassian.net";

    public String jiraCreateIssue(String summary, String description) {

        String result = "";
        try {
            Client client = Client.create();
            WebResource webResource = client.resource(jiraUrl + "/rest/api/latest/issue");

            String data = format("{\"fields\":{\"project\":{\"key\":\"EBS\"},\"summary\":\"%s\",\"description\": \"%s\",\"issuetype\":{\"name\":\"Bug\"}}}", summary, description);

            String auth = new String(Base64.encode((username + ":" + password).getBytes()));
            ClientResponse response = webResource.header("Authorization", "Basic " + auth).type("application/json").accept("application/json").post(ClientResponse.class, data);
            int statusCode = response.getStatus();

            BufferedReader inputStream = new BufferedReader(new InputStreamReader(response.getEntityInputStream()));
            String line;
            while ((line = inputStream.readLine()) != null)
                result = line;

        } catch (Exception e) {}

        return result;
    }
}
