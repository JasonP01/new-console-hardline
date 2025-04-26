package newconsole.runtime

import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.jvm.*
import kotlin.script.experimental.jvmhost.JvmScriptCompiler

object KtsEval {

    val scriptCompiler by lazy {
        JvmScriptCompiler()
    }

    val scriptEvaluator by lazy {
        BasicJvmScriptEvaluator()
    }

    val scriptCompileConfig by lazy {
        ScriptCompilationConfiguration {
            jvm {
                dependenciesFromCurrentContext(wholeClasspath = true)
                dependenciesFromClassloader(classLoader = Thread.currentThread().contextClassLoader)
            }
        }
    }

}
