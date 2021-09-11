package com.comit.earth

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import com.google.ar.sceneform.*
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Light
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable

class MainActivity : AppCompatActivity() {
    companion object {

        private const val MIN_OPENGL_VERSION = 3.0

        private fun checkGlVersion(activity: Activity): Boolean {
            val openGlVersionString =
                (activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
                    .deviceConfigurationInfo
                    .glEsVersion

            if (openGlVersionString.toDouble() < MIN_OPENGL_VERSION) {
                Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                    .show()
                return false
            }
            return true
        }

    }

    private lateinit var sceneView: SceneView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!checkGlVersion(this)) {
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        sceneView = findViewById(R.id.scene_view)
        val camera = sceneView.scene.camera
        camera.localPosition = Vector3(0f, 0f, 4f) // 设置 eye 的位置
//        camera.setLookDirection()

        // 透视投影
//        camera.nearClipPlane
//        camera.farClipPlane
//        camera.verticalFovDegrees
//        camera.projectionMatrix

//        sceneView.scene.camera.light = createLight()
//        sceneView.scene.sunlight?.light = createLight()

        createRenderable()

        sceneView.scene.addOnPeekTouchListener { p0, p1 ->
            Log.d(
                "Comit",
                "result: ${p0?.point}, event: ${p1?.action}"
            )
        }
    }

    private fun createRenderable() {
        val modelPath = "models/Earthcut3.gltf"
        ModelRenderable.builder()
            .setSource(this, RenderableSource.builder()
                .setSource(this, Uri.parse(modelPath), RenderableSource.SourceType.GLTF2)
                .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                .build()
            )
            .setRegistryId(modelPath)
            .build()
            .thenAccept {
                addNodeToScene(it)
            }
            .exceptionally {
                runOnUiThread {
                    Toast.makeText(this, "Unable to load earth renderable", Toast.LENGTH_LONG).show()
                }
                return@exceptionally null
            }
    }

    private fun addNodeToScene(renderable: ModelRenderable) {
        val node = Node()
        node.localPosition = Vector3(0.0f, 0.0f, 0.0f) // 设置在 node 的位置
        node.localScale = Vector3(2f, 2f, 2f) // 设置 node 的缩放
//        node.localRotation = Quaternion(Vector3.right(), 30f) // 绕 x 轴旋转
        node.localRotation = Quaternion(Vector3.up(), 30f) // 绕 y 轴旋转
        node.renderable = renderable
        node.setParent(sceneView.scene)
        sceneView.scene.addChild(node)
        sceneView.resume() // 一定要调用 resume 触发渲染

    }

    private fun createLight(): Light {
        return Light.builder(Light.Type.FOCUSED_SPOTLIGHT)
            .setColor(com.google.ar.sceneform.rendering.Color(Color.YELLOW))
            .setShadowCastingEnabled(true)
            .build()
    }

    override fun onResume() {
        super.onResume()
        sceneView.resume()
    }

    override fun onPause() {
        super.onPause()
        sceneView.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        sceneView.destroy()
    }
}