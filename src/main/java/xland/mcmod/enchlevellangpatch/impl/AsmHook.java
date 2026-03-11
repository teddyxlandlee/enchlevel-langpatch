package xland.mcmod.enchlevellangpatch.impl;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Nullable;
import xland.mcmod.enchlevellangpatch.api.EnchantmentLevelLangPatch;

import java.lang.invoke.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public final class AsmHook {
    public static @Nullable String langPatchHookWithFallback(
            String key, Map<String, String> translations,
            String fallback
    ) {
        return langPatchHook(key, translations, fallback, true);
    }

    public static @Nullable String langPatchHook(
            String key,
            Map<String, String> translations) {
        return langPatchHook(key, translations, null, false);
    }

    private static @Nullable String langPatchHook(
            String key, Map<String, String> translations,
            String fallback, boolean useFallback
    ) {
//        Mutable<@Nullable String> ms = new MutableObject<>();
        String[] ret = new String[1];   // ret[0] == null

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

        final Map<String, String> unmodifiable = translations;
        LangPatchImpl.forEach((Predicate<String> keyPredicate,
                               EnchantmentLevelLangPatch valueMapping) -> {
            if (keyPredicate.test(key)) {
                String candidate;
                if (useFallback) {
                    candidate = valueMapping.apply(unmodifiable, key, fallback);
                } else {
                    candidate = valueMapping.apply(unmodifiable, key);
                }
                if (candidate == null) return false;    // user skip

//                ms.setValue(candidate);
                ret[0] = candidate;
                return true;    // interrupt
            }
            return false;   // predicate fail, skip
        });
//        return ms.getValue();
        return ret[0];
    }

    public static CallSite guardRefEqual(MethodHandles.Lookup lookup, String errorMessage, MethodType methodType,
                                         MethodHandle... guards) {
        errorMessage = new String(Base64.getUrlDecoder().decode(errorMessage), StandardCharsets.UTF_8);
        //<editor-fold desc="Parameter checks">
        final Class<?> paramType = checkSingleArgument(methodType);
        Preconditions.checkArgument(
                !paramType.isPrimitive(),
                "Primitive argument is unsupported, got: %s", methodType
        );
        Preconditions.checkArgument(
                methodType.returnType() == void.class ||
                        methodType.returnType().isAssignableFrom(paramType),
                "Return type must be void or assignable from the parameter, got: %s", methodType
        );
        for (MethodHandle handle : guards) {
            Preconditions.checkArgument(
                    !handle.type().returnType().isPrimitive(),
                    "Primitive return type is unsupported, got handle: %s", handle
            );
            Preconditions.checkArgument(
                    checkSingleArgument(handle.type()).isAssignableFrom(paramType),
                    "Return type must be assignable from the parameter, got handle: %s", handle
            );
        }
        //</editor-fold>

        final MethodHandle success = MethodHandles.identity(paramType).asType(methodType);
        if (guards.length == 0) {
            return new ConstantCallSite(success);
        }

        final MethodHandle throwException = makeException(errorMessage, paramType);
        final MethodHandle refEqual = doCall(() -> MethodHandles.lookup().findStatic(
                AsmHook.class, "refEqual", MethodType.methodType(boolean.class, Object.class, Object.class)
        ), "Cannot access refEqual");
        final MethodHandle paramToObject = MethodHandles.identity(paramType)
                .asType(MethodType.methodType(Object.class, paramType));    // (T) -> O

        MethodHandle ret = throwException;

        for (int i = guards.length - 1; i >= 0; i--) {
            MethodHandle process = guards[i];

            MethodHandle test = MethodHandles.filterArguments(
                    refEqual, 1,
                    process.asType(MethodType.methodType(Object.class, paramType))
            );    // (Ref[O], UnmappedRef[T]) -> Z
            test = MethodHandles.foldArguments(test, paramToObject);
            ret = MethodHandles.guardWithTest(test, success, ret);
        }

        return new ConstantCallSite(ret.asType(methodType));
    }

    private static Class<?> checkSingleArgument(MethodType methodType) {
        Preconditions.checkArgument(
                methodType.parameterCount() == 1,
                "Expected exact one argument, got: %s", methodType
        );
        return methodType.parameterType(0);
    }

    private static MethodHandle makeException(String errorMessage, Class<?> paramType) {
        // (E) -> T: throw (E) Error
        MethodHandle handle = MethodHandles.throwException(paramType, IncompatibleClassChangeError.class);
        MethodHandle exceptionConstructor = doCall(() -> MethodHandles.publicLookup().findConstructor(
                IncompatibleClassChangeError.class, MethodType.methodType(void.class, String.class)
        ), "Cannot build makeException");   // (String) -> E
        // (String) -> T: throw Error
        handle = MethodHandles.filterArguments(handle, 0, exceptionConstructor);
        // () -> T: throw Error
        handle = handle.bindTo(errorMessage);
        // (T) -> T: throw Error
        handle = MethodHandles.dropArguments(handle, 0, paramType);
        return handle;
    }

    private static <T> T doCall(java.util.concurrent.Callable<T> callable, String errorMessage) {
        try {
            return callable.call();
        } catch (Exception e) {
            throw new IllegalStateException(errorMessage, e);
        }
    }

    private static boolean refEqual(Object o1, Object o2) {
        return o1 == o2;
    }

    private AsmHook() {}
}
