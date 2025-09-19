package vacislavbaluyev.eduatlas.payload;

import java.time.LocalDateTime;

public record ErrorResposneDTO(LocalDateTime timestamp, String message, String details) {
}
