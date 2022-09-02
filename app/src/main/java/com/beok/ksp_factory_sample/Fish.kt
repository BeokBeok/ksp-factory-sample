package com.beok.ksp_factory_sample

import com.beok.annotations.AutoElement

@AutoElement
class Fish : Animal {
    override fun sound(): String = "NA"
}
