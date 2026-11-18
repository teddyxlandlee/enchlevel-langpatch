package xland.mcmod.enchlevellangpatch.mixin.ext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Modifier;
import java.util.Objects;

public class BypassNonReplaceablePrefixes implements AsmExtension<ClassNode> {
    private static final String FIELD_NAME = "NON_REPLACEABLE_PREFIXES";
    private static final String DISABLE_PROPERTY = "langpatch.asm.dontBypassNonReplaceablePrefixes";
    private static final Logger LOGGER = LogManager.getLogger(BypassNonReplaceablePrefixes.class);

    @Override
    public void accept(ClassNode classNode) {
        if (Boolean.getBoolean(DISABLE_PROPERTY)) return;

        if (classNode.fields.stream().noneMatch(f -> FIELD_NAME.equals(f.name) && "Ljava/util/Set;".equals(f.desc) && Modifier.isStatic(f.access))) {
            // Do not probe
            return;
        }
        final String ownerName = classNode.name;
        LOGGER.debug("Found {} in class {}", FIELD_NAME, ownerName);

        classNode.methods.stream().filter(m -> "<clinit>".equals(m.name) && "()V".equals(m.desc))
                .findAny()
                .ifPresent(m -> {
                    for (AbstractInsnNode node = m.instructions.getFirst(); node != null; node = node.getNext()) {
                        if (node.getOpcode() != Opcodes.PUTSTATIC) continue;
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) node;
                        if (Objects.equals(ownerName, fieldInsnNode.owner) && FIELD_NAME.equals(fieldInsnNode.name) && "Ljava/util/Set;".equals(fieldInsnNode.desc)) {
                            InsnList list = new InsnList();
                            {
                                list.add(new InsnNode(Opcodes.POP));
                                list.add(new FieldInsnNode(Opcodes.GETSTATIC, "java/util/Collections", "EMPTY_SET", "Ljava/util/Set;"));
                            }
                            m.instructions.insertBefore(fieldInsnNode, list);
                            LOGGER.info("Applied {} to class {}", this, ownerName);
                        }
                    }
                });
    }
}
