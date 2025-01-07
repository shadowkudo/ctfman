package ch.heigvd.dai.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.heigvd.dai.db.DB;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "setup", description = "setup the database", scope = CommandLine.ScopeType.INHERIT, mixinStandardHelpOptions = true)
public class Setup implements Callable<Integer> {

  private static final Logger LOG = LoggerFactory.getLogger(Setup.class);

  @Mixin
  private Options options;

  @Override
  public Integer call() throws Exception {
    DB.configure(options.dbUrl, options.dbUser, options.dbPassword);

    try (Connection conn = DB.getConnection()) {
      ArrayList<String> scripts = new ArrayList<>(
          Arrays.asList("01_create_tables.sql", "02_triggers.sql", "04_views.sql"));

      if (options.seed) {
        scripts.add("03_populate.sql");
      }

      for (String script : scripts) {
        runScript(conn, script);
      }

    } catch (Exception ex) {
      LOG.error(ex.toString());
      return -1;
    }

    return 0;
  }

  private void runScript(Connection conn, String script) throws SQLException, IOException {
    LOG.info("Executing script: " + script);

    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(getClass().getClassLoader().getResourceAsStream("sql/" + script)))) {

      String sql = reader.lines().collect(Collectors.joining("\n"));
      conn.createStatement().execute(sql);

    }
  }

  public static class Options extends Root.Options {
    @Option(names = "--seed", description = "fill the database with dummy data (default: ${DEFAULT-VALUE})", defaultValue = "false")
    private boolean seed;
  }

}
