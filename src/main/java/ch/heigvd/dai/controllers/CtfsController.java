package ch.heigvd.dai.controllers;

import ch.heigvd.dai.models.Ctf;
import ch.heigvd.dai.models.User;
import ch.heigvd.dai.utils.Validation;
import io.javalin.apibuilder.CrudHandler;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.HandlerType;
import io.javalin.http.HttpStatus;
import io.javalin.http.InternalServerErrorResponse;
import io.javalin.http.NotFoundResponse;
import io.javalin.openapi.*;
import io.javalin.validation.BodyValidator;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
      security = {@OpenApiSecurity(name = "CookieAuth")},
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
      security = {@OpenApiSecurity(name = "CookieAuth")},
      requestBody =
          @OpenApiRequestBody(
              required = true,
              description =
                  "The startedAt and endedAt fields are optional and can be omitted or null. The"
                      + " status must be one of ['wip','in progress','ready','finished']",
              content = {@OpenApiContent(from = CreateRequest.class, type = ContentType.JSON)}),
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

    if (!user.hasRole(User.Role.ADMIN)) {
      throw new ForbiddenResponse();
    }

    BodyValidator<CreateRequest> precheck =
        ctx.bodyValidator(CreateRequest.class)
            // Prechecks for null (missing fields)
            .check(it -> it.title != null, "Missing title field")
            .check(it -> it.description != null, "Missing description field")
            .check(it -> it.localisation != null, "Missing localisation field")
            .check(it -> it.status != null, "Missing status field");

    if (!precheck.errors().isEmpty()) {
      precheck.get();
      return;
    }

    CreateRequest rq =
        ctx.bodyValidator(CreateRequest.class)
            .check(
                it -> it.title.map(Validation::notBlank).orElse(false),
                "The title is required and cannot be left empty")
            .check(
                it -> it.description.map(Validation::notBlank).orElse(false),
                "The description is required and cannot be left empty")
            .check(
                it -> it.localisation.map(Validation::notBlank).orElse(false),
                "The localisation is required and cannot be left empty")
            .check(
                it -> it.status.map(Ctf.Status::fromString).isPresent(),
                "The status must be one of ["
                    + Stream.of(Ctf.Status.values())
                        .map(Ctf.Status::toString)
                        .collect(Collectors.joining(","))
                    + "]")
            .check(
                it ->
                    it.start == null
                        || it.end == null
                        || it.start.isEmpty()
                        || it.end.isEmpty()
                        || it.start.get().isBefore(it.end.get()),
                "Start date must be before end date")
            .get();

    Ctf ctf =
        new Ctf(
            user.getAuthentication(),
            rq.title.get(),
            rq.description.get(),
            rq.localisation.get(),
            rq.status.map(Ctf.Status::fromString).get(),
            rq.start.orElse(null),
            rq.end.orElse(null));

    try {
      if (ctf.insert() != 1) {
        throw new InternalServerErrorResponse();
      }
    } catch (SQLException ex) {
      LOG.error(ex.toString());
      throw new InternalServerErrorResponse();
    }

    ctx.status(HttpStatus.CREATED);
    ctx.json(ctf);
  }

  @OpenApi(
      path = "/ctfs/{ctf-title}",
      methods = HttpMethod.GET,
      summary = "get a ctf",
      operationId = "getOneCTF",
      tags = {"CTF"},
      security = {@OpenApiSecurity(name = "CookieAuth")},
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
      security = {@OpenApiSecurity(name = "CookieAuth")},
      pathParams = {
        @OpenApiParam(name = "ctf-title", type = String.class, description = "The ctf title")
      },
      requestBody =
          @OpenApiRequestBody(
              description =
                  "When using PUT, all fields are required. When using PATCH, fields can be omitted"
                      + " in which case they will keep their current value",
              required = true,
              content = {@OpenApiContent(from = UpdateRequest.class, type = ContentType.JSON)}),
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
    boolean isPut = ctx.method().equals(HandlerType.PUT);
    // The auth middleware already checks that the user is logged in
    User user = ctx.attribute("user");

    // Only a challenger can be the captain (and create a team)
    if (!user.hasRole(User.Role.ADMIN)) {
      throw new ForbiddenResponse();
    }

    BodyValidator<UpdateRequest> validator = ctx.bodyValidator(UpdateRequest.class);

    if (isPut) {
      BodyValidator<CreateRequest> precheck =
          ctx.bodyValidator(CreateRequest.class)
              // Prechecks for null (missing fields)
              .check(it -> it.title != null, "Missing title field")
              .check(it -> it.description != null, "Missing description field")
              .check(it -> it.localisation != null, "Missing localisation field")
              .check(it -> it.status != null, "Missing status field")
              .check(it -> it.start != null, "Missing start field")
              .check(it -> it.end != null, "Missing end field");

      if (!precheck.errors().isEmpty()) {
        precheck.get();
        return;
      }
    }

    // Redundant null checks but this is less code to handle both put/patch
    validator
        .check(
            it -> it.title == null || it.title.map(Validation::notBlank).orElse(false),
            "Title cannot be empty")
        .check(
            it -> it.description == null || it.description.map(Validation::notBlank).orElse(false),
            "Description cannot be empty")
        .check(
            it ->
                it.localisation == null || it.localisation.map(Validation::notBlank).orElse(false),
            "Localisation cannot be empty")
        .check(
            it -> it.status == null || it.status.map(Ctf.Status::fromString).isPresent(),
            "The status must be one of ["
                + Stream.of(Ctf.Status.values())
                    .map(Ctf.Status::toString)
                    .collect(Collectors.joining(","))
                + "]")
        .check(
            it ->
                it.start == null
                    || it.end == null
                    || it.start.isEmpty()
                    || it.end.isEmpty()
                    || it.start.get().isBefore(it.end.get()),
            "Start date must be before end date");

    UpdateRequest rq = validator.get();

    LOG.debug(rq.toString());

    try {
      Ctf ctf = Ctf.getByTitle(title);

      if (ctf == null) {
        throw new NotFoundResponse();
      }

      if (!ctf.getOwner().equals(user.getAuthentication())) {
        throw new ForbiddenResponse();
      }

      if (rq.title != null) {
        ctf.setTitle(rq.title.get());
      }

      if (rq.description != null) {
        ctf.setDescription(rq.description.get());
      }

      if (rq.localisation != null) {
        ctf.setLocalisation(rq.localisation.get());
      }

      if (rq.status != null) {
        ctf.setStatus(rq.status.map(Ctf.Status::fromString).get());
      }

      if (rq.start != null) {
        ctf.setStartedAt(rq.start.orElse(null));
      }

      if (rq.end != null) {
        ctf.setEndedAt(rq.end.orElse(null));
      }

      if (ctf.update() != 1) {
        throw new InternalServerErrorResponse();
      }

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
      security = {@OpenApiSecurity(name = "CookieAuth")},
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

      if (ctf.delete() != 1) {
        throw new InternalServerErrorResponse();
      }

      ctx.status(HttpStatus.NO_CONTENT);

    } catch (SQLException ex) {
      LOG.error(ex.toString());
      throw new InternalServerErrorResponse();
    }
  }

  @OpenApiName("CtfCreateRequest")
  public static record CreateRequest(
      @OpenApiPropertyType(definedBy = String.class)
          @OpenApiDescription("The title of the ctf. Will be used as its primary key")
          @OpenApiExample(value = "DEFCON")
          @OpenApiStringValidation(minLength = "1", maxLength = "256")
          @OpenApiRequired()
          Optional<String> title,
      @OpenApiPropertyType(definedBy = String.class)
          @OpenApiExample(
              value =
                  "The largest hacking and security conference with presentations, workshops,"
                      + " contests, villages and the premier Capture The Flag Contest.")
          @OpenApiRequired()
          Optional<String> description,
      @OpenApiPropertyType(definedBy = String.class)
          @OpenApiExample(value = "Las Vegas")
          @OpenApiRequired()
          Optional<String> localisation,
      @OpenApiPropertyType(definedBy = Ctf.Status.class)
          @OpenApiExample(value = "wip")
          @OpenApiRequired()
          Optional<String> status,
      @OpenApiPropertyType(definedBy = Instant.class) @OpenApiNullable() Optional<Instant> start,
      @OpenApiPropertyType(definedBy = Instant.class) @OpenApiNullable() Optional<Instant> end) {}

  @OpenApiName("CtfUpdateRequest")
  public static record UpdateRequest(
      @OpenApiPropertyType(definedBy = String.class)
          @OpenApiDescription("The title of the ctf. Will be used as its primary key")
          @OpenApiExample(value = "DEFCON")
          @OpenApiStringValidation(minLength = "1", maxLength = "256")
          Optional<String> title,
      @OpenApiPropertyType(definedBy = String.class)
          @OpenApiExample(
              value =
                  "The largest hacking and security conference with presentations, workshops,"
                      + " contests, villages and the premier Capture The Flag Contest.")
          Optional<String> description,
      @OpenApiPropertyType(definedBy = String.class) @OpenApiExample(value = "Las Vegas")
          Optional<String> localisation,
      @OpenApiPropertyType(definedBy = Ctf.Status.class) @OpenApiExample(value = "wip")
          Optional<String> status,
      @OpenApiPropertyType(definedBy = Instant.class) @OpenApiNullable() Optional<Instant> start,
      @OpenApiPropertyType(definedBy = Instant.class) @OpenApiNullable() Optional<Instant> end) {}
}
