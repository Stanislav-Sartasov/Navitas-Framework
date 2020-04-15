package extensions

import com.intellij.psi.PsiElement
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElementFactory
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression

fun GroovyPsiElementFactory.createBreakLineElement(): PsiElement = createLineTerminator("\n")

fun GroovyPsiElementFactory.createClassPathExpression(path: String): GrExpression = createExpressionFromText("classpath \"$path\"")

fun GroovyPsiElementFactory.createApplyPluginExpression(pluginName: String): GrExpression = createExpressionFromText("apply plugin: \"$pluginName\"")