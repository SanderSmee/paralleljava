package nl.jqno.paralleljava.app.persistence.database;

import io.vavr.control.Try;
import org.jdbi.v3.core.HandleCallback;
import org.jdbi.v3.core.HandleConsumer;

public class FailingEngine implements Engine {
    private final Throwable exception;

    public FailingEngine() {
        this(new IllegalStateException());
    }

    public FailingEngine(Throwable exception) {
        this.exception = exception;
    }

    public <X extends Exception> Try<Void> execute(HandleConsumer<X> consumer) {
        return Try.failure(exception);
    }

    public <T, X extends Exception> Try<T> query(HandleCallback<T, X> callback) {
        return Try.failure(exception);
    }
}
