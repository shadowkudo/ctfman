package ch.heigvd.dai.models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import ch.heigvd.dai.db.DB;

public class User extends Authentication {

  private String authentication;
  public String primaryContact;

  // TODO: photo

  public User(String name, String primaryContact) {
    this(name, primaryContact, null, null, null);
  }

  public User(String name, String primaryContact, String passwordHash, Timestamp createdAt, Timestamp deletedAt) {
    super(passwordHash, createdAt, deletedAt);
    this.authentication = name;
    this.primaryContact = primaryContact;
  }

  public static @Nullable User findByName(@NotNull String name) throws SQLException {

    String query = "SELECT * FROM user_account JOIN authentication auth ON auth.identification = user_account.authentication WHERE authentication = ?";
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
            res.getTimestamp("deleted_at"));
      }

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

}
