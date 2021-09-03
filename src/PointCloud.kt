package com.example.findsurface_basicdemo_android

import com.curvsurf.findsurface.FeatureType

data class PresetData(override val featureType: FeatureType, override val seedIndex: Int) : Preset

const val SPHERE_INDEX = 7811
const val CYLINDER_INDEX = 3437
const val CONE_INDEX = 6637
const val TORUS_INDEX = 7384

val NORMAL_PRESET_LIST = arrayOf(
    PresetData(FeatureType.SPHERE, SPHERE_INDEX),
    PresetData(FeatureType.CYLINDER, CYLINDER_INDEX),
    PresetData(FeatureType.CONE, CONE_INDEX),
    PresetData(FeatureType.TORUS, TORUS_INDEX)
)

val SMART_PRESET_LIST = arrayOf(
    PresetData(FeatureType.CONE, CYLINDER_INDEX),
    PresetData(FeatureType.TORUS, SPHERE_INDEX),
    PresetData(FeatureType.TORUS, CYLINDER_INDEX)
)
