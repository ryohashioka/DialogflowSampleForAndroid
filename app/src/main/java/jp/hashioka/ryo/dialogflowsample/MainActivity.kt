package jp.hashioka.ryo.dialogflowsample

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.ToggleButton
import androidx.core.app.ActivityCompat
import jp.hashioka.ryo.dialogflowsample.dialogflow.DetectIntent
import jp.hashioka.ryo.dialogflowsample.dialogflow.StreamingDetectIntent
import jp.hashioka.ryo.dialogflowsample.dialogflow.VoiceRecorder
import jp.wiseplants.googleassistanttest.activity.fragment.MessageDialogFragment


class MainActivity : AppCompatActivity(), MessageDialogFragment.Listener {

    /**
     * dialogflow からの応答処理でコールバック関数で使用
     *
     */
    val handler = Handler()
    private var mVoiceRecorder : VoiceRecorder? = null

    override fun onStart() {
        super.onStart()
        requestPermission()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // MEMO: 音声入力でないときはこっちを使用
//        val detectIntent = DetectIntent(this)

        val streamingDetectIntent = StreamingDetectIntent(this)

        findViewById<Button>(R.id.greetingBtn).setOnClickListener {
            findViewById<TextView>(R.id.textView).text = streamingDetectIntent.send("こんにちは")
        }
        findViewById<Button>(R.id.weatherBtn).setOnClickListener {
            findViewById<TextView>(R.id.textView).text = streamingDetectIntent.send("今日の天気は？")
        }
        findViewById<Button>(R.id.resetBtn).setOnClickListener {
            streamingDetectIntent.resetContexts()
            findViewById<TextView>(R.id.textView).text = "-"
        }
        findViewById<ToggleButton>(R.id.micBtn).setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked) {
                // パーミッションが許可されていなければ開始しない。
                if (!isPermissionGranted()) {
                    return@setOnCheckedChangeListener
                }

                val mVoiceCallback = object : VoiceRecorder.Callback() {
                    override fun onVoiceStart() {
                        // 音声入力開始。ストリーミング開始する。
                        val sampleRate = if(mVoiceRecorder == null) 16000 else mVoiceRecorder!!.sampleRate
                        streamingDetectIntent.startStream(sampleRate)
                    }
                    override fun onVoice(data: ByteArray, size: Int) {
                        // ストリーミング
                        streamingDetectIntent.streaming(data, size)
                    }
                    override fun onVoiceEnd() {
                        // 音声入力終了（音声の切れ目）。Dialogflow から返却されたテキストを画面に表示する。
                        streamingDetectIntent.stopStream {
                            if(it.isEmpty()) return@stopStream
                            handler.post {
                                findViewById<TextView>(R.id.textView).text = it
                            }
                        }
                    }
                }

                // 音声入力開始
                mVoiceRecorder = VoiceRecorder(mVoiceCallback)
                mVoiceRecorder?.start()
            } else {
                // 音声入力終了 喋ってる最中に切ると落ちる？
                mVoiceRecorder?.stop()
                mVoiceRecorder = null
            }
        }
    }

    // region PERMISSION

    private val FRAGMENT_MESSAGE_DIALOG = "message_dialog"

    private fun showPermissionMessageDialog() {
        MessageDialogFragment
            .newInstance(getString(R.string.dialog__audio_permission_required))
            .show(supportFragmentManager, FRAGMENT_MESSAGE_DIALOG)
    }

    override fun onMessageDialogDismissed() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO),
            REQUEST_RECORD_AUDIO_PERMISSION)
    }

    private val REQUEST_RECORD_AUDIO_PERMISSION = 5000

    private fun requestPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            return  // 既に付与されている。
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
            // permissionについての説明が必要な場合、ユーザに説明を表示する。
            showPermissionMessageDialog()
            return
        }

        // permissionをリクエストし、許可または拒否されるのを非同期に待つ。
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO_PERMISSION)
    }

    private fun isPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    // endregion

    companion object {
        private const val TAG = "MainActivity"
    }
}
