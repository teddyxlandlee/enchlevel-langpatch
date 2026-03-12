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
import java.util.stream.IntStream;

@AsmHook.AsmEntrypoint
public final class AsmHook {
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.SOURCE)
    @interface AsmEntrypoint {}

    @AsmEntrypoint
    public static @Nullable String langPatchHookWithFallback(
            String key, @Unmodifiable Map<String, String> translations, String fallback
    ) {
        return langPatchHook(key, translations, fallback, true);
    }

    @AsmEntrypoint
    public static @Nullable String langPatchHook(String key, @Unmodifiable Map<String, String> translations) {
        return langPatchHook(key, translations, null, false);
    }

    private static @Nullable String langPatchHook(
            String key, @Unmodifiable Map<String, String> translations,
            String fallback, boolean useFallback
    ) {
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

        return LangPatchImpl.loop((Predicate<String> keyPredicate,
                            EnchantmentLevelLangPatch valueMapping) -> {
            if (!keyPredicate.test(key)) return null;   // predicate fail, skip

            // returns nonnull -> interrupt; returns null -> skip
            return useFallback ? valueMapping.apply(translations, key, fallback) : valueMapping.apply(translations, key);
        });
    }

    @AsmEntrypoint
    public static CallSite bootstrapLangPatchHook(MethodHandles.Lookup lookup, String methodName,
                                                  MethodType methodType) throws ReflectiveOperationException {
        //<editor-fold desc="Check Arguments">
        Preconditions.checkArgument(
                methodType.parameterCount() != 0,
                "LangPatchHook must receive more than zero arguments, got %s", methodType
        );
        Preconditions.checkArgument(
                !methodType.parameterType(0).isPrimitive(),
                "First argument must not be primitive, got %s", methodType
        );
        MethodType targetMethodType = methodType.dropParameterTypes(0, 1);
        switch (methodName) {
            case "langPatchHook":
                Preconditions.checkArgument(
                        MethodType.methodType(String.class, String.class, Map.class).equals(targetMethodType),
                        "Got wrong method type for langPatchHook: %s", targetMethodType
                );
                break;
            case "langPatchHookWithFallback":
                Preconditions.checkArgument(
                        MethodType.methodType(String.class, String.class, Map.class, String.class).equals(targetMethodType),
                        "Got wrong method type for langPatchHookWithFallback: %s", targetMethodType
                );
                break;
            default:
                throw new IllegalArgumentException("Illegal method name: " + methodName);
        }
        //</editor-fold>
        // Wanted: (Ref, Key, M, ...) -> S

        // (Key, M+, ...) -> S
        MethodHandle targetMethod = lookup.findStatic(AsmHook.class, methodName, targetMethodType);
        // (Ref, M) -> M+
        MethodHandle getOrDefaultStorageMap = GuardRefImpl.getOrDefaultStorageMap.asType(
                MethodType.methodType(Map.class, Object.class, Map.class)
        );
        // (Key, Ref, M, ...) -> S
        MethodHandle handle = MethodHandles.collectArguments(targetMethod, 1, getOrDefaultStorageMap);
        MethodType mt;
        final int[] order = IntStream.concat(
                IntStream.of(1, 0),     // reordered: (Ref, Key, M, ...) -> S
                IntStream.range(2, (mt = handle.type()).parameterCount())
        ).toArray();
        mt = mt.insertParameterTypes(0, mt.parameterType(1));   // mt := (Ref, Key, Ref, M, ...) -> S
        mt = mt.dropParameterTypes(2, 3);                                  // mt := (Ref, Key, M, ...) -> S
        handle = MethodHandles.permuteArguments(handle, mt, order);
        return new ConstantCallSite(handle);
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

        static final MethodHandle refEqual = doCall(() -> MethodHandles.lookup().findStatic(
                GuardRefImpl.class, "refEqual", MethodType.methodType(boolean.class, Object.class, Object.class)
        ), "Cannot access refEqual");

        static final MethodHandle exceptionConstructor = doCall(() -> MethodHandles.publicLookup().findConstructor(
                IncompatibleClassChangeError.class, MethodType.methodType(void.class, String.class)
        ), "Cannot build makeException");   // (String) -> E

        static final MethodHandle checkInstance = doCall(() -> MethodHandles.publicLookup().findVirtual(
                Class.class, "isInstance", MethodType.methodType(boolean.class, Object.class)
        ), "Cannot find Class::isInstance");

        static final MethodHandle logError = doCall(() -> {
            final Logger logger = LogManager.getLogger(AsmHook.class);
            MethodHandle handle = MethodHandles.publicLookup().findVirtual(
                    Logger.class, "error", MethodType.methodType(void.class, Object.class)
            );
            return handle.bindTo(logger);
        }, "Cannot build AsmHook.LOGGER::error");

        static final WeakHashMap<Object, Object> storageToCacheMap = new WeakHashMap<>();
        static final MethodHandle putStorageMap = doCall(() -> MethodHandles.publicLookup().findVirtual(
                Map.class, "put", MethodType.methodType(Object.class, Object.class, Object.class)
        ).bindTo(storageToCacheMap), "Cannot build storageToCacheMap::put");
        static final MethodHandle getOrDefaultStorageMap = doCall(() -> MethodHandles.publicLookup().findVirtual(
                Map.class, "getOrDefault", MethodType.methodType(Object.class, Object.class, Object.class)
        ).bindTo(storageToCacheMap), "Cannot build storageToCacheMap::getOrDefault");
    }

    @AsmEntrypoint
    public static CallSite guardRefEqual(MethodHandles.Lookup lookup, String errorMessage, MethodType methodType,
                                         MethodHandle fallbackCache,
                                         Object... checks) {
        errorMessage = new String(Base64.getUrlDecoder().decode(errorMessage), StandardCharsets.UTF_8);
        //<editor-fold desc="Parameter checks and casts">
        Preconditions.checkArgument(
                methodType.parameterCount() == 2,
                "Expected exact two argument, got: %s", methodType
        );
        final Class<?> paramType = methodType.parameterType(1);
        Preconditions.checkArgument(
                !paramType.isPrimitive(),
                "Primitive argument is unsupported, got: %s", methodType
        );
        Preconditions.checkArgument(
                methodType.returnType() == void.class ||
                        methodType.returnType().isAssignableFrom(paramType),
                "Return type must be void or assignable from the parameter, got: %s", methodType
        );
        Preconditions.checkArgument(
                fallbackCache.type().parameterCount() == 1 &&
                        fallbackCache.type().parameterType(0).isAssignableFrom(paramType),
                "fallbackCache must be able to receive exact" +
                        " one parameter (i.e. paramType), got handle: %s", fallbackCache
        );
        Preconditions.checkArgument(
                fallbackCache.type().returnType() != void.class,
                "fallbackCache must not return void, got handle: %s", fallbackCache
        );
        fallbackCache = fallbackCache.asType(MethodType.methodType(Object.class, paramType));
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
                        handle = MethodHandles.dropArguments(handle, 0, paramType);
                        break;
                    case 1:
                        Preconditions.checkArgument(
                                handle.type().parameterType(0).isAssignableFrom(paramType),
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

        final MethodHandle success = MethodHandles.dropArguments(
                MethodHandles.identity(paramType), 0, Object.class
        ).asType(methodType);
        if (guards.isEmpty()) {
            return new ConstantCallSite(MethodHandles.dropArguments(success, 0, methodType.parameterType(0)));
        }

        final MethodHandle throwException = makeException(errorMessage, paramType, fallbackCache);
        final MethodHandle paramToObject = MethodHandles.identity(paramType)
                .asType(MethodType.methodType(Object.class, paramType));    // (T) -> O

        MethodHandle ret = throwException;

        for (Iterator<MethodHandle> itr = guards.descendingIterator(); itr.hasNext();) {
            MethodHandle process = itr.next();
            MethodHandle test;
            if (process.type().returnType() == boolean.class) {
                test = process.asType(MethodType.methodType(boolean.class, paramType));
            } else {
                test = MethodHandles.filterArguments(
                        GuardRefImpl.refEqual, 1,
                        process.asType(MethodType.methodType(Object.class, paramType))
                );    // (Ref[O], UnmappedRef[T]) -> Z
                test = MethodHandles.foldArguments(test, paramToObject);    // (T) -> Z
            }
            ret = MethodHandles.guardWithTest(test, success, ret);
        }

        return new ConstantCallSite(ret.asType(methodType));
    }

    private static MethodHandle makeException(String errorMessage, Class<?> paramType, MethodHandle fallbackCache) {
        // (E) -> V: log Error
        MethodHandle handle = GuardRefImpl.logError.asType(MethodType.methodType(void.class, IncompatibleClassChangeError.class));
        // (String) -> V: log Error
        handle = MethodHandles.filterArguments(handle, 0, GuardRefImpl.exceptionConstructor);
        // () -> V: log Error
        handle = handle.bindTo(errorMessage);
        // (T) -> T: log Error
        handle = MethodHandles.foldArguments(MethodHandles.identity(paramType), handle);
        // (S, T) -> T: log Error
        handle = MethodHandles.dropArguments(handle, 0, Object.class);

        // (Obj, T) -> Obj: put cached(T)
        MethodHandle putStorageMap = MethodHandles.filterArguments(GuardRefImpl.putStorageMap, 1, fallbackCache);

        // (S, T) -> T: put {S=cached(T)} to cache -> log Error
        handle = MethodHandles.foldArguments(
                handle,
                putStorageMap.asType(handle.type().changeReturnType(void.class))
        );
        return handle;
    }

    @AsmEntrypoint
    public static boolean isCollectionsUnmodifiable(Map<?, ?> map) {
        Map<?, ?> unmodifiable = Collections.unmodifiableMap(map);
        // unmodifiable.getClass() is exactly UnmodifiableMap and will not be its subclasses
        return (unmodifiable == map) || unmodifiable.getClass().isInstance(map);
    }

    private AsmHook() {}
}
