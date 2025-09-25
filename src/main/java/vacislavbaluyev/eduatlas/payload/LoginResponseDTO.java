package vacislavbaluyev.eduatlas.payload;

public record LoginResponseDTO(String token,
                               String username,
                               String email) {
}
