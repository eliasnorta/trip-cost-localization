# Trip Cost Localization Calculator

## Local Setup

### Prerequisites:

- Java
- MariaDB

### 1. Clone the Repository

```zsh
git clone https://github.com/eliasnorta/trip-cost-localization/tree/master
cd trip-cost-localization
```

### 2. Create Database and insert translations

Execute the `schema.sql` script to create the database and to insert translations.

### 3. Set up db connection

In the `DatabaseConnection` class replace `myadmin` and `my_strong_password` with your actual credentials.

### 4. Run the Application

## Database Configuration Details

### Connection String Format

```
jdbc:mariadb://localhost:3306/fuel_calculator_localization
```

- `localhost`: Database host
- `3306`: Default MariaDB port
- `fuel_calculator_localization`: Database name

## Troubleshooting

### "Could not load localized strings from database"
- Check MariaDB is running: `brew services list`
- Verify database exists: `mysql -u myadmin -p -e "SHOW DATABASES;"`
- Verify `localization_strings` table is populated

### "Connection refused" or "Can't connect to MariaDB"
- Ensure MariaDB is running: `brew services start mariadb`
- Check firewall/port 3306 is accessible
- Verify username and password are correct

### NullPointerException on startup
- Check `localization_strings` table has at least en_US entries
