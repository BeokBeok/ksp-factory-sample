# ksp-factory-sample

## 결과물
<details>

```kotlin
package com.beok.ksp_factory_sample

public enum class AnimalType {
  CAT,
  DOG,
  FISH,
}

public fun AnimalFactory(key: AnimalType): Animal = when (key) {
  AnimalType.CAT -> Cat()
  AnimalType.DOG -> Dog()
  AnimalType.FISH -> Fish()
}
```

</details>

ref. https://medium.com/google-developer-experts/ksp-for-code-generation-dfd2073a6635
