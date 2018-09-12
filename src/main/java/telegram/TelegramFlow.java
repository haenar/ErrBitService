package telegram;

import java.io.IOException;
import java.net.*;

import static java.lang.String.format;


public class TelegramFlow {
    String PROXY = "148.251.151.207";
    String USER="sockduser";
    String PASS="J$ngl1Bells";

    public void messageSend(String text){
        try {

            String url = format("https://api.telegram.org/bot643973718:AAFdUtXmpAVxpXADEHM8PSdNOdL7OZug9OQ/sendMessage?chat_id=-317464695&parse_mode=Markdown&text=%s",
                    text.replace("\n",". "));

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
        catch (IOException e) {

        }
    }
}

