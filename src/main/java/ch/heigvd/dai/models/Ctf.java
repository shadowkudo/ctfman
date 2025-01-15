package ch.heigvd.dai.models;

import ch.heigvd.dai.db.DB;
import com.fasterxml.jackson.annotation.JsonValue;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.Nullable;

public class Ctf {
  private String owner;
  private String title;
  private String description;
  private String localisation;
  private Status status;
  @Nullable private Timestamp startedAt;
  @Nullable private Timestamp endedAt;

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
          .filter(s -> s.status.equals(val))
          .findFirst()
          .orElse(null);
    }
  }

  public Ctf(
      String owner,
      String title,
      String description,
      String localisation,
      Status status,
      Timestamp startedAt,
      Timestamp endedAt) {
    this.owner = owner;
    this.title = title;
    this.description = description;
    this.localisation = localisation;
    this.status = status;
    this.startedAt = startedAt;
    this.endedAt = endedAt;
  }

  public Ctf(String owner, String title, String description, String localisation, Status status) {
    this(owner, title, description, localisation, status, null, null);
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getLocalisation() {
    return localisation;
  }

  public void setLocalisation(String localisation) {
    this.localisation = localisation;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public Timestamp getStartedAt() {
    return startedAt;
  }

  public void setStartedAt(Timestamp startedAt) {
    this.startedAt = startedAt;
  }

  public Timestamp getEndedAt() {
    return endedAt;
  }

  public void setEndedAt(Timestamp endedAt) {
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
}
