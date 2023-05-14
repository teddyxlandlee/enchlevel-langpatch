/**
 * <p>The API of Enchantment Level Language Patch.</p>
 * <p></p>
 * <h2>What is it for?</h2>
 * <p>It is an API to patch the language file of Minecraft,
 * resource packs and mods, without modifying it.</p><p>This means,
 * you can make changes to these translations in bulk.</p>
 * <p>In particular, you can register your own style for enchantment
 * levels as well as potion potency.</p>
 * <p></p>
 * <h2>How can I register a patch?</h2>
 * <p>Here's an example:</p><blockquote><pre>
 *     final int prefixLen = "item.minecraft.".length();
 *     {@link xland.mcmod.enchlevellangpatch.api.EnchantmentLevelLangPatch#registerPatch EnchantmnetLevelLangPatch.registerPatch}(
 *          s -> s.startsWith("item.minecraft."),
 *          (storage, key) -> {
 *              String s = storage.get(key);
 *              if (s != null) return s;
 *              if (s.length() == prefixLen) return "???";
 *              return String.join(" ", Arrays.stream(s.substring(prefixLen).split("_"))
 *                  .flatMap(word -> {
 *                      String w = word.strip();
 *                      if (w.isEmpty()) return Stream.empty();
 *                      return Stream.of(Character.toUppercase(w.charAt(0) + w.substring(1)));
 *                  })
 *                  .collect(Collectors.toList()));
 *          }
 *     );
 * </pre></blockquote>
 * <p>When it comes to enchantment level or potion potency, things are
 * quite similar:</p><blockquote><pre>
 *     final Locale localeThai = new Locale("th", "TH", "TH");
 *     EnchantmentLevelLangPatch patch = (storage, key) -> {
 *          int lvl = Integer.parseInt(key.substring(18));
 *          return String.format(localeThai, "%d", lvl);
 *     };
 *     {@link xland.mcmod.enchlevellangpatch.api.EnchantmentLevelLangPatch#registerEnchantmentPatch EnchantmnetLevelLangPatch.registerEnchantmentPatch}("my_mod_id:thai_numbers", patch);
 *     // If you want to apply it immediately:
 *     {@link xland.mcmod.enchlevellangpatch.api.EnchantmentLevelLangPatchConfig#setCurrentEnchantmentHooks EnchantmnetLevelLangPatchConfig.setCurrentEnchantmentHooks}(patch);
 * </pre></blockquote>
 * <h3>Mod entrypoint</h3>
 * <p>If your mod runs on Fabric/Quilt: your patches should be registered
 * in entrypoint {@code "enchlevel-langpatch.init"}, whose type is {@code ClientModInitializer}.</p>
 * <p>If your mod runs on Forge: register your patches in your <b>mod constructor</b>.</p>
 */
package xland.mcmod.enchlevellangpatch.api;
