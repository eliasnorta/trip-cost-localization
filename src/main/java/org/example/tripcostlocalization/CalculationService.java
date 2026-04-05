package org.example.tripcostlocalization;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class CalculationService {
    private static final String INSERT_SQL = """
            INSERT INTO calculation_records
            (distance, consumption, price, total_fuel, total_cost, language, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

    public void saveCalculation(CalculationRecord record) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setBigDecimal(1, record.getDistance());
            statement.setBigDecimal(2, record.getConsumption());
            statement.setBigDecimal(3, record.getPrice());
            statement.setBigDecimal(4, record.getTotalFuel());
            statement.setBigDecimal(5, record.getTotalCost());
            statement.setString(6, record.getLanguage());
            statement.setTimestamp(7, Timestamp.valueOf(record.getCreatedAt()));
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Unable to save calculation record", ex);
        }
    }

    public Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }
}
