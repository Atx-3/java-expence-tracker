# Expense Tracker

A modern desktop expense tracking application built with Java Swing featuring a beautiful UI with dark/light mode support.

## Features

- **Modern UI/UX** - Clean, intuitive interface inspired by mobile apps
- **Dark/Light Mode** - Toggle between themes based on preference
- **Transaction Management** - Add, edit, and delete income/expense transactions
- **Categories** - Organize transactions with visual category icons
- **Reports & Analytics** - View spending distribution and monthly trends
- **Data Persistence** - All data saved locally in JSON format
- **Export Data** - Backup your transactions to a JSON file
- **Multi-Currency** - Support for USD, EUR, GBP, JPY, and INR

## Screenshots

The application features:
- Home screen with balance overview and recent transactions
- Full transaction list with search and filter
- Visual reports with pie charts and bar graphs
- Category breakdown with spending percentages
- Settings for theme, currency, and data management

## Requirements

- Java 17 or higher
- Maven 3.6+ (for building)

## Building the Application

### Using Maven (Recommended)

```bash
# Navigate to project directory
cd "java expence tracker"

# Clean and compile
mvn clean compile

# Run the application
mvn exec:java -Dexec.mainClass="com.expensetracker.main.Main"

# Or build a JAR file
mvn package

# Run the JAR
java -jar target/expense-tracker-1.0.0.jar
```

### Using Command Line (without Maven)

```bash
# Create output directory
mkdir -p out

# Download JSON library
# Get from: https://mvnrepository.com/artifact/org.json/json

# Compile all Java files
javac -d out -cp "lib/*" src/com/expensetracker/**/*.java

# Run the application
java -cp "out;lib/*" com.expensetracker.main.Main
```

## Project Structure

```
java expence tracker/
├── src/
│   └── com/
│       └── expensetracker/
│           ├── main/
│           │   └── Main.java           # Application entry point
│           ├── model/
│           │   ├── Transaction.java    # Transaction data model
│           │   └── Category.java       # Category definitions
│           ├── data/
│           │   ├── DataManager.java    # Data persistence layer
│           │   └── JsonUtil.java       # JSON serialization
│           ├── ui/
│           │   ├── MainFrame.java      # Main application window
│           │   ├── HomePanel.java      # Home screen panel
│           │   ├── TransactionsPanel.java  # Transactions list
│           │   ├── ReportsPanel.java   # Analytics/reports
│           │   ├── TransactionDialog.java  # Add/Edit dialog
│           │   └── SettingsDialog.java # Settings panel
│           └── util/
│               ├── Theme.java          # Theme colors/definitions
│               └── UIUtil.java         # UI helper utilities
├── pom.xml                             # Maven build file
└── README.md                           # This file
```

## Usage

### Adding a Transaction

1. Click the "Add" button (center of bottom navigation)
2. Select transaction type (Income/Expense)
3. Enter amount and description
4. Choose a category
5. Set the date
6. Add optional notes
7. Click "Save Transaction"

### Viewing Reports

1. Click "Reports" in the navigation
2. Select time period (Week/Month/Year/All)
3. View spending distribution chart
4. See category breakdown with percentages
5. Check monthly spending trend

### Changing Theme

1. Click "Settings" in the navigation
2. Toggle "Dark Mode" switch
3. Theme changes apply immediately

### Exporting Data

1. Go to Settings
2. Click "Export Data"
3. Choose save location
4. Data saved as JSON file

## Data Storage

All transaction data is stored locally in:
- Windows: `C:\Users\<YourName>\.expensetracker\data.json`
- macOS/Linux: `~/.expensetracker/data.json`

User preferences (theme, currency) are stored in Java Preferences API.

## Customization

### Adding New Categories

Edit `Category.java` and add new entries to `EXPENSE_CATEGORIES` or `INCOME_CATEGORIES`:

```java
new Category("category_id", "Display Name", "font-awesome-icon", new Color(r, g, b), false)
```

### Adding New Currencies

1. Add currency code and symbol in `DataManager.getCurrencySymbol()`
2. Add option in `SettingsDialog` currency combo box

## Troubleshooting

### Application won't start
- Ensure Java 17+ is installed: `java -version`
- Check if JSON library is in classpath

### Data not saving
- Check write permissions for user home directory
- Verify `.expensetracker` folder exists

### UI looks incorrect
- Try changing Look and Feel in code
- Ensure FlatLaf library is included

## License

This project is open source and available for personal and commercial use.

## Version

1.0.0 - Initial Release
