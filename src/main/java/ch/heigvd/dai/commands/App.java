package ch.heigvd.dai.commands;

import ch.heigvd.dai.db.DB;
import ch.heigvd.dai.routing.Router;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.redoc.ReDocPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import java.util.Arrays;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Mixin;

@CommandLine.Command(
    name = "serve",
    description = "serve the application",
    scope = CommandLine.ScopeType.INHERIT,
    mixinStandardHelpOptions = true)
public class App implements Callable<Integer> {

  private static final Logger LOG = LoggerFactory.getLogger(App.class);

  @Mixin private Options options;

  @Override
  public Integer call() throws Exception {

    DB.configure(options.dbUrl, options.dbUser, options.dbPassword);

    Javalin.create(
            config -> {
              config.jetty.defaultHost = options.address;
              config.jetty.defaultPort = options.port;
              config.http.generateEtags = true;

              config.jsonMapper(
                  new JavalinJackson()
                      .updateMapper(
                          mapper -> {
                            // NOTE: this behaviour is really weird but that's the only way to have
                            // the expected behaviour. sending { "value": null } will result in the
                            // actual value being Optional.empty() while sending { } will make value
                            // == null. This is backward but we can't just inverse it through
                            // configuration so this will have to do
                            mapper
                                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                                .registerModule(new Jdk8Module().configureReadAbsentAsNull(true));
                          }));

              config.bundledPlugins.enableCors(
                  cors -> {
                    cors.addRule(
                        it -> {
                          options.cors =
                              Arrays.stream(options.cors)
                                  .filter(f -> !f.isBlank())
                                  .toArray(String[]::new);
                          if (options.cors.length == 0
                              || Arrays.stream(options.cors).anyMatch("*"::equals)) {
                            it.anyHost();
                          } else if (options.cors.length > 0) {
                            Arrays.stream(options.cors).forEach(it::allowHost);
                            it.allowCredentials = true;
                          }
                        });
                  });

              config.registerPlugin(
                  new OpenApiPlugin(
                      pluginConfig -> {
                        pluginConfig.withDefinitionConfiguration(
                            (version, openApiDefinition) -> {
                              openApiDefinition.withSecurity(
                                  security -> security.withCookieAuth("CookieAuth", "session"));
                            });
                      }));
              config.registerPlugin(new SwaggerPlugin());
              config.registerPlugin(new ReDocPlugin());

              config.router.apiBuilder(new Router());
            })
        .start();
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  LOG.info("stopping gracefully");
                  DB.close();
                }));
    return 0;
  }

  public static class Options extends Root.Options {
    @CommandLine.Option(
        names = {"-a", "--address"},
        description = "The address to listen on (default: ${DEFAULT-VALUE})",
        defaultValue = "${SERVER_ADDRESS:-0.0.0.0}")
    private String address;

    @CommandLine.Option(
        names = {"-p", "--port"},
        description = "The port to listen on (default: ${DEFAULT-VALUE})",
        defaultValue = "${SERVER_PORT:-8080}")
    private int port;

    @CommandLine.Option(
        names = "--allowed-origins",
        description =
            "The allowed-origin for Access control. Do note that using '*' will prevent"
                + " allowCredentials which will break compatibility with sites of different origin",
        split = ",",
        defaultValue = "${ALLOWED_ORIGINS:-}")
    private String[] cors;
  }
}
