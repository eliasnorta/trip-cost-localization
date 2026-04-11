package org.example.tripcostlocalization;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LocalizationService {
    private static final String DEFAULT_LANGUAGE = "en_US";
    private static final String SELECT_STRINGS_SQL = """
            SELECT `key`, value
            FROM localization_strings
            WHERE language = ?
            ORDER BY `key`
            """;

    private final Map<String, Map<String, String>> cacheByLanguage = new ConcurrentHashMap<>();
    private String currentLanguage = DEFAULT_LANGUAGE;
    private Map<String, String> currentStrings = Collections.emptyMap();

    public Map<String, String> loadStrings(String language) {
        currentLanguage = language;
        currentStrings = cacheByLanguage.computeIfAbsent(language, this::fetchStrings);
        return currentStrings;
    }

    public String getString(String key) {
        if (key == null || key.isBlank()) {
            return "";
        }

        if (currentStrings.isEmpty()) {
            loadStrings(currentLanguage);
        }

        String value = currentStrings.get(key);
        return value != null ? value : key;
    }

    public Set<String> getAllKeys() {
        if (currentStrings.isEmpty()) {
            loadStrings(currentLanguage);
        }
        return currentStrings.keySet();
    }

    protected Map<String, String> fetchStrings(String language) {
        return fetchStringsFromDb(language);
    }

    private Map<String, String> fetchStringsFromDb(String language) {
        Map<String, String> values = new LinkedHashMap<>();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_STRINGS_SQL)) {
            statement.setString(1, language);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    values.put(resultSet.getString("key"), resultSet.getString("value"));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Unable to load localization strings for language: " + language, ex);
        }
        return Collections.unmodifiableMap(values);
    }

    protected Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }
}
