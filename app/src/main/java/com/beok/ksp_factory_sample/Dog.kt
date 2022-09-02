package com.beok.ksp_factory_sample

import com.beok.annotations.AutoElement

@AutoElement
class Dog : Animal {
    override fun sound(): String = "Dog sound"
}
