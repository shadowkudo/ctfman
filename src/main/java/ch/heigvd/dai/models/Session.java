package ch.heigvd.dai.models;

import ch.heigvd.dai.db.DB;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiNullable;
import io.javalin.openapi.OpenApiPropertyType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Session {
  @JsonIgnore protected String token;
  @JsonIgnore protected String userAccount;
  protected Timestamp expiresAt;
  protected String ipAddress;
  protected String userAgent;

  public Session(
      @NotNull String token,
      @NotNull String userAccount,
      @NotNull Timestamp expiresAt,
      @Nullable String ipAddress,
      @Nullable String userAgent) {
    this.token = token;
    this.userAccount = userAccount;
    this.expiresAt = expiresAt;
    this.ipAddress = ipAddress;
    this.userAgent = userAgent;
  }

  public Session(@NotNull String token, @NotNull String userAccount, @NotNull Timestamp expiresAt) {
    this(token, userAccount, expiresAt, null, null);
  }

  @OpenApiExample("Fx1OcCTAwBGKfCC106Vrs48XKCcUJ371SthgQUgF6kM")
  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getUserAccount() {
    return userAccount;
  }

  public void setUserAccount(String userAccount) {
    this.userAccount = userAccount;
  }

  @OpenApiPropertyType(definedBy = Instant.class)
  public Timestamp getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(Timestamp expiresAt) {
    this.expiresAt = expiresAt;
  }

  @OpenApiExample("127.0.0.1")
  @OpenApiNullable
  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  @OpenApiExample("Mozilla/5.0 (X11; Linux x86_64; rv:134.0) Gecko/20100101 Firefox/134.0")
  @OpenApiNullable
  public String getUserAgent() {
    return userAgent;
  }

  public void setUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }

  public static @Nullable Session findByToken(@NotNull String token) throws SQLException {

    String query = "SELECT * FROM session WHERE token = ?";
    try (Connection conn = DB.getConnection()) {
      PreparedStatement stmt = conn.prepareStatement(query);
      stmt.setString(1, token);
      ResultSet res = stmt.executeQuery();

      if (res.next()) {
        return new Session(
            res.getString("token"),
            res.getString("user_account"),
            res.getTimestamp("expires_at"),
            res.getString("ip_address"),
            res.getString("user_agent"));
      }
    }

    return null;
  }

  /** Get all the sessions of an user */
  public static @Nullable List<Session> findAllUserSessions(@NotNull String user)
      throws SQLException {
    List<Session> sessions = new ArrayList<>();
    String query = "SELECT * FROM session WHERE user_account = ?";
    try (Connection conn = DB.getConnection()) {
      PreparedStatement stmt = conn.prepareStatement(query);
      stmt.setString(1, user);
      ResultSet res = stmt.executeQuery();

      while (res.next()) {
        sessions.add(
            new Session(
                res.getString("token"),
                res.getString("user_account"),
                res.getTimestamp("expires_at"),
                res.getString("ip_address"),
                res.getString("user_agent")));
      }
    }

    return sessions;
  }

  /**
   * Insert the model in the database
   *
   * @throws SQLException
   * @return the number of rows inserted (1 or 0)
   */
  public int insert() throws SQLException {

    String query =
        "INSERT INTO session (token, user_account, expires_at, ip_address, user_agent) VALUES"
            + " (?,?,?,?,?)";
    try (Connection conn = DB.getConnection()) {
      PreparedStatement stmt = conn.prepareStatement(query);
      stmt.setString(1, token);
      stmt.setString(2, userAccount);
      stmt.setTimestamp(3, expiresAt);
      stmt.setString(4, ipAddress);
      stmt.setString(5, userAgent);
      return stmt.executeUpdate();
    }
  }

  /**
   * Delete a session using its token and user_account (prevents deleting a token that might not be
   * linked to the user) @TODO: Check if we should only require the token since the controller
   * should check beforehand
   */
  public static int delete(@NotNull String token, @NotNull String user) throws SQLException {

    String query = "DELETE FROM session WHERE token = ? AND user_account = ?";
    try (Connection conn = DB.getConnection()) {
      PreparedStatement stmt = conn.prepareStatement(query);
      stmt.setString(1, token);
      stmt.setString(2, user);
      return stmt.executeUpdate();
    }
  }
}
