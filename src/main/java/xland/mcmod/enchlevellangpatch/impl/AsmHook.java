package xland.mcmod.enchlevellangpatch.impl;

import com.google.common.base.Preconditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import xland.mcmod.enchlevellangpatch.api.EnchantmentLevelLangPatch;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;

import static java.lang.invoke.MethodHandles.*;

@AsmHook.AsmEntrypoint
public final class AsmHook {
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.SOURCE)
    @interface AsmEntrypoint {}

    /*
     * Post-1.16 versions report the `storage` field already unmodifiable.
     *   [20w22a+ uses Guava's ImmutableMap; 24w33a+ uses Map.copyOf()]
     * 1.13.2~20w21a uses a mutable hashmap. We can optimize it.
     *
     * Optimization:
     * If version is <1.16-alpha.20.22.a, we wrap Collections.unmodifiableMap() at
     *   invocation point *in bytecode*.
     * Otherwise, we just pass through the argument.
     *
     * Fun fact: `ne.mi.cl.re.la.ClientLanguage` (moj-name, `TranslationStorage` in yarn)
     *   was named as `Locale` (`ne.mi.cl.re.la` in moj-name, `ne.mi.cl.re` in srg-name)
     *   until 20w22a.
     */
    // assert translations == Map.copyOf(translations) ||
    //          translations instanceof ImmutableMap ||
    //          translations == Collections.unmodifiableMap(translations)

    @AsmEntrypoint
    public static @Nullable String langPatchHookWithFallback(
            String key, @Unmodifiable Map<String, String> translations, String fallback
    ) {
        return LangPatchImpl.loop((Predicate<String> keyPredicate, EnchantmentLevelLangPatch valueMapping) -> {
            if (!keyPredicate.test(key)) return null;   // predicate fail, skip

            // returns nonnull -> interrupt; returns null -> skip
            return valueMapping.apply(translations, key, fallback);
        });
    }

    @AsmEntrypoint
    public static @Nullable String langPatchHook(String key, @Unmodifiable Map<String, String> translations) {
        return LangPatchImpl.loop((Predicate<String> keyPredicate, EnchantmentLevelLangPatch valueMapping) -> {
            if (!keyPredicate.test(key)) return null;   // predicate fail, skip

            // returns nonnull -> interrupt; returns null -> skip
            return valueMapping.apply(translations, key);
        });
    }

    private static final class GuardRefImpl {
        private GuardRefImpl() {}

        private static <T> T doCall(java.util.concurrent.Callable<T> callable, String errorMessage) {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new IllegalStateException(errorMessage, e);
            }
        }

        // In Java 8 classfile there is no nest host/members, so refEqual() is inaccessible from outer class
        private static boolean refEqual(Object o1, Object o2) {
            return o1 == o2;
        }

        static final MethodHandle refEqual = doCall(() -> lookup().findStatic(
                GuardRefImpl.class, "refEqual", MethodType.methodType(boolean.class, Object.class, Object.class)
        ), "Cannot access refEqual");

        static final MethodHandle checkInstance = doCall(() -> publicLookup().findVirtual(
                Class.class, "isInstance", MethodType.methodType(boolean.class, Object.class)
        ), "Cannot find Class.isInstance()");

        private static void logError(Logger logger, String errorMessage, Object obj) {
            final String identity = obj.getClass().getName() + '@' + Integer.toHexString(obj.hashCode());
            logger.error(new IncompatibleClassChangeError(errorMessage + ": " + identity));
        }
        static final MethodHandle logError = doCall(() -> lookup().findStatic(
                GuardRefImpl.class, "logError", MethodType.methodType(void.class, Logger.class, String.class, Object.class)
        ).bindTo(LogManager.getLogger(AsmHook.class)), "Cannot build logError");
    }

    @AsmEntrypoint
    public static CallSite makeUnmodifiableView(Lookup ignoreLookup, String errorMessage, MethodType methodType,
                                                MethodHandle fallback,
                                                Object... checks) {
        errorMessage = new String(Base64.getUrlDecoder().decode(errorMessage), StandardCharsets.UTF_8);
        //<editor-fold desc="Parameter checks and casts">
        /* methodType */
        Preconditions.checkArgument(
                methodType.parameterCount() == 1,
                "Expected exact one argument, got: %s", methodType
        );
        final Class<?> paramType = methodType.parameterType(0);
        Preconditions.checkArgument(
                !paramType.isPrimitive(),
                "Primitive argument is unsupported, got: %s", methodType
        );
        Preconditions.checkArgument(
                methodType.returnType() == void.class ||
                        methodType.returnType().isAssignableFrom(paramType),
                "Return type must be void or assignable from the parameter, got: %s", methodType
        );

        /* fallback */
        Preconditions.checkArgument(
                methodType.returnType().isAssignableFrom(fallback.type().returnType()),
                "Fallback return type must be assignable to the method, got handle: %s", fallback
        );
        Preconditions.checkArgument(
                fallback.type().parameterCount() == 1 && fallback.type().parameterType(0).isAssignableFrom(paramType),
                "Fallback parameter must be single and assignable from paramType, got handle: %s", fallback
        );

        /* checks */
        final Deque<MethodHandle> guards = new ArrayDeque<>(checks.length);  // implicit null check
        for (Object arg : checks) {
            Preconditions.checkArgument(
                    arg.getClass() == Class.class || arg instanceof MethodHandle,
                    "A class or method handle is expected, got: %s", arg
            );

            if (arg instanceof MethodHandle) {
                MethodHandle handle = (MethodHandle) arg;
                Class<?> returnType = handle.type().returnType();
                Preconditions.checkArgument(
                        !returnType.isPrimitive() || returnType == boolean.class || returnType == Boolean.class,
                        "Primitive return type is unsupported, got handle: %s", handle
                );
                // Support zero or one argument
                switch (handle.type().parameterCount()) {
                    case 0:
                        handle = dropArguments(handle, 0, paramType);
                        break;
                    case 1:
                        Preconditions.checkArgument(
                                methodType.parameterType(0).isAssignableFrom(paramType),
                                "Return type must be assignable from the parameter, got handle: %s", handle
                        );
                        break;
                    default:
                        throw new IllegalArgumentException("Expected handle to take zero or one arguments, got handle: " + handle);
                }
                if (returnType == Boolean.class) {
                    handle = handle.asType(handle.type().changeReturnType(boolean.class));
                }
                guards.addLast(handle);
            } else {
                Preconditions.checkArgument(
                        !((Class<?>) arg).isPrimitive(), "Primitive type check is unsupported, got: %s", arg
                );
                guards.addLast(GuardRefImpl.checkInstance.bindTo(arg));
            }
        }
        //</editor-fold>

        final MethodHandle success = identity(paramType).asType(methodType);
        if (guards.isEmpty()) {
            return new ConstantCallSite(success);
        }

        final MethodHandle logException = logException(fallback, errorMessage).asType(methodType);
        final MethodHandle paramToObject = identity(paramType)
                .asType(MethodType.methodType(Object.class, paramType));    // (T) -> O

        MethodHandle ret = logException;

        for (Iterator<MethodHandle> itr = guards.descendingIterator(); itr.hasNext();) {
            MethodHandle process = itr.next();
            MethodHandle test;
            if (process.type().returnType() == boolean.class) {
                test = process.asType(MethodType.methodType(boolean.class, paramType));
            } else {
                test = filterArguments(
                        GuardRefImpl.refEqual, 1,
                        process.asType(MethodType.methodType(Object.class, paramType))
                );    // (Ref[O], UnmappedRef[T]) -> Z
                test = foldArguments(test, paramToObject);    // (T) -> Z
            }
            ret = guardWithTest(test, success, ret);
        }

        return new ConstantCallSite(ret.asType(methodType));
    }

    private static MethodHandle logException(MethodHandle fallback, String errorMessage) {
        return foldArguments(
                fallback.asType(fallback.type().changeParameterType(0, Object.class)),
                GuardRefImpl.logError.bindTo(errorMessage)
        );
    }

    @AsmEntrypoint
    public static boolean isCollectionsUnmodifiable(Map<?, ?> map) {
        Map<?, ?> unmodifiable = Collections.unmodifiableMap(map);
        // unmodifiable.getClass() is exactly UnmodifiableMap and will not be its subclasses
        return (unmodifiable == map) || unmodifiable.getClass().isInstance(map);
    }

    private AsmHook() {}
}
