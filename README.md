# MealGenerator Java Application

> **Warning**: **This project is currently non-runnable due to known bugs in database connectivity and GUI logic.**  
> **This repository serves as a documentation showcase** of system design, MySQL integration, and Java Swing UI planning.

MealGenerator is a Java-based tool that suggests meals tailored to dietary needs, calories, regions, and ingredients. It integrates a MySQL database to retrieve and filter meal data dynamically, making it ideal for health-conscious users or meal planning.

Features
1. Dynamic meal suggestions based on user inputs like calories, nutrition, and diet.
2. Normalized database schemas for efficient data management.
3. Unit tests and input validation for reliability.
4. Graphical user interface for easy interaction.

Technologies Used
1. Language: Java
2. Database: MySQL
3. Tools: VS Code, Java Swing for GUI

Installation
- Clone the repository: git clone https://github.com/afreenportfolio/mealgenerator.git
- Set up MySQL: Create database and import schema/tables (e.g., Meals, Ingredients).
- Add MySQL Connector/J JAR to your classpath.
- Compile: javac MealGenerator.java
- Run: java MealGenerator

Usage
- Run the application: java Main
- Input preferences via the GUI.
- View generated meal lists with details.

Contributing
- Contributions are welcome! Please fork the repo and submit a pull request.

License
- MIT License
