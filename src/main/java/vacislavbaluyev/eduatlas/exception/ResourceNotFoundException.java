package vacislavbaluyev.eduatlas.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

  public ResourceNotFoundException(String risorsa, String campo, Object valore) {
    super(String.format("%s non trovato con %s: %s", risorsa, campo, valore));
  }
}