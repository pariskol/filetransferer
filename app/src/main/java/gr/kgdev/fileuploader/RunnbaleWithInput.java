package gr.kgdev.fileuploader;

@FunctionalInterface
public interface RunnbaleWithInput<T> {

    public void run(T input);
}
