package de.fleigm.ptmm.feeds;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Error {
  private String code;
  private String message;
  private Exception exception;
  private Map<String, Object> details = new HashMap<>();

  public Error(String code, String message, Throwable throwable) {
    this.code = code;
    this.message = message;
    this.exception = throwable == null ? null : Exception.of(throwable);
  }

  public static Error of(String code, String message) {
    return new Error(code, message, null);
  }

  public static Error of(String code, String message, Throwable throwable) {
    return new Error(code, message, throwable);
  }

  public Error addDetail(String key, Object value) {
    details.put(key, value);

    return this;
  }


  /**
   * We need an extra class for storing throwables.
   * Manual serialization from Throwable to json is not possible via JSONB
   * because Quarkus cannot resolve JsonbBuilder.create()
   */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Exception {
    private String message;
    private String cause;
    private String stacktrace;

    public static Exception of(Throwable throwable) {
      return new Exception(
          throwable.getMessage(),
          ExceptionUtils.getRootCauseMessage(throwable),
          ExceptionUtils.getStackTrace(throwable)
      );
    }
  }

}
