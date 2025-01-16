package ch.heigvd.dai.controllers;

import ch.heigvd.dai.models.Ctf;
import ch.heigvd.dai.models.User;
import io.javalin.apibuilder.CrudHandler;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.HandlerType;
import io.javalin.http.HttpStatus;
import io.javalin.http.InternalServerErrorResponse;
import io.javalin.http.NotFoundResponse;
import io.javalin.http.NotImplementedResponse;
import io.javalin.openapi.*;
import io.javalin.validation.BodyValidator;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CtfsController implements CrudHandler {

  private static final Logger LOG = LoggerFactory.getLogger(CtfsController.class);

  @OpenApi(
      path = "/ctfs",
      methods = HttpMethod.GET,
      summary = "get all the ctfs",
      operationId = "getAllCTFs",
      tags = {"CTF"},
      responses = {
        @OpenApiResponse(
            status = "200",
            description = "The list of ctfs",
            content = {@OpenApiContent(from = Ctf[].class, type = ContentType.JSON)}),
        @OpenApiResponse(
            status = "401",
            description = "User not authenticated",
            content = @OpenApiContent(from = ErrorResponse.class, type = ContentType.JSON))
      })
  public void getAll(Context ctx) {
    try {
      // get all the ctfs
      List<Ctf> teams = Ctf.getAll();
      ctx.json(teams);
    } catch (SQLException ex) {
      LOG.error(ex.toString());
      throw new InternalServerErrorResponse();
    }
  }

  @OpenApi(
      path = "/ctfs",
      methods = HttpMethod.POST,
      summary = "create a ctf",
      operationId = "createCTF",
      tags = {"CTF"},
      requestBody =
          @OpenApiRequestBody(
              required = true,
              content = {@OpenApiContent(from = CtfCreateRequest.class, type = ContentType.JSON)}),
      responses = {
        @OpenApiResponse(
            status = "201",
            description = "Resource created",
            content = @OpenApiContent(from = Ctf.class, type = ContentType.JSON)),
        @OpenApiResponse(
            status = "401",
            description = "User not authenticated",
            content = @OpenApiContent(from = ErrorResponse.class, type = ContentType.JSON)),
        @OpenApiResponse(
            status = "403",
            description = "Only admins can create a ctf",
            content = @OpenApiContent(from = ErrorResponse.class, type = ContentType.JSON)),
        @OpenApiResponse(
            status = "409",
            description = "A resource with this name already exists",
            content = @OpenApiContent(from = ErrorResponse.class, type = ContentType.JSON))
      })
  public void create(Context ctx) {
    // The auth middleware already checks that the user is logged in
    User user = ctx.attribute("user");

    // Only a challenger can be the captain (and create a team)
    if (!user.hasRole(User.Role.ADMIN)) {
      throw new ForbiddenResponse();
    }

    CtfCreateRequest rq =
        ctx.bodyValidator(CtfCreateRequest.class)
            // TODO: validation
            .get();

    // TODO: insertion

    throw new NotImplementedResponse();
  }

  @OpenApi(
      path = "/ctfs/{ctf-title}",
      methods = HttpMethod.GET,
      summary = "get a ctf",
      operationId = "getOneCTF",
      tags = {"CTF"},
      pathParams = {
        @OpenApiParam(name = "ctf-title", type = String.class, description = "The ctf title")
      },
      responses = {
        @OpenApiResponse(
            status = "200",
            description = "The ctf details",
            content = {@OpenApiContent(from = Ctf.class, type = ContentType.JSON)}),
        @OpenApiResponse(
            status = "404",
            description = "No ctf with this title",
            content = @OpenApiContent(from = ErrorResponse.class, type = ContentType.JSON)),
        @OpenApiResponse(
            status = "401",
            description = "User not authenticated",
            content = @OpenApiContent(from = ErrorResponse.class, type = ContentType.JSON))
      })
  public void getOne(Context ctx, String title) {
    // TODO: maybe check access
    try {
      Ctf ctf = Ctf.getByTitle(title);

      if (ctf == null) {
        throw new NotFoundResponse();
      }

      ctx.status(HttpStatus.OK);
      ctx.json(ctf);

    } catch (SQLException ex) {
      LOG.error(ex.toString());
      throw new InternalServerErrorResponse();
    }
  }

  @OpenApi(
      path = "/ctfs/{ctf-title}",
      methods = {HttpMethod.PUT, HttpMethod.PATCH},
      summary = "update a ctf",
      operationId = "updateCTF",
      tags = {"CTF"},
      pathParams = {
        @OpenApiParam(name = "ctf-title", type = String.class, description = "The ctf title")
      },
      requestBody =
          @OpenApiRequestBody(
              description =
                  "When using PUT, all fields are required. When using PATCH, fields can be omitted"
                      + " in which case they will keep their current value",
              required = true,
              content = {@OpenApiContent(from = CtfCreateRequest.class, type = ContentType.JSON)}),
      responses = {
        @OpenApiResponse(
            status = "200",
            description = "Update successful",
            content = {@OpenApiContent(from = Ctf.class, type = ContentType.JSON)}),
        @OpenApiResponse(
            status = "401",
            description = "User not authenticated",
            content = @OpenApiContent(from = ErrorResponse.class, type = ContentType.JSON)),
        @OpenApiResponse(
            status = "403",
            description = "User isn't the owner of the ctf",
            content = @OpenApiContent(from = ErrorResponse.class, type = ContentType.JSON))
      })
  public void update(Context ctx, String title) {
    boolean isPatch = ctx.method().equals(HandlerType.PATCH);
    // The auth middleware already checks that the user is logged in
    User user = ctx.attribute("user");

    // Only a challenger can be the captain (and create a team)
    if (!user.hasRole(User.Role.ADMIN)) {
      throw new ForbiddenResponse();
    }

    BodyValidator<CtfUpdateRequest> validator = ctx.bodyValidator(CtfUpdateRequest.class);

    if (isPatch) { // PATCH
      // TODO: patch validation
    } else { // PUT
      // TODO: put validation
    }

    CtfUpdateRequest rq = validator.get();

    LOG.debug(rq.toString());

    // Check that the user is the team captain
    try {
      Ctf ctf = Ctf.getByTitle(title);

      if (ctf == null) {
        throw new NotFoundResponse();
      }

      if (!ctf.getOwner().equals(user.getAuthentication())) {
        throw new ForbiddenResponse();
      }

      // TODO: rest of the update

      ctx.json(ctf);

    } catch (SQLException ex) {
      LOG.error(ex.toString());
      throw new InternalServerErrorResponse();
    }
  }

  @OpenApi(
      path = "/ctfs/{ctf-title}",
      methods = HttpMethod.DELETE,
      summary = "delete a ctf",
      operationId = "deleteCTF",
      tags = {"CTF"},
      pathParams = {
        @OpenApiParam(name = "ctf-title", type = String.class, description = "The ctf title")
      },
      responses = {
        @OpenApiResponse(status = "204", description = "Successfully deleted resource"),
        @OpenApiResponse(
            status = "401",
            description = "User not authenticated",
            content = @OpenApiContent(from = ErrorResponse.class, type = ContentType.JSON)),
        @OpenApiResponse(
            status = "403",
            description = "Not the owner of the ctf",
            content = @OpenApiContent(from = ErrorResponse.class, type = ContentType.JSON))
      })
  public void delete(Context ctx, String title) {

    // The auth middleware already checks that the user is logged in
    User user = ctx.attribute("user");

    // Only an admin can delete the team for now
    if (!user.hasRole(User.Role.ADMIN)) {
      throw new ForbiddenResponse();
    }

    try {
      Ctf ctf = Ctf.getByTitle(title);

      if (ctf == null) {
        throw new NotFoundResponse();
      }

      if (!ctf.getOwner().equals(user.getAuthentication())) {
        throw new ForbiddenResponse();
      }

      // TODO: delete

      ctx.status(HttpStatus.NO_CONTENT);

    } catch (SQLException ex) {
      LOG.error(ex.toString());
      throw new InternalServerErrorResponse();
    }
  }

  public static record CtfCreateRequest(
      String title,
      String description,
      String localisation,
      Ctf.Status status,
      Instant start,
      Instant end) {}

  public static record CtfUpdateRequest(
      Optional<String> title,
      Optional<String> description,
      Optional<String> localisation,
      Optional<Ctf.Status> status,
      Optional<Instant> start,
      Optional<Instant> end) {}
}
