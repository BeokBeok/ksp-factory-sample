package com.beok.ksp_factory_sample

import com.beok.annotations.AutoElement

@AutoElement
class Cat : Animal {
    override fun sound(): String = "Cat sound"
}
