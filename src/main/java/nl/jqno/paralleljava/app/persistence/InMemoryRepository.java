package nl.jqno.paralleljava.app.persistence;

import io.vavr.collection.List;
import io.vavr.control.Option;
import nl.jqno.paralleljava.app.domain.Todo;
import nl.jqno.paralleljava.app.logging.Logger;
import nl.jqno.paralleljava.app.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.UUID;

/**
 * NOTE: this class is totally not thread-safe!
 */
public class InMemoryRepository implements Repository {
    private static final java.util.List<Todo> todos = new ArrayList<>();

    private final Logger logger;

    public InMemoryRepository(LoggerFactory loggerFactory) {
        this.logger = loggerFactory.create(getClass());
    }

    public void createTodo(Todo todo) {
        logger.forProduction("Creating Todo " + todo);
        todos.add(todo);
    }

    public Option<Todo> get(UUID id) {
        return List.ofAll(todos)
                .find(t -> t.id().equals(id));
    }

    public List<Todo> getAllTodos() {
        return List.ofAll(todos);
    }

    public void updateTodo(Todo todo) {
        var index = List.ofAll(todos)
                .map(Todo::id)
                .indexOf(todo.id());
        todos.remove(index);
        todos.add(index, todo);
    }

    public void delete(UUID id) {
        var index = List.ofAll(todos)
                .map(Todo::id)
                .indexOf(id);
        todos.remove(index);
    }

    public void clearAllTodos() {
        logger.forProduction("Clearing all Todos");
        todos.clear();
    }
}
