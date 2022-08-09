package xland.mcmod.enchlevellangpatch.impl.f2f;

import cpw.mods.modlauncher.api.*;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;
import xland.mcmodbridge.fa2fomapper.SupportedPlatform;
import xland.mcmodbridge.fa2fomapper.api.Mapping;
import xland.mcmodbridge.fa2fomapper.api.tiny.TinyUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.objectweb.asm.Opcodes.*;

public class ClientLanguageTransformationService implements ITransformationService {
    String type;
    Mapping.NodeElement nodeStorage;
    Mapping.NodeElement nodeGetOrDefault;

    @Override
    public @NotNull String name() {
        return "enchlevel-langpatch-clientlanguage";
    }

    @Override
    public void initialize(IEnvironment env) {
        String s = LangPatchMappingContextProvider.getMappingString();
        Mapping mapping = TinyUtils.read(new BufferedReader(new StringReader(s)), "base",
                SupportedPlatform.current().getId());
        type = mapping.mapClass("net/minecraft/class_1078");
        nodeStorage = mapping.mapField(Mapping.NodeElement.of(
                "net/minecraft/class_1078", /*storage*/"field_5330", "Ljava/util/Map;"));
        nodeGetOrDefault = mapping.mapMethod(Mapping.NodeElement.of(
                "net/minecraft/class_1078", /*getOrDefault*/"method_4679", "(Ljava/lang/String;)Ljava/lang/String;"));
    }

    @Override
    public void beginScanning(IEnvironment env) {}

    @Override
    public void onLoad(IEnvironment env, Set<String> set) {

    }

    @Override
    @SuppressWarnings("rawtypes")
    public @NotNull List<ITransformer> transformers() {
        return Collections.singletonList(new ITransformer<ClassNode>() {
            @Override
            public @NotNull ClassNode transform(ClassNode cn, ITransformerVotingContext iTransformerVotingContext) {
                cn.methods.stream().filter(this::matchGetDefault).findAny().ifPresent(m -> {
                    InsnList list = new InsnList();
                    LabelNode jumpReturn = new LabelNode(), start = new LabelNode();
                    list.add(start);
                    list.add(new VarInsnNode(ALOAD, 1 /*String key*/));
                    list.add(new VarInsnNode(ALOAD, 0 /*this*/));
                    list.add(new FieldInsnNode(GETFIELD, nodeStorage.getOwner().getInternalName(),
                            nodeStorage.getName(), nodeStorage.getDesc().getDescriptor()));
                    list.add(new MethodInsnNode(INVOKESTATIC, "xland/mcmod/enchlevellangpatch/impl/AsmHook",
                            "langPatchHook", "(Ljava/lang/String;Ljava/util/Map;)Ljava/lang/String;"));
                    list.add(new InsnNode(DUP));    // after: stack 2
                    list.add(new JumpInsnNode(IFNULL, jumpReturn));
                    // stack 1
                    list.add(new InsnNode(ARETURN));
                    list.add(jumpReturn);
                    // stack 1
                    list.add(new InsnNode(POP));

                    list.add(new LineNumberNode(114514, start));
                    m.instructions.insert(list);
                });
                if ("true".equals(System.getProperty("fa2fomapper.export"))) {
                    new Thread(() -> {
                        LogManager.getLogger().info("CLTransform Args: type={}, storage={}, getDefault={}", type, nodeStorage, nodeGetOrDefault);
                        ClassWriter writer = new ClassWriter(1);
                        cn.accept(writer);
                        Path p = Paths.get(".fa2fomapper", cn.name + ".class");
                        try {
                            Files.createDirectories(p.getParent());
                            try (OutputStream os = Files.newOutputStream(p)) {
                                os.write(writer.toByteArray());
                            }
                            LogManager.getLogger().warn("CLTransform: Dumped ClientLanguage into {}",
                                    p.toAbsolutePath());
                        } catch (IOException e) {
                            LogManager.getLogger().error("Can't dump " + cn.name, e);
                        }
                    }, "CL-Transform-Dump-8").start();
                }
                return cn;
            }

            @Override
            public @NotNull TransformerVoteResult castVote(ITransformerVotingContext iTransformerVotingContext) {
                return TransformerVoteResult.YES;
            }

            @Override
            public @NotNull Set<Target> targets() {
                return Collections.singleton(Target.targetClass(type));
            }

            private boolean matchGetDefault(MethodNode node) {
                return Objects.equals(node.name, nodeGetOrDefault.getName()) &&
                       Objects.equals(node.desc, nodeGetOrDefault.getDesc().getDescriptor());
            }
        });
    }
}
