package com.anime.alarm.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import java.util.Locale

class BatteryOptimizationHelper(private val context: Context) {

    fun getOptimizationIntent(): Intent? {
        val manufacturer = Build.MANUFACTURER.lowercase(Locale.ROOT)
        var intent: Intent? = null

        try {
            when {
                manufacturer.contains("xiaomi") || manufacturer.contains("redmi") -> {
                    intent = Intent()
                    intent.component = ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")
                }
                manufacturer.contains("oppo") -> {
                    intent = Intent()
                    intent.component = ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")
                }
                manufacturer.contains("vivo") -> {
                    intent = Intent()
                    intent.component = ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")
                }
                manufacturer.contains("letv") -> {
                    intent = Intent()
                    intent.component = ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity")
                }
                manufacturer.contains("honor") || manufacturer.contains("huawei") -> {
                    intent = Intent()
                    intent.component = ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")
                }
                manufacturer.contains("asus") -> {
                    intent = Intent()
                    intent.component = ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.powersaver.PowerSaverSettings")
                }
                manufacturer.contains("nokia") -> {
                    intent = Intent()
                    intent.component = ComponentName("com.hmdglobal.app.battery", "com.hmdglobal.app.battery.AppBatteryActivity")
                }
                manufacturer.contains("samsung") -> {
                    intent = Intent()
                    intent.component = ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity")
                }
                manufacturer.contains("oneplus") -> {
                    intent = Intent()
                    intent.component = ComponentName("com.oneplus.security", "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity")
                }
                // Fallback for standard Android Battery Optimization
                else -> {
                    intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // If specific intent fails, fallback to generic settings
            intent = Intent(Settings.ACTION_SETTINGS)
        }

        return intent
    }

    fun isVendorSpecificOptimizationNeeded(): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase(Locale.ROOT)
        return manufacturer.contains("xiaomi") || 
               manufacturer.contains("redmi") ||
               manufacturer.contains("oppo") || 
               manufacturer.contains("vivo") || 
               manufacturer.contains("huawei") || 
               manufacturer.contains("honor") ||
               manufacturer.contains("oneplus") ||
               manufacturer.contains("samsung")
    }
}

@Composable
fun BatteryOptimizationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Critical Permission Needed") },
        text = { 
            Text("To ensure your Anime Alarm actually wakes you up, please allow 'Auto-start' or remove 'Battery Optimization' in the next screen.\n\nOtherwise, the system WILL kill the alarm.") 
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Fix Now")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Risk It")
            }
        }
    )
}
