package dev.westernpine.site.properties;

import dev.westernpine.lib.properties.PropertiesFile;
import dev.westernpine.lib.properties.Property;
import dev.westernpine.lib.properties.PropertyField;
import dev.westernpine.lib.properties.PropertyFile;

public class IdentityProperties extends PropertiesFile {

    @PropertyField
    public static final Property SESSION_LOCKER_PORT = new Property("session locker server port", "5211");

    @PropertyField
    public static final Property MANAGER_ADDRESS = new Property("manager address", "localhost");

    @PropertyField
    public static final Property MANAGER_TOKEN = new Property("manager token", "TOKEN");

    @PropertyField
    public static final Property STRIPE_API_KEY = new Property("stripe api key", "KEY");

    @PropertyField
    public static final Property STRIPE_WEBHOOK_SIGNATURE = new Property("stripe webhook signature", "SIGNATURE");

    @PropertyField
    public static final Property OAUTH_CLIENT_ID = new Property("oauth client id", "ID");

    @PropertyField
    public static final Property OAUTH_CLIENT_SECRET = new Property("oauth client secret", "SECRET");

    @PropertyField
    public static final Property OAUTH_REDIRECT = new Property("oauth redirect", "redirect");

    @PropertyField
    public static final Property OAUTH_SCOPES = new Property("oauth scopes (comma separated)", "identify, guilds, email");

    @PropertyField
    public static final Property HOST_ADDRESS = new Property("host address", "localhost");

    @PropertyField
    public static final Property KEYSTORE = new Property("keystore", "keystore.jks");

    @PropertyField
    public static final Property KEYSTORE_PASSWORD = new Property("keystore password", "PASSWORD");

    @PropertyField
    public static final Property TRUSTSTORE = new Property("truststore", "truststore.jks");

    @PropertyField
    public static final Property TRUSTSTORE_PASSWORD = new Property("truststore password", "PASSWORD");

    @PropertyField
    public static final Property SITE_THREADS = new Property("site threads", "15");

    @PropertyField
    public static final Property SQL_IDENTITY = new Property("sql identity", "clientsql");

    public IdentityProperties(String identity) throws Throwable {
        super(identity + ".properties", PropertyFile.getDeclaredProperties(IdentityProperties.class));
    }

    @Override
    public PropertyFile reload() throws Throwable {
        super.reload();
        return this;
    }
}
