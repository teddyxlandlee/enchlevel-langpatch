/*
 * ModLauncher - for launching Java programs with in-flight transformation ability.
 *
 *     Copyright (C) 2017-2019 cpw
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package cpw.mods.modlauncher.api;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * System environment. Global properties relevant to the current environment and lookups to find global artifacts
 * in the environment.
 */
public interface IEnvironment {
    <T> Optional<T> getProperty(TypesafeMap.Key<T> key);

    static <T> Supplier<TypesafeMap.Key<T>> buildKey(String name, Class<T> clazz) {
        throw new AssertionError("Trying to invoke a stub method");
    }

    final class Keys {
        public static final Supplier<TypesafeMap.Key<String>> VERSION = buildKey("version", String.class);
    }
}
