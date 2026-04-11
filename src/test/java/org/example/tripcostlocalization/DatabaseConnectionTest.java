package org.example.tripcostlocalization;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DatabaseConnectionTest {

    @Test
    void getConnection_usesConfiguredUrlAndCredentials() throws Exception {
        String expectedUrl = System.getenv().getOrDefault("TRIP_DB_URL", "jdbc:mariadb://localhost:3306/fuel_calculator_localization");
        String expectedUser = System.getenv().getOrDefault("TRIP_DB_USER", "myadmin");
        String expectedPassword = System.getenv().getOrDefault("TRIP_DB_PASSWORD", "your_strong_password");

        RecordingDriver driver = new RecordingDriver(false);

        withOnlyDriverRegistered(driver, () -> {
            try (Connection ignored = DatabaseConnection.getConnection()) {
                assertEquals(expectedUrl, driver.lastUrl);
                assertEquals(expectedUser, driver.lastUser);
                assertEquals(expectedPassword, driver.lastPassword);
            }
        });
    }

    @Test
    void getConnection_propagatesSQLException() throws Exception {
        RecordingDriver driver = new RecordingDriver(true);

        withOnlyDriverRegistered(driver, () ->
                assertThrows(SQLException.class, DatabaseConnection::getConnection)
        );
    }

    private void withOnlyDriverRegistered(Driver testDriver, ThrowingRunnable body) throws Exception {
        List<Driver> existingDrivers = new ArrayList<>();
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver existing = drivers.nextElement();
            existingDrivers.add(existing);
            DriverManager.deregisterDriver(existing);
        }

        DriverManager.registerDriver(testDriver);

        try {
            body.run();
        } finally {
            DriverManager.deregisterDriver(testDriver);
            for (Driver existing : existingDrivers) {
                DriverManager.registerDriver(existing);
            }
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private static class RecordingDriver implements Driver {
        private final boolean throwOnConnect;
        private String lastUrl;
        private String lastUser;
        private String lastPassword;

        private RecordingDriver(boolean throwOnConnect) {
            this.throwOnConnect = throwOnConnect;
        }

        @Override
        public Connection connect(String url, Properties info) throws SQLException {
            this.lastUrl = url;
            this.lastUser = info.getProperty("user");
            this.lastPassword = info.getProperty("password");

            if (throwOnConnect) {
                throw new SQLException("forced connection error");
            }
            return (Connection) java.lang.reflect.Proxy.newProxyInstance(
                    DatabaseConnectionTest.class.getClassLoader(),
                    new Class[]{Connection.class},
                    (proxy, method, args) -> null
            );
        }

        @Override
        public boolean acceptsURL(String url) {
            return url != null && url.startsWith("jdbc:mariadb:");
        }

        @Override
        public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) {
            return new DriverPropertyInfo[0];
        }

        @Override
        public int getMajorVersion() {
            return 1;
        }

        @Override
        public int getMinorVersion() {
            return 0;
        }

        @Override
        public boolean jdbcCompliant() {
            return false;
        }

        @Override
        public Logger getParentLogger() {
            return Logger.getGlobal();
        }
    }
}

