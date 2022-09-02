package com.beok.ksp_factory_sample

import com.beok.annotations.AutoFactory

@AutoFactory
interface Animal {
    fun sound(): String
}
