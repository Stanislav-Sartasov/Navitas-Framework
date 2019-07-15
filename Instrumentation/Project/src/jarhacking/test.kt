package jarhacking

class Example1(){
    val check1 = "This is first line"
    fun print(){
        println("This is first method")
    }
}

fun string2(){
    println("This is second method")
}

fun string3(){
    println("This is a third method")
}

internal class Example2(){
    val test = Example1()
    fun print(){
        println("This is an invocation inside example2")
        test.print()
    }
}
