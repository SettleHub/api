# Instructions to build and run project locally

### To compile the code and run it use

```
mvn compile exec:java -Dexec.mainClass="org.ossfmct.projects.EsettlementApi"
```

#### You may also need a ```.jar``` package

To create a package run

```
mvn package
```

It will be located in ```target/``` folder. You can run it with

```
java -jar target/*.jar
```

#### Cleaning previously build classes and packages

```
mvn clean
```

> [!IMPORTANT]
> ```Java``` and ```Apache Maven``` should be installed on your computer
>
> For Java installing I recommend to use [SdkMan](https://sdkman.io/)
>
> install it with official instructions [see](https://sdkman.io/install/)
>
> then install needed JDK version with command
>
> ```
> sdk install java 17.0.11-amzn
> ```
>
> and make it default
>
> ```
> sdk default java 17.0.11-amzn
> ```
> then check with simple command
>
> ```
> javac -version 
> ```
>
> For Maven installing see [Downlaod](https://maven.apache.org/download.cgi) and [Install](https://maven.apache.org/install.html) official instructions.
>
> and check with simple command.
>
> ```
> mvn --version
> ```

