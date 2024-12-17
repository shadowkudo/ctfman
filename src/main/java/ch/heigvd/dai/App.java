package ch.heigvd.dai;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.simple.SimpleLogger;
import org.slf4j.simple.SimpleLoggerFactory;

import io.javalin.Javalin;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.redoc.ReDocPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import picocli.CommandLine;
import ch.heigvd.dai.controllers.*;
import ch.heigvd.dai.db.DB;
import ch.heigvd.dai.models.User;
import static io.javalin.apibuilder.ApiBuilder.*;

@CommandLine.Command(description = "CTF manager backend", version = "1.0.0", subcommands = {
    // Maybe add a subcommand to run the database scripts
}, scope = CommandLine.ScopeType.INHERIT, mixinStandardHelpOptions = true)
public class App implements Callable<Integer> {

  @CommandLine.Option(names = { "-a",
      "--address" }, description = "The address to listen on", defaultValue = "localhost")
  private String address;

  @CommandLine.Option(names = { "-p",
      "--port" }, description = "The port to listen on", defaultValue = "8080")
  private int port;

  @CommandLine.Option(names = {
      "--db-url" }, description = "The database jdbc url", defaultValue = "jdbc:postgresql://db:5432/ctfman")
  private String dbUrl;

  @CommandLine.Option(names = {
      "--db-user" }, description = "The database user", defaultValue = "ctfman")
  private String dbUser;

  @CommandLine.Option(names = {
      "--db-password" }, description = "The database password", defaultValue = "ctfman")
  private String dbPassword;

  @Override
  public Integer call() throws Exception {

    DB.configure(dbUrl, dbUser, dbPassword);

    Javalin app = Javalin.create(config -> {
      config.registerPlugin(new OpenApiPlugin(pluginConfig -> {

      }));
      config.registerPlugin(new SwaggerPlugin());
      config.registerPlugin(new ReDocPlugin());

      config.router.apiBuilder(() -> {
        path("/teams", () -> {
          get(TeamsController::index);
        });
      });
    });

    ConcurrentHashMap<Integer, User> users = new ConcurrentHashMap<>();

    // Controllers
    AuthController authController = new AuthController(users);
    UsersController usersController = new UsersController(users);

    app.get("/", ctx -> ctx.result("Hello World"));

    // Auth routes
    app.post("/login", authController::login);
    app.post("/logout", authController::logout);
    app.get("/profile", authController::profile);

    // Users routes
    app.post("/users", usersController::create);
    app.get("/users", usersController::getMany);
    app.get("/users/{id}", usersController::getOne);
    app.put("/users/{id}", usersController::update);
    app.delete("/users/{id}", usersController::delete);

    // TODO: create server config to use the address
    app.start(port);

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      System.out.println("stopping gracefully");
      DB.close();
    }));
    return 0;
  }
}
