package ch.heigvd.dai;

import java.util.concurrent.Callable;

import ch.heigvd.dai.controllers.AuthController;
import ch.heigvd.dai.controllers.TeamsController;
import ch.heigvd.dai.db.DB;
import ch.heigvd.dai.middlewares.AuthMiddleware;
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

@CommandLine.Command(description = "CTF manager backend", version = "1.0.0", subcommands = {
    // Maybe add a subcommand to run the database scripts
}, scope = CommandLine.ScopeType.INHERIT, mixinStandardHelpOptions = true)
public class App implements Callable<Integer> {

  @CommandLine.Option(names = { "-a",
      "--address" }, description = "The address to listen on", defaultValue = "${SERVER_ADDRESS:-0.0.0.0}")
  private String address;

  @CommandLine.Option(names = { "-p",
      "--port" }, description = "The port to listen on", defaultValue = "${SERVER_PORT:-8080}")
  private int port;

  @CommandLine.Option(names = {
      "--db-url" }, description = "The database jdbc url", defaultValue = "jdbc:postgresql://${DB_HOST}:${DB_PORT:-5432}/${DB_NAME}")
  private String dbUrl;

  @CommandLine.Option(names = {
      "--db-user" }, description = "The database user", defaultValue = "${DB_USER:-ctfman}")
  private String dbUser;

  @CommandLine.Option(names = {
      "--db-password" }, description = "The database password", defaultValue = "${DB_USER:-ctfman}")
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
        crud("/teams/{id}", new TeamsController());
      });
    });

    // Global middlewares
    app.before(new AuthMiddleware());

    // Controllers
    AuthController authController = new AuthController();

    app.get("/", ctx -> ctx.result("Hello World"));

    // Auth routes
    app.post("/login", authController::login);
    app.post("/logout", authController::logout);
    app.post("/profile", authController::profile);

    // TODO: create server config to use the address
    app.start(port);

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      System.out.println("stopping gracefully");
      DB.close();
    }));
    return 0;
  }
}
