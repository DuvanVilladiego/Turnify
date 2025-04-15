import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.turnify.app.R

object PushNotificationService {

    private const val TAG = "FCM"

    fun initialize(
        activity: Activity,
        permissionLauncher: ActivityResultLauncher<String>
    ) {
        FirebaseApp.initializeApp(activity)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    activity,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    getFirebaseToken(activity)
                }

                else -> {
                    permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            getFirebaseToken(activity)
        }
    }

    fun handlePermissionResult(activity: Activity, isGranted: Boolean) {
        if (isGranted) {
            getFirebaseToken(activity)
        } else {
            Toast.makeText(
                activity,
                "Permiso de notificaciones denegado",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun getFirebaseToken(activity: Activity) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            val msg = activity.getString(R.string.msg_token_fmt, token)
            Log.d(TAG, msg)
            //Agregar guardado de token
        }
    }
}
