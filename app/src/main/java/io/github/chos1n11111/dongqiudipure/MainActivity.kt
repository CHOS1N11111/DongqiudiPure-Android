package io.github.chos1n11111.dongqiudipure

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // 深浅色跟随系统。若后续在设置中提供三档切换，
            // 由 :feature:settings 读取偏好后传入 darkTheme 覆盖值。
            DqdTheme {
                DqdApp()
            }
        }
    }
}
