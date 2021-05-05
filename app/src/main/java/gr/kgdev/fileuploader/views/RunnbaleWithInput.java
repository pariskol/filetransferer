package gr.kgdev.fileuploader.views;

@FunctionalInterface
public interface RunnbaleWithInput<T> {

    public void run(T input);
}
