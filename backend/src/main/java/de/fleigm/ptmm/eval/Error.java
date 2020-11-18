package de.fleigm.ptmm.eval;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.json.bind.JsonbBuilder;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Error {
  private String code;
  private String message;
  private String throwable;
  private Map<String, Object> details = new HashMap<>();

  public Error(String code, String message, Throwable throwable) {
    this.code = code;
    this.message = message;
    this.throwable = JsonbBuilder.create().toJson(throwable);
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


}
