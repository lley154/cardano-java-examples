# Cardano Java Examples
## Environment Setup - Ubuntu Linux 20.04 LTS
### SDKMAN!
Got to https://sdkman.io/ for more information on managing JDKs on your computer

Run the command in your terminal window 

```curl -s "https://get.sdkman.io" | bash```

Please close your terminal window and re-open it to make sure environment variables are set correctly. 

### Install Java
```sdk list java```

```sdk install java 23-open```

```
$ sdk current

Using:

java: 23-open
$ java --version
openjdk 23 2024-09-17
OpenJDK Runtime Environment (build 23+37-2369)
OpenJDK 64-Bit Server VM (build 23+37-2369, mixed mode, sharing)
$ javac --version
javac 23
```

### Instal gradle

```sdk list gradle```
```sdk install gradle```


## Project Template
### Creating a skeleton Java application
```
$ gradle init --type java-application  --dsl kotlin
```
```
Enter target Java version (min: 7, default: 21): 21

Project name (default: java-example): java-example

Select application structure:
  1: Single application project
  2: Application and library project
Enter selection (default: Single application project) [1..2] 1

Select test framework:
  1: JUnit 4
  2: TestNG
  3: Spock
  4: JUnit Jupiter
Enter selection (default: JUnit Jupiter) [1..4] 4

Generate build using new APIs and behavior (some features may change in the next minor release)? (default: no) [yes, no] no


> Task :init
Learn more about Gradle by exploring our Samples at https://docs.gradle.org/8.11/samples/sample_building_java_applications.html

BUILD SUCCESSFUL in 25s
1 actionable task: 1 executed
```

```
$ tree .
.
├── app
│   ├── build.gradle.kts
│   └── src
│       ├── main
│       │   ├── java
│       │   │   └── org
│       │   │       └── example
│       │   │           └── App.java
│       │   └── resources
│       └── test
│           ├── java
│           │   └── org
│           │       └── example
│           │           └── AppTest.java
│           └── resources
├── gradle
│   ├── libs.versions.toml
│   └── wrapper
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradle.properties
├── gradlew
├── gradlew.bat
└── settings.gradle.kts

```

## IDE Setup


## Transaction Testing


