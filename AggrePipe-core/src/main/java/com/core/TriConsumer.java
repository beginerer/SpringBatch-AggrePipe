package com.core;

import java.util.Objects;

@FunctionalInterface
public interface TriConsumer <T, U, V>{




    void accept(T t, U u ,V v);



    default TriConsumer<T, U, V> andThen(TriConsumer<? super T, ? super U, ? super V> after) {
        Objects.requireNonNull(after);

        return (l, r, u) -> {
            accept(l,r,u);
            after.accept(l,r,u);
        };

    }

}
