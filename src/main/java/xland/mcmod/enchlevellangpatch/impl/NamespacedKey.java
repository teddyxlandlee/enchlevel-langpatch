package xland.mcmod.enchlevellangpatch.impl;

import com.google.common.base.Preconditions;
import org.apiguardian.api.API;

import java.util.regex.Pattern;

@API(status = API.Status.INTERNAL)
public final class NamespacedKey implements Comparable<NamespacedKey>, java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private static final Pattern NS_PATTERN, PATH_PATTERN;
    private final String namespace, path;
    private transient String toStringCache;

    public NamespacedKey(String namespace, String path) {
        Preconditions.checkArgument(NS_PATTERN.matcher(namespace).matches(), "Illegal namespace: " + namespace);
        Preconditions.checkArgument(PATH_PATTERN.matcher(path).matches(), "Illegal path: " + path);
        this.namespace = namespace;
        this.path = path;
    }

    // Trusted implementation
    private NamespacedKey(String namespace, String path, String asString) {
        this(namespace, path);
        this.toStringCache = asString;
    }

    public static NamespacedKey of(String s) {
        final int i = s.indexOf(':');
        if (i < 0) return new NamespacedKey("minecraft", s);
        return new NamespacedKey(s.substring(0, i), s.substring(i+1), s);
    }

    @SuppressWarnings("unused")
    public String getNamespace() {
        return namespace;
    }

    @SuppressWarnings("unused")
    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        if (toStringCache == null) {
            toStringCache = namespace + ':' + path;
        }
        return toStringCache;
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
