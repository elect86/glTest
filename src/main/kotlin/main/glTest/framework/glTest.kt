package main.glTest.framework

import gli_.has
import glm_.d
import glm_.vec4.Vec4
import main.glTest.problems.Problem
import org.lwjgl.glfw.GLFW.*
import uno.glfw.KeyCallbackT
import uno.glfw.glfw


const val DEBUG = true

var shouldQuit = false

lateinit var app: ApplicationState

fun main() {

    glfw.init()

    app = ApplicationState()

    while (!shouldQuit) {

        glfw.pollEvents()

        shouldQuit = shouldQuit || (app.benchmarkMode && app.isBenchmarkModeComplete())
        if (shouldQuit) break

        app.update()
        render(app.getActiveProblem(), app.activeApi)
    }

    if (app.benchmarkMode) {
        println("\n\nResults")
        println(asTable(app.benchmarkResults))
    }

    app.destroy()
    cleanup()
}

val keyCallback: KeyCallbackT = { key: Int, _: Int, action: Int, mods: Int ->
    if (action == GLFW_PRESS)
        if (key == GLFW_KEY_Q || key == GLFW_KEY_ESCAPE || (key == GLFW_KEY_F4 && mods has GLFW_MOD_ALT))
            shouldQuit = true
        else when (key) {
            GLFW_KEY_LEFT -> if (!app.benchmarkMode) app.prevProblem()
            GLFW_KEY_RIGHT -> {
                println("nextProblem")
                if (!app.benchmarkMode) app.nextProblem()
            }
            GLFW_KEY_UP -> if (!app.benchmarkMode) app.prevSolution()
            GLFW_KEY_DOWN -> if (!app.benchmarkMode) app.nextSolution()
            GLFW_KEY_A -> if (!app.benchmarkMode) app.nextAPI()
        }
}

val clearColor = Vec4(0f, 0f, 0f, 1f)

fun render(activeProblem: Problem?, activeApi: OpenGLBase) {
    if (activeProblem == null) return

    val clearDepth = activeProblem.getClearValues(clearColor)

    activeApi.clear(clearColor, clearDepth)

    // This is the main entry point shared by all tests.
    activeProblem.render()

    // Present the results.
    activeApi.swapBuffers()
}

fun asTable(results: BenchmarkResults): String {

    var retStr = ""
    val headerFmt = " %-23s %-10s %-30s %7s %12s %12s %12s\n"
    val rowFmt = " %-23s %-10s %-30s %7d %12.3f %12.3f %12.3f\n"
    val rowFailFmt = " %-23s %-10s %-30s %7s %12s %12s %12s\n"

    retStr += headerFmt.format("Problem", "API", "Solution", "Frames", "Elapsed (s)", "fps", "ms/f")

    val rows = ArrayList<BenchmarkRow>()

    // First, accumulate data into rows.
    results.forEach { key, value ->
        val (glApiName, problemName, solutionName) = key
        val (frameCount, elapsedMs, workCount) = value

        val elapsedS = elapsedMs / 1_000.0

        if (frameCount != 0 && elapsedS != 0.0) {
            val fps = frameCount / elapsedS
            val mspf = elapsedMs.d / frameCount
            val wps = workCount / elapsedS

            val newRow = BenchmarkRow(glApiName, problemName, solutionName,
                    frameCount, elapsedS, workCount, fps, mspf, wps)

            rows += newRow
        } else {
            val newRow = BenchmarkRow(glApiName, problemName, solutionName,
                    frameCount, elapsedS, workCount, 0.0, 0.0, 0.0)
        }
    }

    rows.sortWith(benchmarkSorter)

    for (row in rows) {

        retStr += if (row.frameCount != 0 && row.elapsedS != 0.0)
            rowFmt.format(row.problemName, row.glApiName, row.solutionName, row.frameCount, row.elapsedS, row.framesPerSecond, row.millisecondsPerFrame)
        else
            rowFailFmt.format(row.problemName, row.glApiName, row.solutionName, "N/A", "N/A", "N/A", "N/A")
    }
    return retStr
}

val benchmarkSorter = compareBy(BenchmarkRow::problemName, BenchmarkRow::glApiName, BenchmarkRow::millisecondsPerFrame)

class BenchmarkRow(
        val glApiName: String,
        val problemName: String,
        val solutionName: String,
        val frameCount: Int,
        val elapsedS: Double,
        val workCount: Int,
        val framesPerSecond: Double,
        val millisecondsPerFrame: Double,
        val workPerSecond: Double)

fun cleanup() = glfw.terminate()