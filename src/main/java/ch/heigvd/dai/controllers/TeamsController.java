package ch.heigvd.dai.controllers;

import java.sql.SQLException;
import java.util.List;

import ch.heigvd.dai.models.Team;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

public class TeamsController {

  public static void index(Context ctx) {
    try {
      // get all the teams
      List<Team> teams = Team.getAll();
      ctx.json(teams);
    } catch (SQLException ex) {
      ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
    }

  }
}
