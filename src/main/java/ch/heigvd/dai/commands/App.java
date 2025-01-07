package ch.heigvd.dai.commands;

import java.util.concurrent.Callable;

import ch.heigvd.dai.controllers.AuthController;
import ch.heigvd.dai.controllers.TeamsController;
import ch.heigvd.dai.controllers.UsersController;
import ch.heigvd.dai.db.DB;
import ch.heigvd.dai.middlewares.AuthMiddleware;
import ch.heigvd.dai.middlewares.SessionMiddleware;
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
      config.router.apiBuilder(() -> {
        crud("/users/{id}", new UsersController());
      });

    });

    // Global middlewares
    app.before(new SessionMiddleware());

    // Requires the user to be connected before accessing this endpoint
    app.before("/teams", new AuthMiddleware());
    app.before("/teams/*", new AuthMiddleware());
    app.before("/users", new AuthMiddleware());
    app.before("/users/*", new AuthMiddleware());


    // Controllers
    AuthController authController = new AuthController();

    app.get("/", ctx -> ctx.result("Hello World"));

    // Auth routes
    app.post("/login", authController::login);
    app.post("/logout", authController::logout);
    app.post("/profile", authController::profile);

    // User interaction


    // TODO: create server config to use the address
    app.start(port);

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      System.out.println("stopping gracefully");
      DB.close();
    }));
    return 0;
  }
}
