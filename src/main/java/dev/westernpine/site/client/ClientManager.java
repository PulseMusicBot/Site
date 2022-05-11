package dev.westernpine.site.client;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.param.CustomerCreateParams;
import dev.westernpine.bettertry.Try;
import dev.westernpine.lib.object.Client;
import dev.westernpine.site.properties.SqlProperties;
import dev.westernpine.sql.Sql;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class ClientManager {

    private final ClientBackend backend;

    private final LoadingCache<String, Client> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build(new CacheLoader<String, Client>() {
                @Override
                public Client load(String userid) throws Exception {
                    return backend.readByUser(userid).orElseThrow(() -> new NullPointerException("Client doesn't exist."));
                }
            });

    public ClientManager(String sqlIdentity) throws Throwable {
        this.backend = new ClientBackend(new SqlProperties(sqlIdentity).toSql());
    }

    public Try<Client> getOrCreate(String userId, String email) {
        return getByUser(userId)
                .orElseTry(() -> createNewClient(userId, email))
                .onSuccess(client -> cache.put(userId, client))
                .onSuccess(client -> {
                    if(!backend.readByUser(userId).isSuccessful())
                        backend.create(client);
                });
    }

    public Try<Client> update(Client client) {
        return Try.to(() -> backend.update(client)).map(changed -> client);
    }

    public Try<Integer> delete(String userid) {
        cache.asMap().remove(userid);
        return backend.delete(userid);
    }

    public Try<Client> getByUser(String userId) {
        return Try.to(() -> cache.get(userId));
    }

    public Try<Client> getByCustomer(String customerid) {
        Stream<Client> clients = cache.asMap().values().stream();
        return clients.anyMatch(client -> client.getCustomerId().equals(customerid))
                ? Try.to(() -> clients.filter(client -> client.getCustomerId().equals(customerid)).findAny().get())
                : backend.readByCustomer(customerid);
    }

    private Client createNewClient(String userId, String email) throws StripeException {
        return new Client(userId, createNewCustomer(userId, email).getId());
    }

    private Customer createNewCustomer(String userid, String email) throws StripeException {
        return Customer.create(CustomerCreateParams.builder().setName(userid).setEmail(email).build());
    }

}