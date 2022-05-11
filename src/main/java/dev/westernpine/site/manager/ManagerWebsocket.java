package dev.westernpine.site.manager;

import dev.westernpine.bettertry.Try;
import dev.westernpine.pipeline.Pipeline;
import dev.westernpine.site.Site;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Map;

public class ManagerWebsocket extends WebSocketClient {

    private Manager manager;

    private Pipeline pipeline;

    public ManagerWebsocket(Manager manager, URI uri, Map<String, String> headers) {
        super(uri, headers);
        this.manager = manager;
        this.pipeline = new Pipeline(this::send, manager::received);
        Try.to(this::connect);
    }

    public Manager getManager() {
        return manager;
    }

    public Pipeline getPipeline() {
        return pipeline;
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        System.out.println("Connection to manager established!");
    }

    @Override
    public void onMessage(String s) {
        this.pipeline.received(s);
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        System.out.println("Disconnected from manager. (%d) %s [Remote: %b]".formatted(i, s, b));
        Site.scheduler.runLater(() -> {
            System.out.println("Reconnecting to manager...");
            Try.to(this::reconnect);
        }, 5000L);
    }

    @Override
    public void onError(Exception e) {
        e.printStackTrace();
        System.out.println(e.getMessage());
        System.out.println("An error occurred with the connection to manager!");
    }
}
