
# FindSurface-BasicDemo-Android (Kotlin)

**Curv*Surf* FindSurface™** BasicDemo for Android (Kotlin)

## Overview

----

This sample source code demonstrates the basic usage of FindSurface for a simple task, which attempts to search for specific geometry shapes in point cloud data. 

[FindSurfaceFramework] is required to build the source code into a program. Download the framework [here] and refer to [here] for an instruction about how to setup your project with the source code to build it.



## About the source code

----

Look around  `runFindSurfaceDemo` function of FindSurfaceDemo.swift file first, where FindSurface APIs are called.  The logic in the function consists of the following 4 steps:

### Obtaining FindSurface Context

````kotlin
val fsCtx = FindSurface.getInstance()
````

First of all, obtain the FindSurface context, which is a singleton instance, to call FindSurface APIs using this instance.

You can either use it once and then dump it as a single-use object because a static variable holds its strong reference inside of FindSurface class, or keep the reference in a static variable of your class for your convenience. Ever since the context is created once, it will not be released until the application is terminated (the internal storage for input point clouds can be released by explicitly calling `cleanUp` function if the input points are not used anymore).

### Setting Input point cloud and parameters

````kotlin
// points declared as a FloatArray instance containing xyz values contiguously
fsCtx.measurementAccuracy = measurementAccuracy
fsCtx.meanDistance = meanDistance

val pointBuffer = ByteBuffer.allocateDirect(Float.SIZE_BYTES * points.size)
		.order(ByteOrder.nativeOrder()).asFloatBuffer().put(points).rewind()

fsCtx.setPointCloudData(pointBuffer, 
                        points.size / 3,
                        Float.SIZE_BYTES * 3,
                        false)
````

When an application is ready for an input point cloud, pass it to FindSurface along with parameters related to the points. Refer to [here] for the meanings of the parameters.

> Note: The input buffer must be allocated through  `ByteBuffer.allocateDirect` method and the byte order must be set to  `ByteOrder.nativeOrder()`. 

### Invoking FindSurface algorithm

````kotlin
// in the for loops of runDemo function.
for (preset in normalPresets) {
  	...
  	try {
      	val result = fsCtx.findSurface(preset.featureType,
                          preset.seedIndex,
                          seedRadius,
                          false)
      	if (result != null) {
          	... // do something with the result
        } else {
          	... // not found case
        }
    } catch (e: Exception) {
      	when (e) {
          	is InvalidArgumentException -> ...
          	is InvalidOperationException -> ...
          	else -> ...
        }
    }
}
````

The parameters of  `findSurface` method are composed of `featureType`, `seedIndex`, `seedRadius` and `requestInlierFlags`. The `featureType` is an enum value of `FindSurface.FeatureType`, which can be one of the five geometric shapes (i.e., `plane`, `sphere`, `cylinder`, `cone`, `torus`) and `any`, which means "try finding one of the five". Refer to [here] for the detailed descriptions of the parameters.

This method returns a result of an nullable abstract type, which is also named `FindSurfaceResult` that every geometric surface types inherits. If the method fails to detect any geometric shape, the method returns `null`.

FindSurface throws an `Exception` if it fails to execute its algorithm for any reason (e.g., an invalid parameter value, lack of memory). The possible types that the  `Exception`  represents include `InvalidArgumentException` and `InvalidOperationException`, which indicates some parameters are missing or input point cloud is not set. It is recommended to design your application defensively so that your application does not have to catch any exception. Refer to [here] for the cases of when FindSurface throws an `Exception`.

### Fetching the Result

````kotlin
sealed class FindSurfaceParam(result: FindSurfaceResult) {
  	val type: FeatureType = result.type
  	val rmsError: Float = result.rmsError
  	val inlierFlags: FindSurfaceInlierFlags? = result.inlierFlags
}

val FindSurfaceResult.wrappedParam: FindSurfaceParam
		get() = when (type) {
      	FeatureType.PLANE -> FindPlaneParam(this)
      	FeatureType.SPHERE -> FindSphereParam(this)
      	FeatureType.Cylinder -> FindCylinderParam(this)
      	FeatureType.Cone -> FindConeParam(this)
      	FeatureType.Torus -> FindTorusParam(this)
      	else -> /* will never reach here */
    }

data class Float3(val x: Float, val y: Float, val z: Float)

...

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
      	...
    }
}
````

The  `FindSurfaceResult` instance resulted from `FindSurface.findSurface` contains the `type` of the geometry found, `rmsError`, and the information of that geometry. 

>  Note: In this sample code, a wrapper class with a simple vector type is introduced as an example, because of the lack of a structure like C's union and the other reason, which is mentioned below. It is recommended to define your own wrapper and vector types that is appropriate for your application domain.

The `type` property has a value of `FeatureType` and can be one of the five types. The type will be the same as the input parameter, except for several special cases (refer to [Auto Detection] and [Smart Conversion]). Since the result type cannot be set to `any`, the `default` section will never be executed. 

The `rmsError` property describes the root-mean-square value of errors in orthogonal distance, which means distances in normal direction between inlier points and the surface model that FindSurface detects. The value describes how much of the points fits the geometric model well and it is not related to the algorithm's accuracy. This value will get greater as the points have greater errors in measurement, which means the result also be affected by the errors.

The `param` property is a `FloatBuffer` containing the sizes and locations of the geometry found. Since we couldn't choose appropriate vector types used commonly in Android Java/Kotlin for general purposed usage, we decided to expose the information in a form of `FloatBuffer` and provide index constants so that you can read the values at pre-defined positions.



## About point cloud

--------------------

The point cloud in this demo is the same as the sample used in FindSurface WebDemo. Please refer to the [WebDemo] for a visual representation of FindSurface's results. 

