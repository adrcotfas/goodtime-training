package goodtime.training.wod.timer.common.sound_and_vibration

import android.content.Context
import android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE
import android.hardware.camera2.CameraManager

class TorchHelper(context: Context) {
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var cameraId: String? = null

    @Volatile
    private var thread = Thread{}

    init {
        for(id in cameraManager.cameraIdList) {
            val flashAvailable = cameraManager.getCameraCharacteristics(id).get(FLASH_INFO_AVAILABLE)
            if (flashAvailable == true) {
                cameraId = id
                break
            }
        }
    }

    fun notifyCountdown() {
        thread = Thread {
            cameraId?.let {
                try{
                    cameraManager.setTorchMode(it, true)
                    Thread.sleep(400)
                    cameraManager.setTorchMode(it, false)
                    Thread.sleep(600)
                    cameraManager.setTorchMode(it, true)
                    Thread.sleep(400)
                    cameraManager.setTorchMode(it, false)
                    Thread.sleep(600)
                    cameraManager.setTorchMode(it, true)
                    Thread.sleep(400)
                    cameraManager.setTorchMode(it, false)
                    Thread.sleep(600)
                    cameraManager.setTorchMode(it, true)
                    Thread.sleep(900)
                    cameraManager.setTorchMode(it, false)
                } catch (e : InterruptedException) {
                    // it's fine - the torch was stopped
                }
            }
        }
        if (!thread.isAlive && !thread.isInterrupted) {
            thread.start()
        }
    }

    fun notifyFastTwice() {
        thread = Thread {
            cameraId?.let {
                try {
                    cameraManager.setTorchMode(it, true)
                    Thread.sleep(200)
                    cameraManager.setTorchMode(it, false)
                    Thread.sleep(100)
                    cameraManager.setTorchMode(it, true)
                    Thread.sleep(200)
                    cameraManager.setTorchMode(it, false)
                } catch (e : InterruptedException) {
                    // it's fine - the torch was stopped
                }
            }
        }
        if (!thread.isAlive && !thread.isInterrupted) {
            thread.start()
        }
    }

    fun stop() {
        if (thread.isAlive && !thread.isInterrupted) {
            thread.interrupt()
        }
    }
}