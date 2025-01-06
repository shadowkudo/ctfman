# CTFman

<a name="readme-top"></a>

<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
        <a href="#built-with">Built With</a>
    </li>
    <li>
        <a href="#Introduction">Introduction</a>
        <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#development">Development</a></li>
        <li><a href="#docker">Docker</a></li>
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

#### Development

Use the maven wrapper to install dependencies, build and package the project.

```sh
# install the dependencies
./mvnw clean install
# build
./mvnw package
# run
java -jar target/<filename>.jar --help
```

#### Docker

The application can be used with docker.

#### Github actions

If you want to test the `Github actions` on your machine, you can use [act](https://github.com/nektos/act).

Before you launch any workflow, make sure you have created a repository secret named `AUTH_TOKEN`.

Then, create a file named `.secrets` which should contain the following:

```env
AUTH_TOKEN=<YOUR_AUTH_TOKEN>
```

Finally, launch the publish workflow (which publishes the mvn package to Github repository) with the following command:

```sh
act --secret-file .secrets
```

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

<!-- CONTACT -->

## Contacts

- [Mondotosz](https://github.com/Mondotosz)
- [Shadowkudo](https://github.com/shadowkudo)
- [Arnoheigvd](https://github.com/arnoheigvd)

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
