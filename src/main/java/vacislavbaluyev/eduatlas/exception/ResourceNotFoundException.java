package vacislavbaluyev.eduatlas.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, String field, String value) {
        super(String.format("%s not found with %s = %s", resource, field, value));
    }

    public ResourceNotFoundException(String resource, String field, Long value) {
        super(String.format("%s not found with %s = %d", resource, field, value));
    }
}