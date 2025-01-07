package ch.heigvd.dai.models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import ch.heigvd.dai.db.DB;

public class User extends Authentication {

  private String authentication;
  private String primaryContact;
  private Boolean isChallenger;
  private Boolean isAdmin;
  private Boolean isModerator;
  private Boolean isAuthor;

  // TODO: photo

  public User(String name, String primaryContact) {
    this(name, primaryContact, null, null, null, null, null, null, null);
  }

  public User(String name, String primaryContact, String passwordHash, Timestamp createdAt, Timestamp deletedAt,
      Boolean isChallenger, Boolean isAdmin, Boolean isModerator, Boolean isAuthor) {
    super(passwordHash, createdAt, deletedAt);
    this.authentication = name;
    this.primaryContact = primaryContact;
    this.isChallenger = isChallenger;
    this.isAdmin = isAdmin;
    this.isModerator = isModerator;
    this.isAuthor = isAuthor;
  }

  /**
   * Find a user by name.
   * This includes information on whether the user is a
   * challenger/admin/moderator/author
   */
  public static @Nullable User findByName(@NotNull String name) throws SQLException {

    String query = "SELECT auth.*, ua.*, CASE WHEN chal.user_account IS NOT NULL THEN TRUE ELSE FALSE END is_challenger, CASE WHEN admin.manager IS NOT NULL THEN TRUE ELSE FALSE END is_admin, CASE WHEN mod.manager IS NOT NULL THEN TRUE ELSE FALSE END is_moderator, CASE WHEN aut.manager IS NOT NULL THEN TRUE ELSE FALSE END is_author FROM user_account ua JOIN authentication auth ON auth.identification = ua.authentication LEFT JOIN manager man ON man.user_account = ua.authentication LEFT JOIN challenger chal ON chal.user_account = ua.authentication LEFT JOIN admin ON admin.manager = man.user_account LEFT JOIN moderator mod ON mod.manager = man.user_account LEFT JOIN author aut ON aut.manager = man.user_account WHERE authentication = ?";
    try (Connection conn = DB.getConnection()) {
      PreparedStatement stmt = conn.prepareStatement(query);
      stmt.setString(1, name);
      ResultSet res = stmt.executeQuery();

      if (res.next()) {
        return new User(
            res.getString("authentication"),
            res.getString("primary_contact"),
            res.getString("password_hash"),
            res.getTimestamp("created_at"),
            res.getTimestamp("deleted_at"),
            res.getBoolean("is_challenger"),
            res.getBoolean("is_admin"),
            res.getBoolean("is_moderator"),
            res.getBoolean("is_author"));
      }

    }

    return null;
  }

  public static List<User> getAll(Role role) throws SQLException {
    List<User> list = new ArrayList<>();
    String query;

    if (role == null) {
      query ="SELECT DISTINCT * FROM user_account ua WHERE ua.";
    }

    try (Connection conn = DB.getConnection()) {

    }
    return null;
  }

  public String getAuthentication() {
    return authentication;
  }

  public void setAuthentication(String authentication) {
    this.authentication = authentication;
  }

  public String getPrimaryContact() {
    return primaryContact;
  }

  public void setPrimaryContact(String primaryContact) {
    this.primaryContact = primaryContact;
  }

  public Boolean getIsChallenger() {
    return isChallenger;
  }

  public void setIsChallenger(Boolean isChallenger) {
    this.isChallenger = isChallenger;
  }

  public Boolean getIsAdmin() {
    return isAdmin;
  }

  public void setIsAdmin(Boolean isAdmin) {
    this.isAdmin = isAdmin;
  }

  public Boolean getIsModerator() {
    return isModerator;
  }

  public void setIsModerator(Boolean isModerator) {
    this.isModerator = isModerator;
  }

  public Boolean getIsAuthor() {
    return isAuthor;
  }

  public void setIsAuthor(Boolean isAuthor) {
    this.isAuthor = isAuthor;
  }

  public Boolean hasRole(Role role) {
    return switch (role) {
      case ADMIN -> isAdmin;
      case AUTHOR -> isAuthor;
      case CHALLENGER -> isChallenger;
      case MODERATOR -> isModerator;
    };
  }

  public static enum Role {
    CHALLENGER, ADMIN, MODERATOR, AUTHOR
  }

}
