package vacislavbaluyev.eduatlas.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String sistemaValutazione, String paese, String nome) {
    }

    public ResourceNotFoundException(String sistemaValutazione, String id, Long id1) {
    }
}