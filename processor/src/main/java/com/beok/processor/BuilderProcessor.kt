package com.beok.processor

import com.beok.annotations.AutoElement
import com.beok.annotations.AutoFactory
import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo

class BuilderProcessor(private val codeGenerator: CodeGenerator) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val factories = getFactories(resolver)
        val data = getElements(resolver, factories)
        data.forEach {
            genFile(it.key, it.value).writeTo(codeGenerator, Dependencies(true))
        }
        return emptyList()
    }

    private fun getFactories(resolver: Resolver): Set<ClassName> {
        return resolver.getSymbolsWithAnnotation(AutoFactory::class.qualifiedName.orEmpty())
            .filterIsInstance<KSClassDeclaration>()
            .filter(KSNode::validate)
            .map(KSClassDeclaration::toClassName)
            .toSet()
    }

    private fun getElements(
        resolver: Resolver,
        factories: Set<ClassName>
    ): Map<ClassName, List<ClassName>> {
        val result = mutableMapOf<ClassName, MutableList<ClassName>>()
        factories.forEach { result[it] = mutableListOf() }
        resolver.getSymbolsWithAnnotation(AutoElement::class.qualifiedName.orEmpty())
            .filterIsInstance<KSClassDeclaration>()
            .filter(KSNode::validate)
            .forEach { ksClassDeclaration ->
                ksClassDeclaration.superTypes
                    .map {
                        it.resolve().declaration.closestClassDeclaration()
                            ?.toClassName()
                    }
                    .filter { result.containsKey(it) }
                    .forEach { className ->
                        result[className]?.add(ksClassDeclaration.toClassName())
                    }
            }
        return result.toMap()
    }

    private fun genFile(key: ClassName, value: List<ClassName>): FileSpec {
        val funcName = "${key.simpleName}Factory"
        val enumName = "${key.simpleName}Type"

        return FileSpec.builder(key.packageName, funcName)
            .addType(
                TypeSpec.enumBuilder(enumName)
                    .apply {
                        value.forEach {
                            addEnumConstant(it.simpleName.uppercase())
                        }
                    }
                    .build()
            )
            .addFunction(
                FunSpec.builder(funcName)
                    .addParameter("key", ClassName(key.packageName, enumName))
                    .returns(key)
                    .beginControlFlow("return when (key)")
                    .apply {
                        value.forEach {
                            addStatement("${enumName}.${it.simpleName.uppercase()} -> %T()", it)
                        }
                    }
                    .endControlFlow()
                    .build()
            )
            .build()
    }
}
