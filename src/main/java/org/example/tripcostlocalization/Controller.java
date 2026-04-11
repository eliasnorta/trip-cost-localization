package org.example.tripcostlocalization;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
import java.util.Set;

public class Controller {
    private static final Set<String> REQUIRED_KEYS = Set.of(
            "distance.label",
            "consumption.label",
            "price.label",
            "language.label",
            "calculate.button",
            "distance.prompt",
            "consumption.prompt",
            "price.prompt"
    );

    @FXML
    private Label lblDistance;

    @FXML
    private Label lblConsumption;

    @FXML
    private Label lblPrice;

    @FXML
    private Label lblLanguage;

    @FXML
    private Label lblResult;

    @FXML
    private TextField txtDistance;

    @FXML
    private TextField txtConsumption;

    @FXML
    private TextField txtPrice;

    @FXML
    private Button btnCalculate;

    @FXML
    private VBox root;

    private Locale currentLocale = Locale.US;
    private final CalculationService calculationService;
    private final LocalizationService localizationService;

    public Controller() {
        this(new CalculationService(), new LocalizationService());
    }

    Controller(LocalizationService localizationService) {
        this(new CalculationService(), localizationService);
    }

    Controller(CalculationService calculationService, LocalizationService localizationService) {
        this.calculationService = calculationService;
        this.localizationService = localizationService;
    }

    @FXML
    public void initialize() {
        setLanguage(currentLocale);
    }

    @FXML
    public void calculateCost() {
        try {
            double distance = parsePositiveNumber(txtDistance.getText());
            double consumption = parsePositiveNumber(txtConsumption.getText());
            double price = parsePositiveNumber(txtPrice.getText());

            double totalFuel = (consumption / 100.0) * distance;
            double totalCost = totalFuel * price;

            NumberFormat numberFormat = NumberFormat.getNumberInstance(currentLocale);
            numberFormat.setMinimumFractionDigits(2);
            numberFormat.setMaximumFractionDigits(2);

            String localizedCost = formatCurrency(totalCost);

            String resultMessage = MessageFormat.format(
                    text("result.label"),
                    numberFormat.format(totalFuel),
                    localizedCost
            );
            lblResult.setText(resultMessage);

            CalculationRecord record = new CalculationRecord(
                    BigDecimal.valueOf(distance),
                    BigDecimal.valueOf(consumption),
                    BigDecimal.valueOf(price),
                    BigDecimal.valueOf(totalFuel),
                    BigDecimal.valueOf(totalCost),
                    currentLocale.toLanguageTag()
            );
            calculationService.saveCalculation(record);


        } catch (NumberFormatException ex) {
            lblResult.setText(text("invalid.input"));
        } catch (RuntimeException ex) {
            lblResult.setText(text("database.error"));
        }
    }

    @FXML
    public void setLanguage(ActionEvent event) {
        String languageCode = ((Button) event.getSource()).getUserData().toString();
        Locale locale = switch (languageCode) {
            case "FR" -> Locale.FRANCE;
            case "JP" -> Locale.JAPAN;
            case "IR" -> Locale.of("fa", "IR");
            default -> Locale.US;
        };
        setLanguage(locale);
    }

    public void setLanguage(Locale locale) {
        try {
            localizationService.loadStrings(locale.toString());
            validateRequiredKeys();
            currentLocale = locale;
            applyLocalizedText();

            if (locale.getLanguage().equals("fa")) {
                root.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            } else {
                root.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            }
        } catch (IllegalStateException ex) {
            lblResult.setText(ex.toString());
        } catch (RuntimeException ex) {
            lblResult.setText(ex.toString());
        }
    }

    private void applyLocalizedText() {
        lblDistance.setText(text("distance.label"));
        lblConsumption.setText(text("consumption.label"));
        lblPrice.setText(text("price.label"));
        lblLanguage.setText(text("language.label"));
        btnCalculate.setText(text("calculate.button"));

        txtDistance.setPromptText(text("distance.prompt"));
        txtConsumption.setPromptText(text("consumption.prompt"));
        txtPrice.setPromptText(text("price.prompt"));
        lblResult.setText("");
    }

    private String text(String key) {
        String value = localizationService.getString(key);
        if (!value.equals(key)) {
            return value;
        } else {
            throw new IllegalStateException();
        }
    }

    private void validateRequiredKeys() {
        Set<String> availableKeys = localizationService.getAllKeys();
        for (String key : REQUIRED_KEYS) {
            if (!availableKeys.contains(key)) {
                throw new IllegalStateException();
            }
        }
    }

    public String formatCurrency(double amount) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(currentLocale);

        Currency currency = switch (currentLocale.getLanguage()) {
            case "fr" -> Currency.getInstance("EUR");
            case "ja" -> Currency.getInstance("JPY");
            case "fa" -> Currency.getInstance("IRR");
            default -> Currency.getInstance("USD");
        };

        currencyFormat.setCurrency(currency);
        return currencyFormat.format(amount);
    }

    private double parsePositiveNumber(String value) {
        if (value == null || value.isBlank()) {
            throw new NumberFormatException();
        }

        double parsed = Double.parseDouble(value.trim().replace(',', '.'));
        if (parsed <= 0) {
            throw new NumberFormatException();
        }
        return parsed;
    }

    public Locale getLanguage() {
        return currentLocale;
    }
}
