package xland.mcmod.enchlevellangpatch.mixin.ext;

import org.jetbrains.annotations.ApiStatus;

import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

@FunctionalInterface
@ApiStatus.Experimental
public interface AsmExtension<T> extends Consumer<T>, UnaryOperator<T> {
    @Override
    default T apply(T t) {
        this.accept(t);
        return t;
    }

    static <T> void applyWhenMatch(T target, boolean condition, Supplier<? extends Consumer<T>> patch) {
        if (condition) patch.get().accept(target);
    }
}
