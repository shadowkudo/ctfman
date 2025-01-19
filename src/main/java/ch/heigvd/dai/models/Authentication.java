package ch.heigvd.dai.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.javalin.openapi.OpenApiPropertyType;
import java.sql.Timestamp;
import java.time.Instant;
import org.jetbrains.annotations.Nullable;

public class Authentication {
  @JsonIgnore protected String passwordHash;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
  protected Timestamp createdAt;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
  protected Timestamp deletedAt;

  public Authentication() {}

  public Authentication(
      @Nullable String passwordHash, @Nullable Timestamp createdAt, @Nullable Timestamp deletedAt) {
    this.passwordHash = passwordHash;
    this.createdAt = createdAt;
    this.deletedAt = deletedAt;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  @OpenApiPropertyType(definedBy = Instant.class)
  public Timestamp getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Timestamp createdAt) {
    this.createdAt = createdAt;
  }

  @OpenApiPropertyType(definedBy = Instant.class)
  public Timestamp getDeletedAt() {
    return deletedAt;
  }

  public void setDeletedAt(Timestamp deletedAt) {
    this.deletedAt = deletedAt;
  }
}
