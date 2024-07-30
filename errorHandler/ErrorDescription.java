package errorHandler;

public class ErrorDescription implements Comparable<ErrorDescription> {
    private final int line;
    private final ErrorType errorType;

    public ErrorDescription(int line, ErrorType errorType) {
        this.line = line;
        this.errorType = errorType;
    }

    @Override
    public String toString() {
        return String.format("%d %s", line, errorType.toString());
    }

    @Override
    public int compareTo(ErrorDescription o) {
        return this.line - o.line;
    }
}
