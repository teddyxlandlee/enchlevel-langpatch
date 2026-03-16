package xland.mcmod.enchlevellangpatch.mixin.legacy;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import xland.mcmod.enchlevellangpatch.mixin.AsmTranslationStorage;

import java.util.NoSuchElementException;

@SuppressWarnings("unused")
public class LegacyTransformer implements IClassTransformer {
    private static final String TARGET_METHOD_DESC = "(Ljava/lang/String;)Ljava/lang/String;";

    @Override
    public byte[] transform(String basicClassName, String transformedName, byte[] basicClass) {
        if (basicClass == null) return null;
        final AsmTranslationStorage asm;
        final String targetMethod;
        switch (transformedName) {
            case "net.minecraft.client.resources.Locale":
                asm = new AsmTranslationStorage(
                        transformedName, "field_135032_a",
                        /*fallback=*/false, /*unmodifiableWrap*/true, /*guardPutField=*/false
                );
                targetMethod = "func_135026_c";
                break;
            case "net.minecraft.util.text.translation.LanguageMap":
                asm = new AsmTranslationStorage(
                        transformedName, "field_74816_c",
                        /*fallback=*/false, /*unmodifiableWrap=*/true, /*guardPutField=*/false
                );
                targetMethod = "func_135064_c";
                break;
            default:
                return basicClass;
        }

        ClassReader reader = new ClassReader(basicClass);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

//        System.out.println("Available methods: " + com.google.common.collect.Lists.transform(node.methods, m -> m.name + m.desc));
        MethodNode method = node.methods.stream()
                .filter(m -> TARGET_METHOD_DESC.equals(m.desc) &&
                        targetMethod.equals(FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(basicClassName, m.name, TARGET_METHOD_DESC))
                )
                .findAny()
                .orElseThrow(() -> new NoSuchElementException(String.format(
                        "Method not found: %s.%s:%s", transformedName, targetMethod, TARGET_METHOD_DESC
                )));
        asm.accept(method);
        // 1.12.2 has no `fallback` parameter, so the "injected header" won't be 3
        method.maxStack = Math.max(2, method.maxStack);
        //maxLocals: no change, since all allocations are on the stack

        ClassWriter cw = new ClassWriter(0);
        node.accept(cw);
        return cw.toByteArray();
    }
}
