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

    public void saveCalculation(CalculationRecord calcRecord) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setBigDecimal(1, calcRecord.getDistance());
            statement.setBigDecimal(2, calcRecord.getConsumption());
            statement.setBigDecimal(3, calcRecord.getPrice());
            statement.setBigDecimal(4, calcRecord.getTotalFuel());
            statement.setBigDecimal(5, calcRecord.getTotalCost());
            statement.setString(6, calcRecord.getLanguage());
            statement.setTimestamp(7, Timestamp.valueOf(calcRecord.getCreatedAt()));
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Unable to save calculation record", ex);
        }
    }

    public Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }
}
