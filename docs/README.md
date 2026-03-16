Without the patch:

![Sharpness enchantment.level.100](https://i.loli.net/2021/10/04/2m8XUWgJZkA6BIs.png)

With the patch:

![Sharpness 100](https://i.loli.net/2021/10/04/lg6X5otEKYHqWVi.png)

## Common alternative formats
* Want pure arabic numerals (e.g. Sharpness 5)? [Download Stub Mod](https://cdn.modrinth.com/data/Lf4kDKU9/versions/1EKfo2Mc/lpstub-all_arabic-20251209.2.jar)
* Want roman numerals (e.g. Sharpness XXV)? [Download Stub Mod](https://cdn.modrinth.com/data/Lf4kDKU9/versions/1EKfo2Mc/lpstub-roman-20251209.2.jar)

These stub mods should be loaded *with* LangPatch itself. 

## I18n API

This mod supports a limited configuration of enchantment level and potion effect level formatting through language files.

### Template Keys
*   **`enchantment.level.x`** & **`potion.potency.x`**:
    Modify the templates for enchantment levels and potion effect levels, respectively.
    *   **Default value:** `"%s"`

### Format Types
*   **`langpatch.conf.enchantment.default.type`** & **`langpatch.conf.potion.default.type`**:
    Modify the format of enchantment levels and potion effect levels, respectively.

    | Value | Format | Example |
    | :--- | :--- | :--- |
    | `simplified`, `chinese`, `zh_normal` | Lowercase Chinese numerals | 一百二十三 |
    | `traditional`, `zh_upper` | Uppercase Chinese numerals (traditional) | 壹佰貳拾叄 |
    | `numeral`, `number`, `numeric`, `arabic`, `default` | Arabic numerals (default) | 123 |
    | `roman` | Roman numerals | CXXIII |
    | `skip`, `ignore` | No change (e.g., for only replacing other text) | — |

### Override
*   **`langpatch.conf.enchantment.override`** & **`langpatch.conf.potion.override`**:
    Whether to override existing translations. Acceptable values are `"true"` or `"false"`.
    *   If set to `"true"`, existing translations will be replaced. See the implementation of the "pure arabic numeral" pack above.

## For Developers

LangPatch, as is named, allows developers to make dynamic changes to in-game translations without directly modifying lang files.

To introduce LangPatch API as a dependency:
```groovy
repositories {
    // Our new maven
    maven { url 'https://mvn.7c7.icu' }
}

dependencies {
    compileOnly "xland.mcmod:enchlevel-langpatch:3.1.0"
}
```

Online javadoc can be found [here](https://teddyxlandlee.github.io/enchlevel-langpatch/javadoc).


