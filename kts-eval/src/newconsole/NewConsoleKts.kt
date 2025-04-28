package newconsole

import arc.*
import kotlinx.coroutines.*
import mindustry.gen.*
import mindustry.mod.*
import newconsole.NewConsoleMod.*
import newconsole.console.*
import newconsole.js.NCJSLink
import newconsole.runtime.*
import newconsole.ui.*
import newconsole.ui.dialogs.*
import kotlin.collections.set
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.*
import kotlin.script.experimental.util.*

class NewConsoleKts: Mod() {
    init {
        Events.run(NewConsoleInitEvent::class.java){
            ConsoleVars.consoles.add(Console(KtsCodeArea("", CStyles.monoArea), "KTS", {code ->
                val result = runScript(script = code)

                if(result.contains("Loading modules:", true)) {
                    result.substringAfter("Loading modules:") // strip everything before list of modules
                        .substringAfter("]") // strip to the end of the list
                        .trimStart()
                        //why is there two of these
                        .substringAfter("Loading modules:")
                        .substringAfter("]")
                        .trimStart()
                } else result
            }, {scr, variable, eventObj ->
                val result = runScript(confEval = {
                    data[PropertiesCollection.Key<String>("_autorun_event")] = eventObj
                }, script = scr.replace("_autorun_event", variable))

                if(result.contains("Loading modules:", true)) {
                    result.substringAfter("Loading modules:") // strip everything before list of modules
                        .substringAfter("]") // strip to the end of the list
                        .trimStart()
                        //why is there two of these
                        .substringAfter("Loading modules:")
                        .substringAfter("]")
                        .trimStart()
                } else result
            }).apply {
                buttonIcon = Icon.android
            })

            NCJSLink.importPackage("newconsole.runtime", "newconsole.console")
        }
    }

    private fun runScript(confCompile: (ScriptCompilationConfiguration.Builder.() -> Unit)? = null, confEval: (ScriptEvaluationConfiguration.Builder.() -> Unit)? = null, prescript: String = "", script: String = "", postscript: String = ""): String{
        return runBlocking {
            val resultI = try {
                val compileConfig = ScriptCompilationConfiguration(KtsEval.scriptCompileConfig) {
                    confCompile?.invoke(this)
                }
                val evalConfig = ScriptEvaluationConfiguration(KtsEval.scriptEvalConfig) {
                    compilationConfiguration(compileConfig)
                    confEval?.invoke(this)
                }
                val compiledScript = KtsEval.scriptCompiler("$prescript\n$script\n$postscript".toScriptSource(), compileConfig).valueOrThrow()
                KtsEval.scriptEvaluator(compiledScript, evalConfig).valueOrThrow().returnValue
            } catch(e: Exception) {
                ResultValue.Error(e)
            }

            when (resultI) {
                is ResultValue.Value -> {
                    val value = when (val value = resultI.value) {
                        is Deferred<*> -> value.await()
                        is Job -> value.join()
                        else -> value
                    }
                    "${resultI.type}: $value"
                }
                is ResultValue.Error -> {
                    resultI.toString()
                }
                is ResultValue.Unit, ResultValue.NotEvaluated -> {
                    "<no output>"
                }
            }
        }
    }
}
