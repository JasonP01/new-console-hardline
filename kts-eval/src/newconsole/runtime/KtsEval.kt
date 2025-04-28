package newconsole.runtime

import mindustry.Vars
import mindustry.mod.Mod
import mindustry.mod.Mods.LoadedMod
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.jvm.BasicJvmScriptEvaluator
import kotlin.script.experimental.jvm.dependenciesFromClassloader
import kotlin.script.experimental.jvm.jvm
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
                dependenciesFromClassloader(
                    wholeClasspath = true,
                    //get classloader from this context which points to the mod jar file
                    classLoader = {}::class.java.classLoader
                )

                //get all other mods' class loaders and include them
                eachJavaMod {
                    dependenciesFromClassloader(
                        wholeClasspath = true,
                        classLoader = it::class.java.classLoader
                    )
                }
            }
        }
    }

    val scriptEvalConfig by lazy {
        ScriptEvaluationConfiguration {
            jvm {

            }
        }
    }

    private fun eachJavaMod(mod: (Mod) -> Unit){
        Vars.mods.orderedMods().each{
            if(it.main != null) mod(it.main)
        }
    }
}
