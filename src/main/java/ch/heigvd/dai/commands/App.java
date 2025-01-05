package ch.heigvd.dai.commands;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import ch.heigvd.dai.controllers.AuthController;
import ch.heigvd.dai.controllers.TeamsController;
import ch.heigvd.dai.controllers.UsersController;
import ch.heigvd.dai.db.DB;
import ch.heigvd.dai.models.User;
import io.javalin.Javalin;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.apibuilder.ApiBuilder.put;
import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.crud;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.redoc.ReDocPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import picocli.CommandLine;
import picocli.CommandLine.Mixin;

@CommandLine.Command(name = "serve", description = "serve the application", scope = CommandLine.ScopeType.INHERIT, mixinStandardHelpOptions = true)
public class App implements Callable<Integer> {

  @Mixin
  private Root.Options root;

  @CommandLine.Option(names = { "-a",
      "--address" }, description = "The address to listen on", defaultValue = "${SERVER_ADDRESS:-0.0.0.0}")
  private String address;

  @CommandLine.Option(names = { "-p",
      "--port" }, description = "The port to listen on", defaultValue = "${SERVER_PORT:-8080}")
  private int port;

  @Override
  public Integer call() throws Exception {

    DB.configure(root.dbUrl, root.dbUser, root.dbPassword);

    Javalin app = Javalin.create(config -> {
      config.registerPlugin(new OpenApiPlugin(pluginConfig -> {

      }));
      config.registerPlugin(new SwaggerPlugin());
      config.registerPlugin(new ReDocPlugin());

      config.router.apiBuilder(() -> {
        crud("/teams/{id}", new TeamsController());
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
