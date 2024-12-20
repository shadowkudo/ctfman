package ch.heigvd.dai.controllers;

import java.sql.SQLException;
import java.util.List;

import ch.heigvd.dai.models.Team;
import io.javalin.apibuilder.CrudHandler;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.openapi.*;

public class TeamsController implements CrudHandler {

  @OpenApi(path = "/teams", methods = HttpMethod.GET, summary = "get all teams", operationId = "getAllTeams", tags = {
      "Team" }, responses = {
          @OpenApiResponse(status = "200", content = { @OpenApiContent(from = Team[].class) })
      })
  public void getAll(Context ctx) {
    try {
      // get all the teams
      List<Team> teams = Team.getAll();
      ctx.json(teams);
    } catch (SQLException ex) {
      ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
    }

  }

  @OpenApi(path = "/teams", methods = HttpMethod.POST, summary = "create a team", operationId = "createTeam", tags = {
      "Team" })
  public void create(Context ctx) {
    ctx.json("create");
  }

  @OpenApi(path = "/teams/{id}", methods = HttpMethod.GET, summary = "get a team", operationId = "getOneTeam", tags = {
      "Team" }, responses = {
          @OpenApiResponse(status = "200", content = { @OpenApiContent(from = Team.class) })
      })
  public void getOne(Context ctx, String id) {
    ctx.json("getOne");
  }

  @OpenApi(path = "/teams/{id}", methods = { HttpMethod.PUT,
      HttpMethod.PATCH }, summary = "update a team", operationId = "updateTeam", tags = { "Team" })
  public void update(Context ctx, String id) {
    ctx.json("update");
  }

  @OpenApi(path = "/teams/{id}", methods = HttpMethod.DELETE, summary = "delete a team", operationId = "deleteTeam", tags = {
      "Team" })
  public void delete(Context ctx, String id) {
    ctx.json("delete");
  }
}
