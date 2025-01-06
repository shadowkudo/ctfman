package ch.heigvd.dai.commands;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.heigvd.dai.controllers.AuthController;
import ch.heigvd.dai.controllers.TeamsController;
import ch.heigvd.dai.db.DB;
import ch.heigvd.dai.middlewares.AuthMiddleware;
import ch.heigvd.dai.middlewares.SessionMiddleware;
import io.javalin.Javalin;
import static io.javalin.apibuilder.ApiBuilder.crud;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.redoc.ReDocPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import picocli.CommandLine;
import picocli.CommandLine.Mixin;

@CommandLine.Command(name = "serve", description = "serve the application", scope = CommandLine.ScopeType.INHERIT, mixinStandardHelpOptions = true)
public class App implements Callable<Integer> {

  private static final Logger LOG = LoggerFactory.getLogger(App.class);

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
      config.jetty.defaultHost = address;
      config.jetty.defaultPort = port;

      config.registerPlugin(new OpenApiPlugin(pluginConfig -> {

      }));
      config.registerPlugin(new SwaggerPlugin());
      config.registerPlugin(new ReDocPlugin());

      config.router.apiBuilder(() -> {
        crud("/teams/{id}", new TeamsController());
      });
    });

    // Global middlewares
    app.before(new SessionMiddleware());

    // Requires the user to be connected before accessing this endpoint
    app.before("/teams", new AuthMiddleware());
    app.before("/teams/*", new AuthMiddleware());

    // Controllers
    AuthController authController = new AuthController();

    app.get("/", ctx -> ctx.result("Hello World"));

    // Auth routes
    app.post("/login", authController::login);
    app.post("/logout", authController::logout);
    app.post("/profile", authController::profile);

    app.start();

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      LOG.info("stopping gracefully");
      DB.close();
    }));
    return 0;
  }
}
