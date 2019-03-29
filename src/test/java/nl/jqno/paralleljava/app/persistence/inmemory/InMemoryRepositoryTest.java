package nl.jqno.paralleljava.app.persistence.inmemory;

import nl.jqno.paralleljava.TestData.AnotherTodo;
import nl.jqno.paralleljava.TestData.SomeTodo;
import nl.jqno.paralleljava.app.logging.NopLogger;
import nl.jqno.picotest.Test;

import java.util.UUID;

import static org.assertj.vavr.api.VavrAssertions.assertThat;

public class InMemoryRepositoryTest extends Test {

    public void repository() {
        var repo = new InMemoryRepository(c -> new NopLogger());

        beforeEach(() -> {
            repo.deleteAll();
        });

        test("initialize does nothing", () -> {
            repo.initialize();
        });

        test("create a todo", () -> {
            var result = repo.create(SomeTodo.TODO);

            assertThat(result).isSuccess();
            assertThat(repo.getAll()).hasValueSatisfying(l -> assertThat(l).contains(SomeTodo.TODO));
        });

        test("get a specific todo", () -> {
            repo.create(SomeTodo.TODO);

            assertThat(repo.get(SomeTodo.ID)).hasValueSatisfying(o -> assertThat(o).contains(SomeTodo.TODO));
            assertThat(repo.get(UUID.randomUUID())).hasValueSatisfying(o -> assertThat(o).isEmpty());
        });

        test("update a specific todo at index 0", () -> {
            var expected = SomeTodo.TODO.withTitle("another title");
            repo.create(SomeTodo.TODO);
            repo.create(AnotherTodo.TODO);

            var result = repo.update(expected);
            var actual = repo.get(SomeTodo.ID).get();

            assertThat(result).isSuccess();
            assertThat(actual).contains(expected);
        });

        test("update a specific todo at index 1", () -> {
            var expected = SomeTodo.TODO.withTitle("another title");
            repo.create(AnotherTodo.TODO);
            repo.create(SomeTodo.TODO);

            var result = repo.update(expected);
            var actual = repo.get(SomeTodo.ID).get();

            assertThat(result).isSuccess();
            assertThat(actual).contains(expected);
        });

        test("delete a specific todo at index 0", () -> {
            repo.create(SomeTodo.TODO);
            repo.create(AnotherTodo.TODO);

            var result = repo.delete(SomeTodo.ID);
            var actual = repo.get(SomeTodo.ID).get();

            assertThat(result).isSuccess();
            assertThat(actual).isEmpty();
        });

        test("delete a specific todo at index 1", () -> {
            repo.create(AnotherTodo.TODO);
            repo.create(SomeTodo.TODO);

            var result = repo.delete(SomeTodo.ID);
            var actual = repo.get(SomeTodo.ID).get();

            assertThat(result).isSuccess();
            assertThat(actual).isEmpty();
        });

        test("delete all todos", () -> {
            repo.create(SomeTodo.TODO);

            var result = repo.deleteAll();

            assertThat(result).isSuccess();
            assertThat(repo.getAll()).hasValueSatisfying(l -> assertThat(l).isEmpty());
        });
    }
}
