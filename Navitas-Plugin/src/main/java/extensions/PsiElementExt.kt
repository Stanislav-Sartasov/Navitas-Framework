package extensions

import com.intellij.psi.PsiElement

fun PsiElement.findChildWithPrefix(prefix: String): PsiElement? = children.firstOrNull { child -> child.text.startsWith(prefix) }

fun PsiElement.hasChildWithText(text: String): Boolean = children.any { child -> child.text.contains(text) }