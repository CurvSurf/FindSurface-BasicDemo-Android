package com.example.findsurface_basicdemo_android

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.curvsurf.findsurface.FindSurface
import com.example.findsurface_basicdemo_android.ui.theme.FindSurfaceBasicDemoAndroidTheme
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.nio.ByteBuffer
import java.util.*

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FindSurfaceBasicDemoAndroidTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Content()
                }
            }
        }

        val scanner = Scanner(resources.assets.open("pointcloud.xyz"))

        val points = generateSequence {
            if (scanner.hasNextLine()) scanner.nextLine()
            else null
        }.mapNotNull { line ->
            line.split(" ")
                .takeIf { it.size == 3 }
                ?.mapNotNull {
                    it.toFloatOrNull()
                }?.takeIf { it.size == 3 }
        }.flatten().toList().toFloatArray()

        FindSurfaceDemo.runDemo(NORMAL_PRESET_LIST, SMART_PRESET_LIST, points)
    }
}

@Composable
fun Content() {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.curvsurf_logo_icon),
                contentDescription = "CurvSurf Logo",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
            )
            Text(
                text = "FindSurface Basic Demo",
                fontSize = 24.sp
            )
        }

        Text(text = "The input point cloud of this application looks like as follows:")

        Image(
            painter = painterResource(id = R.drawable.sample_pc),
            contentDescription = "Point cloud",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        Text(
            text = "TL;DR. Look at the Logcat to see the actual result.",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "This demo project demonstrates a textual example of how to use FindSurface APIs in the source code.",
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "This application attempts to search specific geometry shapes in the point cloud by using FindSurface APIs. The result will be printed in a debug console after the application begins.",
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    FindSurfaceBasicDemoAndroidTheme {
        Content()
    }
}