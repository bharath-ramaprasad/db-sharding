package oracle.sharding.examples;

import oracle.sharding.sql.ShardConfigurationInfo;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by somestuff on 7/31/17.
 */
public class RoutingDataSerialization {
    public ShardConfigurationInfo connectAndSave() throws SQLException, IOException
    {
        ShardConfigurationInfo result;

        /* Create connection to the catalog */
        try (Connection connection = DriverManager.getConnection(
                Parameters.connectionString, Parameters.username, Parameters.password))
        {
            /* Load the routing table from the catalog */
            result = ShardConfigurationInfo.loadFromDatabase(connection, true);
        }

        if (Parameters.routingTableFile != null) {
            try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(Parameters.routingTableFile))) {
                output.writeObject(result);
            }
        }

        return result;
    }

    public ShardConfigurationInfo load() throws IOException, ClassNotFoundException {
        try (ObjectInputStream output = new ObjectInputStream(new FileInputStream(Parameters.routingTableFile)))
        {
            return (ShardConfigurationInfo) output.readObject();
        }
    }

    public ShardConfigurationInfo loadFromFileOrCatalog()
            throws IOException, ClassNotFoundException, SQLException
    {
        if (new File(Parameters.routingTableFile).isFile()) {
            return load();
        } else {
            return connectAndSave();
        }
    }

    public static ShardConfigurationInfo loadRoutingData()
            throws IOException, ClassNotFoundException, SQLException
    {
        if (Parameters.routingTableFile != null && new File(Parameters.routingTableFile).isFile()) {
            try (ObjectInputStream output = new ObjectInputStream(new FileInputStream(Parameters.routingTableFile))) {
                return (ShardConfigurationInfo) output.readObject();
            }
        } else {
            ShardConfigurationInfo result;

            /* Create connection to the catalog */
            try (Connection connection = DriverManager.getConnection(Parameters.connectionString, Parameters.username, Parameters.password))
            {
                /* Load the routing table from the catalog */
                result = ShardConfigurationInfo.loadFromDatabase(connection, true);
            }

            if (Parameters.routingTableFile != null) {
                ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(Parameters.routingTableFile));
                output.writeObject(result);
            }

            return result;
        }
    }

    public static void main(String [] args)
    {
        try {
            Parameters.init(args);
            new RoutingDataSerialization().connectAndSave();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
