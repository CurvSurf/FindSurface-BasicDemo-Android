package com.example.findsurface_basicdemo_android

import android.util.Log
import com.curvsurf.findsurface.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

interface Preset {
    val featureType: FeatureType
    val seedIndex: Int
}

object FindSurfaceDemo {

    var trial = 1

    fun <P0: Preset, P1: Preset> runDemo(normalPresets: Array<P0>,
                                         smartPresets: Array<P1>,
                                         points: FloatArray) {

        trial = 1

        val measurementAccuracy: Float = 0.01F
        val meanDistance: Float = 0.01F
        val seedRadius: Float = 0.025F

        val fsCtx = FindSurface.getInstance()

        fsCtx.measurementAccuracy = measurementAccuracy
        fsCtx.meanDistance = meanDistance

        val pointBuffer = ByteBuffer.allocateDirect(Float.SIZE_BYTES * points.size)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer().put(points).rewind()
        fsCtx.setPointCloudData(pointBuffer, points.size / 3, Float.SIZE_BYTES * 3, false)


        printMsg("Normal cases: ")

        for (preset in normalPresets) {
            runTest(fsCtx, preset.featureType, preset.seedIndex, seedRadius)
        }

        printMsg("Smart cases: ")

        trial = 1

        fsCtx.smartConversionOptions = EnumSet.of(
            SmartConversionOption.CONE_TO_CYLINDER,
            SmartConversionOption.TORUS_TO_SPHERE,
            SmartConversionOption.TORUS_TO_CYLINDER
        )

        for (preset in smartPresets) {
            runTest(fsCtx, preset.featureType, preset.seedIndex, seedRadius)
        }
    }

    private fun runTest(fsCtx: FindSurface, featureType: FeatureType, seedIndex: Int, seedRadius: Float) {

        printMsg("${trial++}. FindSurface searched for a $featureType")
        printMsg("around the point of which index is $seedIndex.")
        try {

            val result = fsCtx.findSurface(featureType, seedIndex, seedRadius, false)

            if (result != null) {
                printMsg("Found a ${result.type} as a result:")
                printMsg("${result.wrappedParam}")
            } else {
                printMsg("Not found.")
            }

        } catch (e: Exception) {
            printMsg("Couldn't run FindSurface due to the following exception: \n${e.message}")
        }
        printMsg(" ")
    }

    private fun printMsg(message: String) {
        message.split("\n").forEach {
            Log.d("FindSurfaceDemo", it)
        }
    }
}

sealed class FindSurfaceParam(result: FindSurfaceResult) {
    val type: FeatureType = result.type
    val rmsError: Float = result.rmsError
    val inlierFlags: FindSurfaceInlierFlags? = result.inlierFlags
}

val FindSurfaceResult.wrappedParam: FindSurfaceParam
    get() = when (type) {
        FeatureType.PLANE -> FindPlaneParam(this)
        FeatureType.SPHERE -> FindSphereParam(this)
        FeatureType.CYLINDER -> FindCylinderParam(this)
        FeatureType.CONE -> FindConeParam(this)
        FeatureType.TORUS -> FindTorusParam(this)
        else -> throw Error("This code must never be executed.")
    }

data class Float3(val x: Float, val y: Float, val z: Float)

class FindPlaneParam(
    result: FindSurfaceResult
) : FindSurfaceParam(result) {

    val lowerLeft = Float3(
        result.params[FindSurfaceResult.PLANE_PARAM_LOWER_LEFT_X],
        result.params[FindSurfaceResult.PLANE_PARAM_LOWER_LEFT_Y],
        result.params[FindSurfaceResult.PLANE_PARAM_LOWER_LEFT_Z]
    )

    val lowerRight = Float3(
        result.params[FindSurfaceResult.PLANE_PARAM_LOWER_RIGHT_X],
        result.params[FindSurfaceResult.PLANE_PARAM_LOWER_RIGHT_Y],
        result.params[FindSurfaceResult.PLANE_PARAM_LOWER_RIGHT_Z]
    )

    val upperRight = Float3(
        result.params[FindSurfaceResult.PLANE_PARAM_UPPER_RIGHT_X],
        result.params[FindSurfaceResult.PLANE_PARAM_UPPER_RIGHT_Y],
        result.params[FindSurfaceResult.PLANE_PARAM_UPPER_RIGHT_Z]
    )

    val upperLeft = Float3(
        result.params[FindSurfaceResult.PLANE_PARAM_UPPER_LEFT_X],
        result.params[FindSurfaceResult.PLANE_PARAM_UPPER_LEFT_Y],
        result.params[FindSurfaceResult.PLANE_PARAM_UPPER_LEFT_Z]
    )

    override fun toString(): String {
        return """
            Plane (rms error: $rmsError)
                Lower Left: $lowerLeft
                Lower Right: $lowerRight
                Upper Right: $upperRight
                Upper Left: $upperLeft
        """.trimIndent()
    }
}

class FindSphereParam(
    result: FindSurfaceResult
) : FindSurfaceParam(result) {

    val center = Float3(
        result.params[FindSurfaceResult.SPHERE_PARAM_CENTER_X],
        result.params[FindSurfaceResult.SPHERE_PARAM_CENTER_Y],
        result.params[FindSurfaceResult.SPHERE_PARAM_CENTER_Z]
    )

    val radius: Float = result.params[FindSurfaceResult.SPHERE_PARAM_RADIUS]

    override fun toString(): String {
        return """
            Sphere (rms error: $rmsError)
                Center: $center
                Radius: $radius
        """.trimIndent()
    }
}

class FindCylinderParam(
    result: FindSurfaceResult
) : FindSurfaceParam(result) {

    val bottomCenter = Float3(
        result.params[FindSurfaceResult.CYLINDER_PARAM_BOTTOM_X],
        result.params[FindSurfaceResult.CYLINDER_PARAM_BOTTOM_Y],
        result.params[FindSurfaceResult.CYLINDER_PARAM_BOTTOM_Z]
    )

    val topCenter = Float3(
        result.params[FindSurfaceResult.CYLINDER_PARAM_TOP_X],
        result.params[FindSurfaceResult.CYLINDER_PARAM_TOP_Y],
        result.params[FindSurfaceResult.CYLINDER_PARAM_TOP_Z]
    )

    val radius: Float = result.params[FindSurfaceResult.CYLINDER_PARAM_RADIUS]

    override fun toString(): String {
        return """
            Cylinder (rms error: $rmsError)
                Bottom Center: $bottomCenter
                Top Center: $topCenter
                Radius: $radius
        """.trimIndent()
    }
}

class FindConeParam(
    result: FindSurfaceResult
) : FindSurfaceParam(result) {

    val bottomCenter = Float3(
        result.params[FindSurfaceResult.CONE_PARAM_BOTTOM_X],
        result.params[FindSurfaceResult.CONE_PARAM_BOTTOM_Y],
        result.params[FindSurfaceResult.CONE_PARAM_BOTTOM_Z]
    )

    val topCenter = Float3(
        result.params[FindSurfaceResult.CONE_PARAM_TOP_X],
        result.params[FindSurfaceResult.CONE_PARAM_TOP_Y],
        result.params[FindSurfaceResult.CONE_PARAM_TOP_Z]
    )

    val bottomRadius: Float = result.params[FindSurfaceResult.CONE_PARAM_BOTTOM_RADIUS]

    val topRadius: Float = result.params[FindSurfaceResult.CONE_PARAM_TOP_RADIUS]

    override fun toString(): String {
        return """
            Cone (rms error: $rmsError)
                Bottom Center: $bottomCenter
                Top Center: $topCenter
                Bottom Radius: $bottomRadius
                Top Radius: $topRadius
        """.trimIndent()
    }
}

class FindTorusParam(
    result: FindSurfaceResult
) : FindSurfaceParam(result) {

    val center = Float3(
        result.params[FindSurfaceResult.TORUS_PARAM_CENTER_X],
        result.params[FindSurfaceResult.TORUS_PARAM_CENTER_Y],
        result.params[FindSurfaceResult.TORUS_PARAM_CENTER_Z]
    )

    val axis = Float3(
        result.params[FindSurfaceResult.TORUS_PARAM_NORMAL_X],
        result.params[FindSurfaceResult.TORUS_PARAM_NORMAL_Y],
        result.params[FindSurfaceResult.TORUS_PARAM_NORMAL_Z]
    )

    val meanRadius: Float = result.params[FindSurfaceResult.TORUS_PARAM_MEAN_RADIUS]

    val tubeRadius: Float = result.params[FindSurfaceResult.TORUS_PARAM_TUBE_RADIUS]

    override fun toString(): String {
        return """
            Torus (rms error: $rmsError)
                Center: $center
                Axis: $axis
                Mean Radius: $meanRadius
                Tube Radius: $tubeRadius
        """.trimIndent()
    }
}