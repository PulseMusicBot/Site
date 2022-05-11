package dev.westernpine.site;

import com.google.gson.Gson;
import com.stripe.Stripe;
import com.stripe.net.OAuth;
import dev.westernpine.eventapi.EventManager;
import dev.westernpine.lib.object.Scheduler;
import dev.westernpine.site.client.ClientManager;
import dev.westernpine.site.manager.Manager;
import dev.westernpine.site.properties.IdentityProperties;
import dev.westernpine.site.properties.SystemProperties;
import dev.westernpine.site.routes.InternalServerError;
import dev.westernpine.site.routes.NotFound;
import dev.westernpine.site.routes.StripeWebhook;
import freemarker.template.Configuration;
import io.mokulu.discord.oauth.DiscordAPI;
import io.mokulu.discord.oauth.DiscordOAuth;
import io.mokulu.discord.oauth.model.Connection;
import io.mokulu.discord.oauth.model.Guild;
import io.mokulu.discord.oauth.model.TokensResponse;
import io.mokulu.discord.oauth.model.User;
import spark.Service;
import spark.Spark;
import spark.template.freemarker.FreeMarkerEngine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import static spark.Spark.*;

public class Site {

    public static final Gson gson = new Gson();
    public static final Scheduler scheduler = new Scheduler();

    public static DiscordOAuth oauth;

    public static Configuration rendererConfiguration;
    public static FreeMarkerEngine renderer;

    public static SystemProperties systemProperties;
    public static IdentityProperties identityProperties;

    public static Manager manager;
    public static String stripeWebhookSignature;

    public static ClientManager clientManager;

    public static void main(String[] args) throws Throwable {
        initShutdownHook();
        initProperties();
        initRenderer(buildDefaultConfiguration());
        initManager();
        initConsole();
        initPayments();
        initClientManager(identityProperties.get(IdentityProperties.SQL_IDENTITY));
        initOAuth();
        initServer();
        initRoutes();
        startServer();
    }

    public static void initShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down...");
            scheduler.run(Spark::stop);
        }));
    }

    public static void initProperties() throws Throwable {
        initSystemProperties();
        initIdentityProperties(systemProperties.get(SystemProperties.IDENTITY));
    }

    public static void initSystemProperties() throws Throwable {
        systemProperties = new SystemProperties();
    }

    public static void initIdentityProperties(String identity) throws Throwable {
        identityProperties = new IdentityProperties(identity);
    }

    public static Configuration buildDefaultConfiguration() {
        Configuration configuration = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
//        configuration.setDirectoryForTemplateLoading(new File("/where/templates/are"));
        configuration.setClassForTemplateLoading(FreeMarkerEngine.class, "");
        return configuration;
    }

    public static void initRenderer(Configuration configuration) {
        renderer = new FreeMarkerEngine(rendererConfiguration = configuration);
    }

    public static void initManager() throws URISyntaxException {
        manager = new Manager(identityProperties.get(IdentityProperties.MANAGER_ADDRESS), identityProperties.get(IdentityProperties.MANAGER_TOKEN));
    }

    public static void initConsole() {
        scheduler.runAsync(() -> {
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                try {
                    String message = input.readLine();
                    System.out.println("Console: " + message);
                    if (message.equalsIgnoreCase("stop")) {
                        scheduler.run(() -> System.exit(0));
                    } else {
                        System.out.println("Unknown command! (stop)");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void initPayments() {
        Stripe.apiKey = identityProperties.get(IdentityProperties.STRIPE_API_KEY);
        stripeWebhookSignature = identityProperties.get(IdentityProperties.STRIPE_WEBHOOK_SIGNATURE);
    }

    public static void initClientManager(String sqlIdentity) throws Throwable {
        clientManager = new ClientManager(sqlIdentity);
    }

    public static void initOAuth() throws IOException {
        String id = identityProperties.get(IdentityProperties.OAUTH_CLIENT_ID);
        String secret = identityProperties.get(IdentityProperties.OAUTH_CLIENT_SECRET);
        String redirect = identityProperties.get(IdentityProperties.OAUTH_REDIRECT);
        String[] scopes = identityProperties.get(IdentityProperties.OAUTH_SCOPES).split(", ");

        //Generate oauth
        oauth = new DiscordOAuth(id, secret, redirect, scopes);
//        String authUrl = oauth.getAuthorizationURL(null);
//
//        //Get auth code from authUrlRedirect
//        String code = "Returned from auth url redirect.";
//
//        //Get tokens based off auth code.
//        TokensResponse tokens = oauth.getTokens(code);
//        String accessToken = tokens.getAccessToken();
//        String refreshToken = tokens.getRefreshToken();
//
//        //Refresh access token
//        tokens = oauth.refreshTokens(refreshToken);
//
//        //Get api session based off user info
//        DiscordAPI api = new DiscordAPI(accessToken);
//
//        //Get user and info from user-api
//        User user = api.fetchUser();
//        String email = user.getEmail();
//        List<Guild> guilds = api.fetchGuilds();
    }

    public static void initServer() {
        initExceptionHandler(Throwable::printStackTrace);
        port(443);
        secure(identityProperties.get(IdentityProperties.KEYSTORE), identityProperties.get(IdentityProperties.KEYSTORE_PASSWORD), identityProperties.get(IdentityProperties.TRUSTSTORE), identityProperties.get(IdentityProperties.TRUSTSTORE_PASSWORD));
        threadPool(Integer.parseInt(identityProperties.get(IdentityProperties.SITE_THREADS)));
        Service http = Service.ignite();
        http.port(80);
        http.before((request, response) -> response.redirect("https://" + (request.url().startsWith("http://") ? request.url().split("http://")[1] : request.url()), 301));
    }

    public static void initRoutes() {
        notFound(new NotFound());
        internalServerError(new InternalServerError());
        post("/webhooks/stripe", new StripeWebhook());
        get("/", (request, response) -> {response.redirect("/invite"); return null;});
        get("/invite", (request, response) -> {response.redirect("https://discord.com/oauth2/authorize?client_id=941560606102806558&permissions=8&scope=applications.commands%20bot"); return null;});


    }

    public static void startServer() {
        if(routes().isEmpty())
            init();
        System.out.println("Web server running.");
    }


}
