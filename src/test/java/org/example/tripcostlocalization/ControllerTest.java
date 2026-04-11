package org.example.tripcostlocalization;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ControllerTest {
    private static boolean javaFxInitialized = false;
    private Controller controller;
    private MockCalculationService mockCalculationService;

    @BeforeAll
    static void initializeJavaFx() {
        if (!javaFxInitialized) {
            Platform.startup(() -> {
            });
            javaFxInitialized = true;
        }
    }

    @BeforeEach
    void setUp() {
        mockCalculationService = new MockCalculationService();
        this.controller = createController(mockCalculationService, new MockLocalizationService());
    }

    @Test
    void calculateCost_validInput_setsResultAndSavesRecord() {
        setInputValues("180", "6.5", "2.05");

        controller.calculateCost();

        assertTrue(getResultLabelText().startsWith("Result:"));
        assertEquals(1, mockCalculationService.savedCount);
    }

    @Test
    void calculateCost_invalidInput_showsInvalidMessage() {
        setInputValues("", "6.5", "2.05");

        controller.calculateCost();

        assertEquals("Invalid input", getResultLabelText());
    }

    @Test
    void calculateCost_whenSaveFails_showsDatabaseError() {
        mockCalculationService.throwOnSave = true;
        setInputValues("180", "6.5", "2.05");

        controller.calculateCost();

        assertEquals("Database error", getResultLabelText());
    }

    @Test
    void setLanguage_actionEventFr_setsFranceLocale() {
        Button button = new Button("FR");
        button.setUserData("FR");

        controller.setLanguage(new ActionEvent(button, null));

        assertEquals(Locale.FRANCE, controller.getLanguage());
    }

    @Test
    void setLanguage_actionEventUnknown_fallsBackToUS() {
        Button button = new Button("XX");
        button.setUserData("XX");

        controller.setLanguage(new ActionEvent(button, null));

        assertEquals(Locale.US, controller.getLanguage());
    }

    @Test
    void setLanguage_actionEventJp_setsJapanLocale() {
        Button button = new Button("JP");
        button.setUserData("JP");

        controller.setLanguage(new ActionEvent(button, null));

        assertEquals(Locale.JAPAN, controller.getLanguage());
    }

    @Test
    void setLanguage_actionEventIr_setsPersianLocale() {
        Button button = new Button("IR");
        button.setUserData("IR");

        controller.setLanguage(new ActionEvent(button, null));

        assertEquals(Locale.of("fa", "IR"), controller.getLanguage());
    }

    @Test
    void setLanguage_localePersian_setsRtlOrientation() {
        controller.setLanguage(Locale.of("fa", "IR"));

        javafx.scene.layout.VBox root = (javafx.scene.layout.VBox) getPrivateField("root");
        assertEquals(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT, root.getNodeOrientation());
    }

    @Test
    void setLanguage_localeEnglish_setsLtrOrientation() {
        controller.setLanguage(Locale.US);

        javafx.scene.layout.VBox root = (javafx.scene.layout.VBox) getPrivateField("root");
        assertEquals(javafx.geometry.NodeOrientation.LEFT_TO_RIGHT, root.getNodeOrientation());
    }

    @Test
    void setLanguage_whenRequiredKeyMissing_setsErrorText() {
        Controller brokenController = createController(mockCalculationService, new MissingKeyLocalizationService());

        brokenController.setLanguage(Locale.US);

        Label lblResult = (Label) getPrivateField(brokenController, "lblResult");
        assertTrue(lblResult.getText().contains("IllegalStateException"));
    }

    @Test
    void setLanguage_whenLocalizationThrows_setsErrorText() {
        Controller brokenController = createController(mockCalculationService, new ThrowingLocalizationService());

        brokenController.setLanguage(Locale.US);

        Label lblResult = (Label) getPrivateField(brokenController, "lblResult");
        assertTrue(lblResult.getText().contains("RuntimeException"));
    }

    @Test
    void parsePositiveNumber_acceptsCommaDecimal() throws Exception {
        Method method = Controller.class.getDeclaredMethod("parsePositiveNumber", String.class);
        method.setAccessible(true);

        double value = (double) method.invoke(controller, "12,5");

        assertEquals(12.5, value, 0.0001);
    }

    @Test
    void parsePositiveNumber_rejectsZero() throws Exception {
        Method method = Controller.class.getDeclaredMethod("parsePositiveNumber", String.class);
        method.setAccessible(true);

        InvocationTargetException ex = assertThrows(InvocationTargetException.class,
                () -> method.invoke(controller, "0"));

        assertTrue(ex.getCause() instanceof NumberFormatException);
    }

    @Test
    void formatCurrency_frenchLocale_usesEuroBranch() {
        controller.setLanguage(Locale.FRANCE);

        String result = controller.formatCurrency(1234.56);

        assertTrue(result.contains("EUR") || result.contains("€"));
    }

    @Test
    void formatCurrency_japaneseLocale_usesYenBranch() {
        controller.setLanguage(Locale.JAPAN);

        String result = controller.formatCurrency(1234.56);

        assertTrue(result.contains("JPY") || result.contains("¥") || result.contains("￥"));
    }

    @Test
    void formatCurrency_persianLocale_usesIrrBranch() {
        controller.setLanguage(Locale.of("fa", "IR"));

        String result = controller.formatCurrency(1234.56);

        assertTrue(
                result.contains("IRR")
                        || result.contains("ریال")
                        || result.contains("ريال")
                        || result.contains("﷼")
                        || result.contains("Rial")
        );
    }

    private void setInputValues(String distance, String consumption, String price) {
        TextField txtDistance = (TextField) getPrivateField("txtDistance");
        TextField txtConsumption = (TextField) getPrivateField("txtConsumption");
        TextField txtPrice = (TextField) getPrivateField("txtPrice");
        txtDistance.setText(distance);
        txtConsumption.setText(consumption);
        txtPrice.setText(price);
    }

    private String getResultLabelText() {
        Label lblResult = (Label) getPrivateField("lblResult");
        return lblResult.getText();
    }

    private Object getPrivateField(String fieldName) {
        try {
            var field = Controller.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(controller);
        } catch (Exception ex) {
            throw new RuntimeException("Unable to access field: " + fieldName, ex);
        }
    }

    private Object getPrivateField(Controller target, String fieldName) {
        try {
            var field = Controller.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (Exception ex) {
            throw new RuntimeException("Unable to access field: " + fieldName, ex);
        }
    }

    private Controller createController(CalculationService calculationService, LocalizationService localizationService) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/example/tripcostlocalization/view.fxml"));

        fxmlLoader.setControllerFactory(type -> {
            if (type == Controller.class) {
                return new Controller(calculationService, localizationService);
            }
            try {
                return type.getDeclaredConstructor().newInstance();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        try {
            fxmlLoader.load();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load FXML", ex);
        }

        return fxmlLoader.getController();
    }

    private static class MockCalculationService extends CalculationService {
        int savedCount = 0;
        boolean throwOnSave = false;

        @Override
        public void saveCalculation(CalculationRecord calcRecord) {
            if (throwOnSave) {
                throw new RuntimeException("DB error");
            }
            savedCount++;
        }
    }

    private static class MockLocalizationService extends LocalizationService {
        private static final Map<String, String> STRINGS = Map.ofEntries(
                Map.entry("distance.label", "Distance"),
                Map.entry("consumption.label", "Consumption"),
                Map.entry("price.label", "Price"),
                Map.entry("language.label", "Language"),
                Map.entry("calculate.button", "Calculate"),
                Map.entry("distance.prompt", "Enter distance"),
                Map.entry("consumption.prompt", "Enter consumption"),
                Map.entry("price.prompt", "Enter price"),
                Map.entry("result.label", "Result: {0} L, {1}"),
                Map.entry("invalid.input", "Invalid input"),
                Map.entry("database.error", "Database error")
        );

        @Override
        public Map<String, String> loadStrings(String language) {
            return STRINGS;
        }

        @Override
        public String getString(String key) {
            return STRINGS.getOrDefault(key, key);
        }

        @Override
        public Set<String> getAllKeys() {
            return STRINGS.keySet();
        }
    }

    private static class MissingKeyLocalizationService extends LocalizationService {
        @Override
        public Map<String, String> loadStrings(String language) {
            return Map.of("distance.label", "Distance");
        }

        @Override
        public String getString(String key) {
            return loadStrings("en_US").getOrDefault(key, key);
        }

        @Override
        public Set<String> getAllKeys() {
            return loadStrings("en_US").keySet();
        }
    }

    private static class ThrowingLocalizationService extends LocalizationService {
        @Override
        public Map<String, String> loadStrings(String language) {
            throw new RuntimeException("forced failure");
        }
    }
}
