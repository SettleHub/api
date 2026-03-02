# Instructions for build and run project in docker container

## Using Docker

Check if you have docker on your system
```bash
docker --version
```

Output should be something like that
```output
Docker version 27.3.1, build ce1223035a
```

> [!IMPORTANT]
> First step is create a database image and run container of it
> 
> Build DB image
> ```bash
> docker build -t esettlement-db:source -f Dockerfile.db .
> ```
> 
> And run container
> ```bash
> docker run -d --name esettlement-db -p 5432:5432 esettlement-db:source
> ```

Build API image with
```bash
docker build -t esettlement-api:source -f Dockerfile.api .
```

Create and run the container
```bash
docker run -d --name esettlement-api -p 8081:8081 esettlement-api:source
```

View container logs
```bash
docker logs esettlement-api
```

## Using Docker Compose

Check if you have docker-compose on your system
```bash
docker-compose --version
```

Output should be something like that
I use development version of docker-compose, so I get ```version dev```
```output
Docker Compose version dev
```

Next just run a command to up all
```bash
docker-compose up -d
```

To stop еthe containers
```bash
docker-compose down
```

> [!TIP]
> Execute command in your container
> ```sh
> docker exec CONTAINER_NAME uname -a
> ```
>
> Get into container with ```exec``` command
> ```sh
> docker exec -it CONTAINER_NAME /bin/bash
> ```

> [!IMPORTANT]
> If you want to run code in container, Docker should be installed on your computer
>
> Docker maintained on many OSs. [supported_platforms](https://docs.docker.com/engine/install/#supported-platforms)
>
> Install Docker Desktop
>
> - For Windows [see](https://docs.docker.com/desktop/setup/install/windows-install/)
> - For MacOS [see](https://docs.docker.com/desktop/setup/install/mac-install/)
> - For Linux OS [see](https://docs.docker.com/desktop/setup/install/linux/)
