package ch.heigvd.dai.commands;

import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(description = "CTF manager backend", version = "1.0.0", subcommands = {
    App.class,
    Setup.class
}, scope = CommandLine.ScopeType.INHERIT, mixinStandardHelpOptions = true)
public class Root implements Callable<Integer> {

  @Override
  public Integer call() throws Exception {

    CommandLine.usage(this, System.out);
    return -1;
  }

  public static class Options {

    @Option(names = {
        "--db-url" }, description = "The database jdbc url (default: jdbc:postgresql://<DB_HOST>:<DB_PORT:-5432>/<DB_NAME>)", defaultValue = "jdbc:postgresql://${DB_HOST}:${DB_PORT:-5432}/${DB_NAME}")
    protected String dbUrl;

    @Option(names = {
        "--db-user" }, description = "The database user (default: ${DEFAULT-VALUE})", defaultValue = "${DB_USER:-ctfman}")
    protected String dbUser;

    @Option(names = {
        "--db-password" }, description = "The database password (default: ${DEFAULT-VALUE})", defaultValue = "${DB_USER:-ctfman}")
    protected String dbPassword;
  }

}
