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
### Download cardano-java-example
- Close the cursor IDE, and start in a new directory to download the cardano-java-example github repo
```
$ git clone https://github.com/lley154/cardano-java-examples.git
$ cd cardano-java-examples
$ cursor .
```

- After launching ```cursor```, the IDE will compile the java files inside the project.  This may take a few minutes to complete.  Then, navigate to one of the test files to see test cases.

### Install docker (if not already installed)
```sudo apt -y install apt-transport-https ca-certificates curl software-properties-common```

```curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg```

```echo "deb [arch=amd64 signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null```

```sudo apt update```

```sudo apt -y install docker-ce docker-ce-cli containerd.io```

```sudo usermod -aG docker $USER```

```newgrp docker ```

```docker run hello-world```

- Check that docker is running.
```
$ sudo systemctl status docker
● docker.service - Docker Application Container Engine
     Loaded: loaded (/lib/systemd/system/docker.service; disabled; vendor preset: enabled)
     Active: active (running) since Mon 2024-11-18 10:07:54 EST; 4s ago
TriggeredBy: ● docker.socket
       Docs: https://docs.docker.com
   Main PID: 1789814 (dockerd)
      Tasks: 10
     Memory: 102.0M
     CGroup: /system.slice/docker.service
             └─1789814 /usr/bin/dockerd -H fd:// --containerd=/run/containerd/containerd.sock

```

### Install Yaci DevKit
- Go to Yaci DevKit Github repo and download v0.9.3-beta release
https://github.com/bloxbean/yaci-devkit/releases/tag/v0.9.3-beta 
- Unzip the zip file and go into the directory

```$ cd yaci-devkit-0.9.3-beta```

- Edit the env file and make sure both ogmios and kupo are enabled.
  
```$ more ./config/env 
yaci_store_enabled=true
ogmios_enabled=true
kupo_enabled=true
...
```

- Run the startup script

```
$ ./bin/devkit.sh start
Attempting to start the service...
docker-compose not found, let's try 'docker compose'
[+] Running 2/0
 ✔ Container node1-yaci-viewer-1  Running                                                   0.0s 
 ✔ Container node1-yaci-cli-1     Running                                                   0.0s 
Docker Compose started successfully.
...
```

- Create a conway node network

```yaci-cli:>create-node -o --era conway```

- And then start it

```
devnet:default>start
...
Waiting for Yaci Store to start ...
Waiting for Yaci Store to start ...
[💡 OK] Yaci Store Started
```

- Select tip to confirm the network is running

```
devnet:default>tip
[✅ Block#] 95
[✅ Slot#] 696
[✅ Block Hash] 42b557096ee7e4460a9f3990163bd0809d19d4f68ad9592be3185079325c1efe
```
### Testing the Java transactions
- With the Yaci Devkit running, run the transaction test case
  
```gradle -Dtest.single=org.example.TransactionTest```

- in the terminal window, and you will get the following error:

![image](https://github.com/user-attachments/assets/ebe83d6d-7590-45cf-8862-add5504853bd)

- Go to the test reports to see what the error is
![image](https://github.com/user-attachments/assets/e13e7ba7-307a-4ef7-9b26-a2eb629cc906)

- We see that there is insufficient funds, so we need to add some fund to the account trying to transfer Ada.

```
devnet:default>topup addr_test1qq8phk43ndg0zf2l4xc5vd7gu4f85swkm3dy7fjmfkf6q249ygmm3ascevccsq5l5ym6khc3je5plx9t5vsa06jvlzls8el07z 7000
[💡 Txn Cbor] 84a40081825820347e9d3c72ddc52bdb43cf1c44d31507983c102e870398b9205fb8ed125f7e64000182825839000e1bdab19b50f1255fa9b14637c8e5527a41d6dc5a4f265b4d93a02aa52237b8f618cb3188029fa137ab5f1196681f98aba321d7ea4cf8bf1b00000001a13b860082581d60a0f1aa7dca95017c11e7e373aebcf0c4568cf47ec12b94f8eb5bba8b1b000aa87a4d156437021a000295c907582024ff08a7f728937e5d01cc55619870ba457d17c94659637acabc5584f9dc440da100818258209b0ee7e26318d3675742c8b841b981ae57c37bcbb2cc5625f80606b3256d08145840ae757f89e921e76473b2e4f396b290de5742dc8adb721e8a35bf62da110b16aac88f6bf86061cb24d55c8820cf6723a6db8ebd1a896d16639a0dfe036f1ace05f5a11902a2a1636d7367816a546f7075702046756e64
✅ Transaction submitted successfully
Waiting for tx to be included in block...
Waiting for tx to be included in block...
[💡 Txn# : ] 1fc75a66df8bd9a3ef8d7a11b4ab874dcfeb4bbf61af7dcc602cdaa679cf2959
Waiting for next block...
💡 Available utxos

1. 1fc75a66df8bd9a3ef8d7a11b4ab874dcfeb4bbf61af7dcc602cdaa679cf2959#0 : [Amount(unit=lovelace, quantity=7000000000)]
--------------------------------------------------------------------------------------
devnet:default>
```

- Now, when running the test case, it passes.

![image](https://github.com/user-attachments/assets/f2997271-3944-4792-b641-963f8ea7cdf9)

- To run all of the test cases
- To see the transactions, use the DevKit explorer and the info to get the URL.

```
devnet:default>info

###### Node Details (Container) ######
[💡 Node port] 3001
[💡 Node Socket Paths] 
/clusters/nodes/default/node/node.sock
[💡 Submit Api Port] 8090
[💡 Protocol Magic] 42
[💡 Block Time] 1.0 sec
[💡 Slot Length] 1.0 sec
[💡 Start Time] 1731945662
[💡 Epoch Length] 600
[💡 Security Param] 300
[💡 SlotsPerKESPeriod] 129600


#################### URLS (Host) ####################
[💡 Yaci Viewer] http://localhost:5173
[💡 Yaci Store Swagger UI] http://localhost:8080/swagger-ui.html
[💡 Yaci Store Api URL] http://localhost:8080/api/v1/
[💡 Pool Id] pool1wvqhvyrgwch4jq9aa84hc8q4kzvyq2z3xr6mpafkqmx9wce39zy


#################### Other URLS ####################
[💡 Ogmios Url (Optional)] ws://localhost:1337
[💡 Kupo Url   (Optional)] http://localhost:1442


#################### Node Ports ####################
[💡 n2n port] localhost:3001
[💡 n2c port (socat)] localhost:3333
devnet:default>
```
![image](https://github.com/user-attachments/assets/1173799c-71df-41e2-8925-d150ba9ed264)







