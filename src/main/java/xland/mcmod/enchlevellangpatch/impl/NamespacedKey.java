package xland.mcmod.enchlevellangpatch.impl;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.ApiStatus;

import java.util.regex.Pattern;

@ApiStatus.Internal
public class NamespacedKey implements Comparable<NamespacedKey> {
    private static final Pattern NS_PATTERN, PATH_PATTERN;
    private final String namespace, path;
    private String toString;

    public NamespacedKey(String namespace, String path) {
        Preconditions.checkArgument(NS_PATTERN.matcher(namespace).matches(), "Illegal namespace: " + namespace);
        Preconditions.checkArgument(PATH_PATTERN.matcher(path).matches(), "Illegal path" + path);
        this.namespace = namespace;
        this.path = path;
    }

    public static NamespacedKey of(String s) {
        final int i = s.indexOf(':');
        if (i < 0) return new NamespacedKey("minecraft", s);
        return new NamespacedKey(s.substring(0, i), s.substring(i+1));
    }

    public String getNamespace() {
        return namespace;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        if (toString == null) {
            toString = namespace + ':' + path;
        }
        return toString;
    }

    public int compareTo(NamespacedKey key) {
        int i = this.path.compareTo(key.path);
        if (i == 0) {
            i = this.namespace.compareTo(key.namespace);
        }

        return i;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof NamespacedKey)) {
            return false;
        } else {
            NamespacedKey key = (NamespacedKey) o;
            return this.namespace.equals(key.namespace) && this.path.equals(key.path);
        }
    }

    public int hashCode() {
        return 31 * this.namespace.hashCode() + this.path.hashCode();
    }

    static {
        NS_PATTERN = Pattern.compile("^[a-z0-9\\u002e\\u002d_]+$");
        PATH_PATTERN = Pattern.compile("^[a-z0-9\\u002e\\u002d_/]+$");
    }
}
