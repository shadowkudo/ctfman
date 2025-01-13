# CTFman

<a name="readme-top"></a>

<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
        <a href="#built-with">Built With</a>
    </li>
    <li><a href="#introduction">Introduction</a></li>
    <li>
        <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#running-in-local">Local</a></li>
        <li><a href="#running-with-docker">Docker</a></li>
        <li><a href="#running-with-docker-compose">Docker Compose</a></li>
      </ul>
    </li>
    <li>
        <a href="#documentation">Documentation</a>
      <ul>
        <li>
            <a href="#vm-azure">VM with Azure</a>
          <ul>
            <li><a href="#installation">Installation</a></li>
            <li><a href="#configuration">Configuration</a></li>
          </ul>
        </li>
        <li><a href="#zone-dns">Zone DNS</a></li>
        <li><a href="#api">API</a></li>
      </ul>
    </li>
    <li>
        <a href="#usage">Usage</a>
      <ul>
        <li><a href="#examples-using-curl">Curl</a></li>
        <li><a href="#examples-using-ui-on-a-web-browser">UI on web browser</a></li>
      </ul>
    </li>
    <li><a href="#license">License</a></li>
    <li><a href="#contacts">Contacts</a></li>
  </ol>
</details>

### Built With

- [Java 21 temurin][java]
- [Maven][maven]
- [Docker][docker]
- [Picocli][picocli]
- [PostgreSQL][postgresql]
- [Swagger][swagger]
- [Javalin][javalin]
- [Microsoft Azure][azure]

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- INTRODUCTION -->

## Introduction

The goal of `CTFman` - a web application using a database - is to efficiently manage Capture The Flag (CTF) competitions. The database will store and organize information about CTFs, participants, teams, tools, and performance metrics. This will allow for:

- Historical Tracking: Preserving records of competitions, which is essential since CTFs are only accessible for a limited time during the event duration.
- Data Interaction: Providing a seamless interface to visualize, modify, and manage the stored data.
- Comprehensive Functionality: Enabling functionalities like managing team registrations, tracking user activities, organizing challenges, managing tools and resources, and moderating comments.

> [!NOTE]
> CTF - Capture The Flag - is a competition where participants solve cybersecurity challenges to gain points and compete against others.

> [!CAUTION]
> Hacking is not necessarily a negative practice. In fact, it plays an essential role in learning and discovering existing vulnerabilities in computer systems. Through disciplines like CTF (Capture the Flag), cybersecurity enthusiasts can develop skills by identifying potential flaws and understanding how they could be exploited. This knowledge is then used to strengthen system security and prevent future attacks. Thus, ethical hacking or "white hat" hacking actively contributes to the protection of digital infrastructures by anticipating risks and proposing solutions.

<!-- GETTING STARTED -->

## Getting Started

### Prerequisites

#### Java 21

- [asdf][asdf]

  ```sh
  # Install the plugin if needed
  asdf plugin add java
  # Install
  asdf install java latest:temurin-21
  ```

- Mac (homebrew)

  ```zsh
  brew tap homebrew/cask-versions
  brew install --cask temurin@21
  ```

- Windows (winget)

  ```ps
  winget install EclipseAdoptium.Temurin.21.JDK
  ```

#### Running in local

Clone this repository with either `ssh` or `http`, but we recommend using `ssh` as it is more secure.

```sh
git clone git@github.com:shadowkudo/ctfman.git
```
Use the maven wrapper to install dependencies, build and package the project.

```sh
# install the dependencies
./mvnw clean install
# build
./mvnw package
# run
java -jar target/<filename>.jar --help
```

#### Running with Docker

Build the Docker image and publish it to GitHub Container Registry:

```sh
docker build -t ghcr.io/<username>/ctfman:latest
docker push ghcr.io/<username>/ctfman:latest
```

#### Running with Docker Compose

Run the application using Docker Compose:

```sh
docker compose --profile dev up -d
```

<!-- DOCUMENTATION -->

## Documentation

### VM - Azure

In our project, we choose to use Microsoft Azure as the provider for the virtual machine for our web server. See below for the installation and configuration process for a student account. 

#### Installation

Access the Azure portal with the following link: <https://portal.azure.com> and log in.

Create a new virtual machine from the dashboard in section `Create a resource`.

Select a virtual machine with the following characteristics:

- **Project details**
  - **Subscription**: Azure for Students
  - **Resource group**: Create new with the name `heig-vd-dai-course`
- **Instance details**
  - **Virtual machine name**: `heig-vd-dai-course-vm`
  - **Region**: (Europe) West Europe
  - **Availability options**: No infrastructure redundancy required
  - **Security type**: Trusted launch virtual machines (the default)
  - **Image**: Ubuntu Server 24.04 LTS - x64 Gen2 (the default)
  - **VM architecture**: x64
  - **Size**: `Standard_B1s` - you might need to click _"See all sizes"_ to see
    this option
- **Administrator account**
  - **Authentication type**: SSH public key
  - **Username**: `ubuntu` - please use this username so the teaching staff can
    help you if needed
  - **SSH public key source**: Use existing public key
  - **SSH public key**: Paste your public key here - see the note below for more
    information
- **Inbound port rules**
  - **Public inbound ports**: Allow selected ports
  - **Select inbound ports**: HTTP (80), HTTPS (443), SSH (22)

Click on the `Review + create` button.

Validate the configuration and click on the `Create` button.

It might take a few minutes to create the virtual machine. Once the virtual
machine is created, you can access it with the `Go to resource` button.

Note the public IP address of the virtual machine. You will need it to connect
to the virtual machine with SSH later.


#### Configuration

TODO

### Zone DNS

TODO

#### Proof using nslookup (Validation)

TODO

### API

With `Swagger`, documentation is created during coding; see the endpoint `/swagger` or `/redoc` to look at the API documentation.

<!-- USAGE -->
#### Usage

1. Starting the database
```sh
docker compose up db -d
```
2. Setting up the database and populate it with data
```sh
docker compose run --rm prod setup --seed
```
3. Starting the web application
```sh
docker compose up prod -d
```
4. Accessing the application in a browser (tested with `Firefox`)
```sh
firefox https://ctfman.cybernest.ch
```

#### Examples using Curl

TODO

#### Examples using the UI on a web browser

TODO

<!-- CONTRIBUTING -->

## Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- LICENSE -->

## License

Distributed under the MIT License. See `LICENSE` for more information.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- CONTACTS -->

## Contacts

- [Mondotosz](https://github.com/Mondotosz)
- [Shadowkudo](https://github.com/shadowkudo)
- [Arnoheigvd](https://github.com/arnoheigvd)

> [!IMPORTANT]
> Have fun !

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->

[java]: https://adoptium.net/temurin/releases/
[maven]: https://maven.apache.org/
[docker]: https://www.docker.com/
[picocli]: https://picocli.info/
[asdf]: https://asdf-vm.com/
[postgresql]: https://www.postgresql.org/ 
[swagger]: https://swagger.io/
[javalin]: https://javalin.io/
[azure]: https://azure.microsoft.com/en-us/explore
