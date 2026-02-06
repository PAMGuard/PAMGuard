# PAMGuard Compilation

This document explains how to compile PAMGuard from source on Linux (Debian/Ubuntu).

## Prerequisites

- Java JDK 21
- Maven
- Git
- OpenJFX (for the graphical interface)

## Compilation Steps

1. **Install dependencies**

```bash
sudo apt update
sudo apt install -y openjdk-21-jdk openjfx git maven
```

1. **Check versions**

```bash
java -version
mvn -v
```

1. **Clone the PAMGuard repository**

```bash
git clone https://github.com/PAMGuard/PAMGuard.git
cd PAMGuard
```

1. **Compile the project**

```bash
mvn clean install shade:shade org.vafer:jdeb:jdeb -DskipTests
```

- The `-DskipTests` option speeds up compilation by skipping tests.
- After compilation, both a `.deb` package (for Debian/Ubuntu installation) and a `.jar` file (Java executable) are generated in the `target/` folder.

## Install via .deb file

After compilation, you can install PAMGuard using the generated `.deb` package:

```bash
sudo apt install ./target/Pamguard_*.deb
```

Then launch:

1. Open the Applications menu
2. Search for "PAMGuard"
3. Click on the PAMGuard icon to launch the application

### Run without installing (.jar only)

If PAMGuard does not appear in your applications menu, or if you prefer to launch it manually, you can run the .jar file in GUI mode with the following shell command :

```bash
java \
 -Xmx10g \
 --module-path /usr/share/openjfx/lib \
 --add-modules=javafx.controls,javafx.fxml,javafx.swing \
 -jar target/Pamguard-*.jar
```

Before running the command line, you should:

- Adjust the paths to your Java and JavaFX installations as needed. Ensure you replace the JAR file name with the actual version you have built.

- Include the required plugins in the /target directory.
