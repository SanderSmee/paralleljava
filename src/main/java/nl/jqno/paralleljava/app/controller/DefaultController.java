package nl.jqno.paralleljava.app.controller;

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
    private final IdGenerator generator;
    private final Serializer serializer;
    private final Logger logger;

    public DefaultController(String url, Repository repository, IdGenerator generator, Serializer serializer, LoggerFactory loggerFactory) {
        this.url = url;
        this.repository = repository;
        this.generator = generator;
        this.serializer = serializer;
        this.logger = loggerFactory.create(getClass());
    }

    public Try<String> get() {
        return Try.of(() -> serializer.serializeTodos(repository.getAllTodos()));
    }

    public String get(String id) {
        return serializer.deserializeUuid(id)
                .flatMap(repository::get)
                .map(serializer::serializeTodo)
                .getOrElse("");
    }

    public String post(String json) {
        logger.forProduction("POSTed: " + json);
        var partialTodo = serializer.deserializePartialTodo(json);
        if (partialTodo.isDefined() && partialTodo.get().title().isDefined()) {
            var pt = partialTodo.get();
            var id = generator.generateId();
            var todo = new Todo(id, pt.title().get(), buildUrlFor(id), false, pt.order().getOrElse(0));
            repository.createTodo(todo);
            logger.forProduction("Returning from POST: " + json);
            return serializer.serializeTodo(todo);
        }
        return "";
    }

    public String patch(String id, String json) {
        logger.forProduction("PATCHed: " + json);
        var uuid = serializer.deserializeUuid(id);
        var partialTodo = serializer.deserializePartialTodo(json);
        if (uuid.isDefined() && partialTodo.isDefined()) {
            var pt = partialTodo.get();
            var existingTodo = repository.get(uuid.get());
            if (existingTodo.isDefined()) {
                var todo = existingTodo.get();
                var updatedTodo = new Todo(
                        todo.id(),
                        pt.title().getOrElse(todo.title()),
                        todo.url(),
                        pt.completed().getOrElse(todo.completed()),
                        pt.order().getOrElse(todo.order()));
                repository.updateTodo(updatedTodo);
                return serializer.serializeTodo(updatedTodo);
            }
        }
        return "";
    }

    public String delete() {
        repository.clearAllTodos();
        return "";
    }

    public String delete(String id) {
        var uuid = serializer.deserializeUuid(id);
        if (uuid.isDefined()) {
            repository.delete(uuid.get());
        }
        return "";
    }

    private String buildUrlFor(UUID id) {
        return url + "/" + id.toString();
    }
}
