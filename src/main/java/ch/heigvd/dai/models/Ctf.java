package ch.heigvd.dai.models;

import ch.heigvd.dai.db.DB;
import com.fasterxml.jackson.annotation.JsonValue;
import io.javalin.openapi.OpenApiExample;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

public class Ctf {
  private String owner;
  private String title;
  private String description;
  private String localisation;
  private Status status;
  @Nullable private Instant startedAt;
  @Nullable private Instant endedAt;
  private String oldTitle;

  public static enum Status {
    WIP("wip"),
    READY("ready"),
    IN_PROGRESS("in progress"),
    FINISHED("finished");

    private String status;

    private Status(String status) {
      this.status = status;
    }

    @JsonValue
    public String getStatus() {
      return this.status;
    }

    public static Status fromString(String val) {
      return Arrays.stream(Status.values())
          .filter(s -> s.status.equalsIgnoreCase(val))
          .findFirst()
          .orElse(null);
    }

    @Override
    public String toString() {
      return this.status;
    }
  }

  public Ctf(
      String owner,
      String title,
      String description,
      String localisation,
      Status status,
      Instant startedAt,
      Instant endedAt) {
    this(owner, title, description, localisation, status);
    this.startedAt = startedAt;
    this.endedAt = endedAt;
  }

  public Ctf(
      String owner,
      String title,
      String description,
      String localisation,
      Status status,
      @Nullable Timestamp startedAt,
      @Nullable Timestamp endedAt) {
    this(
        owner,
        title,
        description,
        localisation,
        status,
        Optional.ofNullable(startedAt).map(Timestamp::toInstant).orElse(null),
        Optional.ofNullable(endedAt).map(Timestamp::toInstant).orElse(null));
  }

  public Ctf(String owner, String title, String description, String localisation, Status status) {

    this.owner = owner;
    this.title = title;
    this.description = description;
    this.localisation = localisation;
    this.status = status;
    this.oldTitle = this.title;
  }

  @OpenApiExample(value = "Admin1")
  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  @OpenApiExample(value = "DEFCON")
  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  @OpenApiExample(
      value =
          "The largest hacking and security conference with presentations, workshops,"
              + " contests, villages and the premier Capture The Flag Contest.")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @OpenApiExample(value = "Las Vegas")
  public String getLocalisation() {
    return localisation;
  }

  public void setLocalisation(String localisation) {
    this.localisation = localisation;
  }

  @OpenApiExample(value = "wip")
  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public Instant getStartedAt() {
    return startedAt;
  }

  public void setStartedAt(Instant startedAt) {
    this.startedAt = startedAt;
  }

  public Instant getEndedAt() {
    return endedAt;
  }

  public void setEndedAt(Instant endedAt) {
    this.endedAt = endedAt;
  }

  private static Ctf fromResultSet(ResultSet res) throws SQLException {
    return new Ctf(
        res.getString("admin"),
        res.getString("title"),
        res.getString("description"),
        res.getString("localisation"),
        Status.fromString(res.getString("status")),
        res.getTimestamp("started_at"),
        res.getTimestamp("ended_at"));
  }

  public static List<Ctf> getAll() throws SQLException {
    List<Ctf> ctfs = new ArrayList<>();
    String query = "SELECT * FROM ctf";

    try (Connection conn = DB.getConnection()) {
      PreparedStatement stmt = conn.prepareStatement(query);
      ResultSet res = stmt.executeQuery();

      while (res.next()) {
        ctfs.add(fromResultSet(res));
      }
    }

    return ctfs;
  }

  public static @Nullable Ctf getByTitle(String title) throws SQLException {
    String query = "SELECT * FROM ctf WHERE title = ?";

    try (Connection conn = DB.getConnection()) {
      PreparedStatement stmt = conn.prepareStatement(query);
      stmt.setString(1, title);
      ResultSet res = stmt.executeQuery();

      if (!res.next()) {
        return null;
      }

      return fromResultSet(res);
    }
  }

  public int insert() throws SQLException {

    String query =
        "INSERT INTO ctf (title, description, admin, localisation, status, started_at, ended_at)"
            + " VALUES(?, ?, ?, ?, ?, ?, ?)";

    try (Connection conn = DB.getConnection()) {
      PreparedStatement stmt = conn.prepareStatement(query);
      stmt.setString(1, this.title);
      stmt.setString(2, this.description);
      stmt.setString(3, this.owner);
      stmt.setString(4, this.localisation);

      stmt.setObject(5, this.status.toString(), java.sql.Types.OTHER);
      stmt.setTimestamp(6, Timestamp.from(this.startedAt));
      stmt.setTimestamp(7, Timestamp.from(this.endedAt));

      return stmt.executeUpdate();
    }
  }

  public int update() throws SQLException {
    String query =
        "UPDATE ctf SET title = ?, description = ?, admin = ?, localisation = ?, started_at = ?,"
            + " ended_at = ? WHERE title = ?";

    try (Connection conn = DB.getConnection()) {
      PreparedStatement stmt = conn.prepareStatement(query);

      stmt.setString(1, title);
      stmt.setString(2, description);
      stmt.setString(3, owner);
      stmt.setString(4, localisation);
      stmt.setTimestamp(5, Optional.ofNullable(startedAt).map(Timestamp::from).orElse(null));
      stmt.setTimestamp(6, Optional.ofNullable(endedAt).map(Timestamp::from).orElse(null));
      stmt.setString(7, oldTitle);

      return stmt.executeUpdate();
    }
  }

  public int delete() throws SQLException {

    String query = "DELETE FROM ctf WHERE title = ?";

    try (Connection conn = DB.getConnection()) {
      PreparedStatement stmt = conn.prepareStatement(query);
      stmt.setString(1, this.title);

      return stmt.executeUpdate();
    }
  }
}
