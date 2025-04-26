package newconsole

import arc.Events
import arc.func.Func3
import kotlinx.coroutines.*
import mindustry.gen.Icon
import mindustry.mod.Mod
import newconsole.NewConsoleMod.NewConsoleInitEvent
import newconsole.console.KtsCodeArea
import newconsole.runtime.KtsEval
import newconsole.ui.CStyles
import newconsole.ui.dialogs.Console
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.util.PropertiesCollection

class NewConsoleKts: Mod() {
    init {
        Events.run(NewConsoleInitEvent::class.java){
            //TODO works fine but WHAT THE FUCK DO YOU MEAN YOU CANT FIND THE STDLIB IT IS RIGHT FUCKING THERE
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
        }
    }

    private fun runScript(confCompile: (ScriptCompilationConfiguration.Builder.() -> Unit)? = null, confEval: (ScriptEvaluationConfiguration.Builder.() -> Unit)? = null, prescript: String = "", script: String = "", postscript: String = ""): String{
        return runBlocking {
            val resultI = try {
                val compileConfig = ScriptCompilationConfiguration(KtsEval.scriptCompileConfig) {
                    confCompile?.invoke(this)
                }
                val evalConfig = ScriptEvaluationConfiguration {
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
