package dev.westernpine.site.manager;

import dev.westernpine.bettertry.Try;
import dev.westernpine.lib.object.Client;
import dev.westernpine.pipeline.message.Message;
import dev.westernpine.pipeline.message.MessageType;
import dev.westernpine.site.Site;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Manager {

    private ManagerWebsocket websocket;

    public Manager(String url, String managerToken) throws URISyntaxException {
        URI uri = new URI(url);
        Map<String, String> headers = Map.of("Authorization", managerToken);
        websocket = new ManagerWebsocket(this, uri, headers);
    }

    public void received(Message message) {
        if(message.isRequest()) {
            switch (message.read().toString()) {
                case "user.premium":
                    String userId = message.read().toString();
                    Try<Client> client = Site.clientManager.getByUser(userId);
                    websocket.getPipeline().send(message.toRespone().write(client.map(Client::getPremiumStatus).map(Client.Status::isActive).orElse(false)));
                    break;
                default:
                    websocket.getPipeline().send(message.toRespone().write("error").write("Unknown request!"));
                    break;
            }
        } else if (message.isMessage()) {
            switch (message.read().toString()) {
                default:
                    StringBuilder builder = new StringBuilder();
                    while(message.hasNext())
                        builder.append(message.read().toString() + "\n");
                    System.out.println("Received Unknown Message: " + builder.toString());
                    break;
            }
        }
    }

    public void updatePremium(String userId, boolean isPremium) {
        websocket.getPipeline().send(new Message().write("user.premium.update").write(userId).write(isPremium));
    }
}
