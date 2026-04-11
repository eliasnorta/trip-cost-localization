package org.example.tripcostlocalization;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalizationServiceTest {

    @Test
    void loadStrings_cachesByLanguage() {
        FixtureLocalizationService service = new FixtureLocalizationService();

        service.loadStrings("en_US");
        service.loadStrings("en_US");

        assertEquals(1, service.fetchCount("en_US"));
    }

    @Test
    void getString_returnsKnownValueAndFallsBackToKey() {
        FixtureLocalizationService service = new FixtureLocalizationService();
        service.loadStrings("en_US");

        assertEquals("Distance", service.getString("distance.label"));
        assertEquals("missing.key", service.getString("missing.key"));
    }

    @Test
    void getString_returnsEmptyForNullOrBlank() {
        FixtureLocalizationService service = new FixtureLocalizationService();

        assertEquals("", service.getString(null));
        assertEquals("", service.getString("   "));
    }

    @Test
    void getString_lazyLoadsDefaultLanguageWhenEmpty() {
        FixtureLocalizationService service = new FixtureLocalizationService();

        String value = service.getString("distance.label");

        assertEquals("Distance", value);
        assertEquals(1, service.fetchCount("en_US"));
    }

    @Test
    void getAllKeys_returnsCurrentLanguageKeys() {
        FixtureLocalizationService service = new FixtureLocalizationService();
        service.loadStrings("fr_FR");

        Set<String> keys = service.getAllKeys();

        assertTrue(keys.contains("distance.label"));
        assertTrue(keys.contains("price.label"));
    }

    @Test
    void getAllKeys_lazyLoadsDefaultLanguageWhenEmpty() {
        FixtureLocalizationService service = new FixtureLocalizationService();

        Set<String> keys = service.getAllKeys();

        assertTrue(keys.contains("distance.label"));
        assertEquals(1, service.fetchCount("en_US"));
    }

    @Test
    void loadStrings_usesDbFetchPathWithConnectionOverride() {
        DbBackedTestLocalizationService service = new DbBackedTestLocalizationService(false);

        Map<String, String> loaded = service.loadStrings("fr_FR");

        assertEquals("Distance (km)", loaded.get("distance.label"));
        assertEquals("Prix", loaded.get("price.label"));
    }

    @Test
    void loadStrings_wrapsSqlExceptionsFromDbPath() {
        DbBackedTestLocalizationService service = new DbBackedTestLocalizationService(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.loadStrings("fr_FR"));

        assertTrue(ex.getMessage().contains("Unable to load localization strings for language"));
    }

    @Test
    void getConnection_delegatesToDatabaseConnection() throws Exception {
        ExposedGetConnectionLocalizationService service = new ExposedGetConnectionLocalizationService();
        RecordingDriver driver = new RecordingDriver(false);

        withOnlyDriverRegistered(driver, () -> {
            try (Connection ignored = service.callBaseGetConnection()) {
                String expectedUrl = System.getenv().getOrDefault(
                        "TRIP_DB_URL",
                        "jdbc:mariadb://localhost:3306/fuel_calculator_localization"
                );
                assertEquals(expectedUrl, driver.lastUrl);
            }
        });
    }

    @Test
    void getConnection_propagatesSQLExceptionFromDatabaseConnection() throws Exception {
        ExposedGetConnectionLocalizationService service = new ExposedGetConnectionLocalizationService();
        RecordingDriver driver = new RecordingDriver(true);

        withOnlyDriverRegistered(driver, () ->
                assertThrows(SQLException.class, service::callBaseGetConnection)
        );
    }

    private static class FixtureLocalizationService extends LocalizationService {
        private final Map<String, AtomicInteger> counts = new HashMap<>();
        private final Map<String, Map<String, String>> byLanguage = Map.of(
                "en_US", Map.of("distance.label", "Distance", "price.label", "Price"),
                "fr_FR", Map.of("distance.label", "Distance", "price.label", "Prix")
        );

        @Override
        protected Map<String, String> fetchStrings(String language) {
            counts.computeIfAbsent(language, k -> new AtomicInteger()).incrementAndGet();
            return byLanguage.getOrDefault(language, Map.of());
        }

        int fetchCount(String language) {
            return counts.getOrDefault(language, new AtomicInteger(0)).get();
        }
    }

    private static class DbBackedTestLocalizationService extends LocalizationService {
        private final boolean throwOnGetConnection;

        private DbBackedTestLocalizationService(boolean throwOnGetConnection) {
            this.throwOnGetConnection = throwOnGetConnection;
        }

        @Override
        protected Connection getConnection() throws SQLException {
            if (throwOnGetConnection) {
                throw new SQLException("connection failed");
            }
            List<Map<String, String>> rows = List.of(
                    Map.of("key", "distance.label", "value", "Distance (km)"),
                    Map.of("key", "price.label", "value", "Prix")
            );
            return fakeConnection(rows);
        }
    }

    private static class ExposedGetConnectionLocalizationService extends LocalizationService {
        Connection callBaseGetConnection() throws SQLException {
            return super.getConnection();
        }
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

        private RecordingDriver(boolean throwOnConnect) {
            this.throwOnConnect = throwOnConnect;
        }

        @Override
        public Connection connect(String url, Properties info) throws SQLException {
            this.lastUrl = url;
            if (throwOnConnect) {
                throw new SQLException("forced connection error");
            }
            return (Connection) Proxy.newProxyInstance(
                    LocalizationServiceTest.class.getClassLoader(),
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

    private static Connection fakeConnection(List<Map<String, String>> rows) {
        return (Connection) Proxy.newProxyInstance(
                LocalizationServiceTest.class.getClassLoader(),
                new Class[]{Connection.class},
                (proxy, method, args) -> {
                    if ("prepareStatement".equals(method.getName())) {
                        return fakePreparedStatement(rows);
                    }
                    if ("close".equals(method.getName())) {
                        return null;
                    }
                    if ("isClosed".equals(method.getName())) {
                        return false;
                    }
                    return defaultValue(method.getReturnType());
                }
        );
    }

    private static PreparedStatement fakePreparedStatement(List<Map<String, String>> rows) {
        return (PreparedStatement) Proxy.newProxyInstance(
                LocalizationServiceTest.class.getClassLoader(),
                new Class[]{PreparedStatement.class},
                (proxy, method, args) -> {
                    if ("setString".equals(method.getName())) {
                        return null;
                    }
                    if ("executeQuery".equals(method.getName())) {
                        return fakeResultSet(rows);
                    }
                    if ("close".equals(method.getName())) {
                        return null;
                    }
                    return defaultValue(method.getReturnType());
                }
        );
    }

    private static ResultSet fakeResultSet(List<Map<String, String>> rows) {
        AtomicInteger index = new AtomicInteger(-1);
        return (ResultSet) Proxy.newProxyInstance(
                LocalizationServiceTest.class.getClassLoader(),
                new Class[]{ResultSet.class},
                (proxy, method, args) -> {
                    if ("next".equals(method.getName())) {
                        return index.incrementAndGet() < rows.size();
                    }
                    if ("getString".equals(method.getName())) {
                        Object column = args[0];
                        return rows.get(index.get()).get(column);
                    }
                    if ("close".equals(method.getName())) {
                        return null;
                    }
                    return defaultValue(method.getReturnType());
                }
        );
    }

    private static Object defaultValue(Class<?> returnType) {
        if (returnType == boolean.class) {
            return false;
        }
        if (returnType == int.class) {
            return 0;
        }
        if (returnType == long.class) {
            return 0L;
        }
        if (returnType == double.class) {
            return 0.0;
        }
        if (returnType == float.class) {
            return 0.0f;
        }
        if (returnType == short.class) {
            return (short) 0;
        }
        if (returnType == byte.class) {
            return (byte) 0;
        }
        if (returnType == char.class) {
            return '\0';
        }
        return null;
    }
}
