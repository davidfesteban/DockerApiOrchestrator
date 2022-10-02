package de.naivetardis.landscaper.utility;

public class Default<T> {

    private T of;

    private Default(T of) {
        this.of = of;
    }

    public static <T> Default<T> of(T of) {
        return new Default<T>(of);
    }

    public T orElse(T orElse) {
        if(of == null) {
            return orElse;
        }

        return get();
    }

    public T get() {
        return of;
    }
}
