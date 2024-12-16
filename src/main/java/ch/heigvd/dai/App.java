package ch.heigvd.dai;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import io.javalin.Javalin;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.redoc.ReDocPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import picocli.CommandLine;
import ch.heigvd.dai.controllers.*;
import ch.heigvd.dai.models.User;

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

  @Override
  public Integer call() throws Exception {
    Javalin app = Javalin.create(config -> {
      config.registerPlugin(new OpenApiPlugin(pluginConfig -> {

      }));
      config.registerPlugin(new SwaggerPlugin());
      config.registerPlugin(new ReDocPlugin());
    });

    // TODO: Database
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
    return 0;
  }
}
