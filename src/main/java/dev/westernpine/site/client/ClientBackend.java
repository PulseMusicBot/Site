package dev.westernpine.site.client;

import dev.westernpine.bettertry.Try;
import dev.westernpine.bettertry.functions.TryFunction;
import dev.westernpine.lib.object.Client;
import dev.westernpine.sql.Sql;

import java.sql.ResultSet;

public class ClientBackend {

    private static final String tableName = "clients";

    private Sql sql;

    public ClientBackend(Sql sql) {
        this.sql = sql;
        sql.update("CREATE TABLE IF NOT EXISTS `%s` (`userid` varchar(18) NOT NULL, `customerid` varchar(255) NOT NULL, PRIMARY KEY (userid))".formatted(tableName));
    }

    public Try<Client> create(Client client) {
        return this.sql.update("INSERT INTO `%s` VALUES (?,?);".formatted(tableName), client.getUserId(), client.getCustomerId()).map(changes -> client);
    }

    public Try<Client> readByUser(String userId) {
        return this.sql.query((TryFunction<ResultSet, Client>) rs -> new Client(userId, rs.getString("customerid")), "SELECT * FROM `%s` WHERE userid=?;".formatted(tableName), userId);
    }

    public Try<Client> readByCustomer(String customerId) {
        return this.sql.query((TryFunction<ResultSet, Client>) rs -> new Client(rs.getString("userid"), customerId), "SELECT * FROM `%s` WHERE customerid=?;".formatted(tableName), customerId);
    }

    public Try<Integer> update(Client client) {
        return this.sql.update("UPDATE `%s` SET customerid=? WHERE userid=?;".formatted(tableName), client.getCustomerId(), client.getUserId());
    }

    public Try<Integer> delete(String userId) {
        return this.sql.update("DELETE FROM `%s` WHERE userid=?;".formatted(tableName), userId);
    }

}