package expo.modules.wifimanager

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import androidx.annotation.RequiresApi
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition

class WifiManagerModule : Module() {
  override fun definition() = ModuleDefinition {
    Name("WifiManager")
    AsyncFunction("scan") {
      val ctx = requireNotNull(appContext.reactContext)
      val wifi = ctx.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
      // Scan results may be cached/throttled by Android. Location permission and services are required.
      @Suppress("DEPRECATION")
      wifi.startScan()
      @Suppress("DEPRECATION")
      val rows = wifi.scanResults.mapNotNull { r ->
        if (r.SSID.isNullOrBlank()) null else {
          val freq = r.frequency
          val band = when { freq >= 5925 -> "6GHz"; freq >= 4900 -> "5GHz"; freq >= 2400 -> "2.4GHz"; else -> "-" }
          val channel = when { freq == 2484 -> 14; freq in 2412..2472 -> (freq-2407)/5; freq in 5000..5895 -> (freq-5000)/5; freq in 5955..7115 -> (freq-5950)/5; else -> null }
          val caps = r.capabilities ?: ""
          val auth = when { caps.contains("SAE") -> "WPA3-SAE"; caps.contains("EAP") -> "Enterprise"; caps.contains("PSK") -> "WPA2-Personal"; caps.contains("OWE") -> "OWE"; caps.contains("WEP") -> "WEP"; else -> "Open" }
          mapOf("ssid" to r.SSID, "signal" to WifiManager.calculateSignalLevel(r.level,101), "channel" to channel, "authentication" to auth, "band" to band)
        }
      }
      rows.groupBy { it["ssid"] }.map { (_, items) -> items.maxByOrNull { it["signal"] as Int }!! }.sortedByDescending { it["signal"] as Int }
    }
    AsyncFunction("status") {
      val ctx = requireNotNull(appContext.reactContext)
      val wifi = ctx.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
      val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
      val active = cm.activeNetwork
      val caps = cm.getNetworkCapabilities(active)
      val connected = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
      @Suppress("DEPRECATION") val info = wifi.connectionInfo
      val ssid = if(connected) (info?.ssid ?: "").removeSurrounding("\\\"").removeSurrounding("\"").takeUnless { it == "<unknown ssid>" } ?: "" else ""
      mapOf("ssid" to ssid, "state" to if(connected) "connected" else "disconnected", "signal" to if(connected && info!=null) WifiManager.calculateSignalLevel(info.rssi,101).toString() else "")
    }
    AsyncFunction("requestConnection") { ssid: String, password: String, authentication: String ->
      require(ssid.isNotBlank() && ssid.length <= 32 && !ssid.contains('\n')) { "잘못된 SSID" }
      require(Build.VERSION.SDK_INT >= 29) { "Android 10 이상이 필요합니다" }
      val open = authentication == "Open"
      require(open || (password.length in 8..63)) { "비밀번호는 8~63자여야 합니다" }
      require(open || authentication.contains("Personal") || authentication.contains("SAE")) { "지원하지 않는 인증 방식입니다" }
      val ctx = requireNotNull(appContext.reactContext)
      val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
      val builder = WifiNetworkSpecifier.Builder().setSsid(ssid)
      if (!open) {
        if(authentication.contains("SAE")) builder.setWpa3Passphrase(password)
        else builder.setWpa2Passphrase(password)
      }
      val request = NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI).setNetworkSpecifier(builder.build()).build()
      val callback = object: ConnectivityManager.NetworkCallback() {}
      // A local, app-scoped connection request. The OS may show an approval dialog.
      // This does not silently change the device's default saved Wi-Fi connection.
      cm.requestNetwork(request,callback,15000)
      android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
        try { cm.unregisterNetworkCallback(callback) } catch (_: Exception) {}
      }, 15000)
      mapOf("requested" to true, "message" to "Android에 연결 요청을 전달했습니다. 시스템 승인 및 연결 상태를 확인하세요.")
    }
  }
}
