package ch.heigvd.dai.controllers;

import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.heigvd.dai.models.Team;
import io.javalin.apibuilder.CrudHandler;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.InternalServerErrorResponse;
import io.javalin.http.MethodNotAllowedResponse;
import io.javalin.http.NotFoundResponse;
import io.javalin.openapi.*;

public class TeamsController implements CrudHandler {

  private static final Logger LOG = LoggerFactory.getLogger(TeamsController.class);

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
      LOG.error(ex.toString());
      throw new InternalServerErrorResponse();
    }

  }

  @OpenApi(path = "/teams", methods = HttpMethod.POST, summary = "create a team", operationId = "createTeam", tags = {
      "Team" })
  public void create(Context ctx) {
    throw new MethodNotAllowedResponse();
  }

  @OpenApi(path = "/teams/{teamName}", methods = HttpMethod.GET, summary = "get a team", operationId = "getOneTeam", tags = {
      "Team" }, pathParams = {
          @OpenApiParam(name = "teamName", type = String.class, description = "The team name") }, responses = {
              @OpenApiResponse(status = "200", description = "The team details", content = {
                  @OpenApiContent(from = Team.class) }),
              @OpenApiResponse(status = "404", description = "No team with this name"),
              @OpenApiResponse(status = "403", description = "User not authenticated")
          })
  public void getOne(Context ctx, String teamName) {
    try {
      Team team = Team.getByName(teamName);

      if (team == null) {
        throw new NotFoundResponse();
      }

      ctx.status(HttpStatus.OK);
      ctx.json(team);

    } catch (SQLException ex) {
      LOG.error(ex.toString());
      throw new InternalServerErrorResponse();
    }
  }

  @OpenApi(path = "/teams/{id}", methods = { HttpMethod.PUT,
      HttpMethod.PATCH }, summary = "update a team", operationId = "updateTeam", tags = { "Team" })
  public void update(Context ctx, String id) {
    throw new MethodNotAllowedResponse();
  }

  @OpenApi(path = "/teams/{id}", methods = HttpMethod.DELETE, summary = "delete a team", operationId = "deleteTeam", tags = {
      "Team" })
  public void delete(Context ctx, String id) {
    throw new MethodNotAllowedResponse();
  }
}
