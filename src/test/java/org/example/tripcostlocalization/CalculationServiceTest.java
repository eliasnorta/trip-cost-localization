package org.example.tripcostlocalization;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalculationServiceTest {

    @Test
    void saveCalculation_bindsAllValuesAndExecutesInsert() {
        CalculationRecord calcRecord = new CalculationRecord(
                BigDecimal.valueOf(180),
                BigDecimal.valueOf(6.5),
                BigDecimal.valueOf(2.05),
                BigDecimal.valueOf(11.7),
                BigDecimal.valueOf(23.985),
                "en-US"
        );

        Map<Integer, Object> boundValues = new HashMap<>();
        AtomicBoolean executeCalled = new AtomicBoolean(false);

        CalculationService service = new CalculationService() {
            @Override
            public Connection getConnection() {
                return fakeConnection(boundValues, executeCalled, false);
            }
        };

        service.saveCalculation(calcRecord);

        assertEquals(calcRecord.getDistance(), boundValues.get(1));
        assertEquals(calcRecord.getConsumption(), boundValues.get(2));
        assertEquals(calcRecord.getPrice(), boundValues.get(3));
        assertEquals(calcRecord.getTotalFuel(), boundValues.get(4));
        assertEquals(calcRecord.getTotalCost(), boundValues.get(5));
        assertEquals(calcRecord.getLanguage(), boundValues.get(6));
        assertEquals(Timestamp.valueOf(calcRecord.getCreatedAt()), boundValues.get(7));
        assertTrue(executeCalled.get());
    }

    @Test
    void saveCalculation_wrapsSqlExceptionWhenGetConnectionFails() {
        CalculationRecord calcRecord = new CalculationRecord(
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE,
                BigDecimal.ONE, BigDecimal.ONE, "en-US"
        );

        CalculationService service = new CalculationService() {
            @Override
            public Connection getConnection() throws SQLException {
                throw new SQLException("connection failed");
            }
        };

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.saveCalculation(calcRecord));
        assertTrue(ex.getMessage().contains("Unable to save calculation record"));
        assertTrue(ex.getCause() instanceof SQLException);
    }

    @Test
    void saveCalculation_wrapsSqlExceptionWhenExecuteUpdateFails() {
        CalculationRecord calcRecord = new CalculationRecord(
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE,
                BigDecimal.ONE, BigDecimal.ONE, "en-US"
        );

        CalculationService service = new CalculationService() {
            @Override
            public Connection getConnection() {
                return fakeConnection(new HashMap<>(), new AtomicBoolean(false), true);
            }
        };

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.saveCalculation(calcRecord));
        assertTrue(ex.getMessage().contains("Unable to save calculation record"));
        assertTrue(ex.getCause() instanceof SQLException);
    }

    private static Connection fakeConnection(Map<Integer, Object> boundValues,
                                             AtomicBoolean executeCalled,
                                             boolean failOnExecute) {
        return (Connection) Proxy.newProxyInstance(
                CalculationServiceTest.class.getClassLoader(),
                new Class[]{Connection.class},
                (proxy, method, args) -> {
                    if ("prepareStatement".equals(method.getName())) {
                        return fakePreparedStatement(boundValues, executeCalled, failOnExecute);
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

    private static PreparedStatement fakePreparedStatement(Map<Integer, Object> boundValues,
                                                           AtomicBoolean executeCalled,
                                                           boolean failOnExecute) {
        return (PreparedStatement) Proxy.newProxyInstance(
                CalculationServiceTest.class.getClassLoader(),
                new Class[]{PreparedStatement.class},
                (proxy, method, args) -> {
                    String methodName = method.getName();
                    if ("setBigDecimal".equals(methodName) || "setString".equals(methodName) || "setTimestamp".equals(methodName)) {
                        boundValues.put((Integer) args[0], args[1]);
                        return null;
                    }
                    if ("executeUpdate".equals(methodName)) {
                        if (failOnExecute) {
                            throw new SQLException("execute failed");
                        }
                        executeCalled.set(true);
                        return 1;
                    }
                    if ("close".equals(methodName)) {
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
