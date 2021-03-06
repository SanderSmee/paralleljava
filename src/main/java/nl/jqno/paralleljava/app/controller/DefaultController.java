package nl.jqno.paralleljava.app.controller;

import io.vavr.Function1;
import io.vavr.control.Try;
import nl.jqno.paralleljava.app.domain.Todo;
import nl.jqno.paralleljava.app.logging.Logger;
import nl.jqno.paralleljava.app.logging.LoggerFactory;
import nl.jqno.paralleljava.app.persistence.IdGenerator;
import nl.jqno.paralleljava.app.persistence.Repository;
import nl.jqno.paralleljava.app.serialization.Serializer;

import java.util.UUID;

public class DefaultController implements Controller {
    private final String url;
    private final Repository repository;
    private final IdGenerator idGenerator;
    private final Serializer serializer;
    private final Logger logger;

    public DefaultController(String url, Repository repository, IdGenerator idGenerator, Serializer serializer, LoggerFactory loggerFactory) {
        this.url = url;
        this.repository = repository;
        this.idGenerator = idGenerator;
        this.serializer = serializer;
        this.logger = loggerFactory.create(getClass());
    }

    public Try<String> get() {
        return repository.getAll()
                .map(serializer::serializeTodos);
    }

    public Try<String> get(String id) {
        var uuid = serializer.deserializeUuid(id);
        if (uuid.isEmpty()) {
            return Try.failure(new IllegalArgumentException("Invalid GET request: " + id));
        }

        return repository
                .get(uuid.get())
                .flatMap(o -> o.map(serializer::serializeTodo).toTry(() -> new IllegalArgumentException("Cannot find " + id)));
    }

    public Try<String> post(String json) {
        logger.forProduction("POSTed: " + json);
        var partialTodo = serializer.deserializePartialTodo(json);
        if (partialTodo.isEmpty() || partialTodo.get().title().isEmpty()) {
            return Try.failure(new IllegalArgumentException("Invalid POST request: " + json));
        }

        var pt = partialTodo.get();
        var id = idGenerator.generateId();
        var todo = new Todo(id, pt.title().get(), buildUrlFor(id), false, pt.order().getOrElse(0));
        return repository.create(todo)
                .map(ignored -> serializer.serializeTodo(todo));
    }

    public Try<String> patch(String id, String json) {
        logger.forProduction("PATCHed: " + json);
        var uuid = serializer.deserializeUuid(id);
        var partialTodo = serializer.deserializePartialTodo(json);
        if (uuid.isEmpty() || partialTodo.isEmpty()) {
            return Try.failure(new IllegalArgumentException("Invalid PATCH request: " + id + ", " + json));
        }

        var pt = partialTodo.get();
        Function1<Todo, Todo> updater = todo -> new Todo(
                todo.id(),
                pt.title().getOrElse(todo.title()),
                todo.url(),
                pt.completed().getOrElse(todo.completed()),
                pt.order().getOrElse(todo.order())
        );
        return repository.update(uuid.get(), updater)
                .map(serializer::serializeTodo);
    }

    public Try<String> delete() {
        return repository.deleteAll()
                .map(ignored -> "");
    }

    public Try<String> delete(String id) {
        var uuid = serializer.deserializeUuid(id);
        if (uuid.isEmpty()) {
            return Try.failure(new IllegalArgumentException("Invalid DELETE request: " + id));
        }

        return repository.delete(uuid.get())
                .map(ignored -> "");
    }

    private String buildUrlFor(UUID id) {
        return url + "/" + id.toString();
    }
}
