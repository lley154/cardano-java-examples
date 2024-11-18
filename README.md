# Cardano Java Examples
## Environment Setup - Ubuntu Linux 20.04 LTS
### SDKMAN!
- Got to https://sdkman.io/ for more information on managing JDKs on your computer

- Run the command in your terminal window 

```curl -s "https://get.sdkman.io" | bash```

- Please close your terminal window and re-open it to ensure environment variables are set correctly. 

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
### Installing cursor
- Go to https://www.cursor.com/ and download the executable.
- Move the executable to ```/usr/local/bin``` directory
  
```
$ sudo mv ~/Downloads/cursor-0.42.5x86_64.AppImage /usr/local/bin/
```

- Change the permissions to make the file an executable
  
```
$ sudo chmod u+x /usr/local/bin/cursor-0.42.5x86_64.AppImage
```

- Create a symbolic link for ```cursor```
  
```
$ sudo ln -s /usr/local/bin/cursor-0.42.5x86_64.AppImage /usr/local/bin/cursor
```
```
$ ls -l /usr/local/bin/
total 904668
lrwxrwxrwx 1 root  root  43 Nov 18 08:28 cursor -> /usr/local/bin/cursor-0.42.5x86_64.AppImage

```
- Go into your java test example directory
```
$ cd ~/src/java-example
```

- Launch cursor
```
$ cursor .
```
![image](https://github.com/user-attachments/assets/9fec8d72-b9ab-4382-bccd-5894bc0a3140)

- Import VS code extensions if you already use VS Code or start from scratch.
- Create an account and log in
- Enable the AI agent by selecting the toggle AI Pane on the top right.

![image](https://github.com/user-attachments/assets/5e20ee0f-4502-4075-ad78-c33ae5711374)

- Install Extension Pack for Java if not installed
- In the top bar navigation, select ```Terminal -> New Terminal``` for a new terminal window
- Then, navigate to and open ```App.java``` file that was generated when the project was initialized.
- In the terminal window, run the program using ```gradle run```

![image](https://github.com/user-attachments/assets/bd1e050d-3bad-4369-b136-6544c36b80ba)

- Navigate to the test case file and run the test case in the terminal window using ```gradle clean test```. If all goes well, you should see the following.

![image](https://github.com/user-attachments/assets/2d48ecb6-e8cb-44b4-afe1-dde95385cc57)


## Transaction Testing


