package ch.heigvd.dai;

import java.io.File;
import java.util.concurrent.Callable;

import io.javalin.Javalin;
import picocli.CommandLine;

@CommandLine.Command(description = "A small CLI with subcommands to demonstrate picocli.", version = "1.0.0", subcommands = {
    // ADD commands
}, scope = CommandLine.ScopeType.INHERIT, mixinStandardHelpOptions = true)
public class Main implements Callable<Integer> {

  @CommandLine.Option(names = { "-a",
      "--address" }, description = "The address to listen on", defaultValue = "localhost")
  private String address;

  @CommandLine.Option(names = { "-p",
      "--port" }, description = "The port to listen on when using server mode", defaultValue = "8080")
  private int port;

  public static void main(String[] args) {
    String jarFilename =
        // Source: https://stackoverflow.com/a/11159435
        new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath())
            .getName();

    Integer code = new CommandLine(new Main()).setCommandName(jarFilename).execute(args);

    if (code != 0) {
      System.exit(code);
    }

  }

  @Override
  public Integer call() throws Exception {
    var app = Javalin.create(/* config */)
        .get("/", ctx -> ctx.result("Hello World"))
        .start(port);
    return 0;
  }
}
