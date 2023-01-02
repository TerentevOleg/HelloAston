package exception;

public class DbCreateEntityFaultException extends RuntimeException {
    public DbCreateEntityFaultException(String message) {
        super(message);
    }
}
