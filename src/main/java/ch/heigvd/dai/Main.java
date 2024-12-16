package ch.heigvd.dai;

import java.io.File;

import picocli.CommandLine;

public class Main {

  public static void main(String[] args) {
    String jarFilename =
        // Source: https://stackoverflow.com/a/11159435
        new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath())
            .getName();

    Integer code = new CommandLine(new App()).setCommandName(jarFilename).execute(args);

    if (code != 0) {
      System.exit(code);
    }

  }

}
