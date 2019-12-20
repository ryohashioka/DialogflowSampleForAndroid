package jp.hashioka.ryo.dialogflowsample.dialogflow

import android.content.Context
import android.util.Log
import com.google.api.gax.rpc.BidiStream
import com.google.cloud.dialogflow.v2.*
import com.google.protobuf.ByteString

/**
 * Dialogflow の detectIntent を音声入力ストリームで行う
 */
class StreamingDetectIntent (
    context: Context
) : DetectIntent(context) {

    companion object {
        private const val TAG = "StreamingDetectIntent"
    }

    private var session = SessionName.of(PROJECT_ID, getSession()).toString()

    // region streaming

    // Build the query with the InputAudioConfig
    private var queryInput : QueryInput? = null
    // Create the Bidirectional stream
    private var bidiStream : BidiStream<StreamingDetectIntentRequest, StreamingDetectIntentResponse>? = null

    fun startStream(sampleRate: Int) {

        val inputAudioConfig = InputAudioConfig.newBuilder()
            .setAudioEncoding(AudioEncoding.AUDIO_ENCODING_LINEAR_16)
            .setLanguageCode(LANGUAGE_CODE)
            .setSampleRateHertz(sampleRate)
            .build()
        // Build the query with the InputAudioConfig
        queryInput = QueryInput.newBuilder().setAudioConfig(inputAudioConfig).build()

        // Create the Bidirectional stream
        bidiStream = sessionsClient.streamingDetectIntentCallable().call()

        // The first request must **only** contain the audio configuration:
        bidiStream?.send(
            StreamingDetectIntentRequest.newBuilder()
                .setSession(session)
                .setQueryInput(queryInput)
                .build()
        )
    }

    fun streaming(data: ByteArray, size: Int) {
        bidiStream?.send(
            StreamingDetectIntentRequest.newBuilder()
                .setInputAudio(ByteString.copyFrom(data, 0, size))
                .build()
        )
    }

    fun stopStream(callback:(text:String)->Unit) {
        if(bidiStream == null) return

        // Tell the service you are done sending data
        bidiStream?.closeSend()

        for (response in bidiStream!!) {
            val queryResult = response.queryResult
            Log.d(TAG, "====================")
            Log.d(TAG, "Intent Display Name: ${queryResult.intent.displayName}")
            Log.d(TAG, "Query Text: '${queryResult.queryText}'")
            Log.d(TAG, "Detected Intent: ${queryResult.intent.displayName} (confidence: ${queryResult.intentDetectionConfidence})")
            Log.d(TAG, "Fulfillment Text: '${queryResult.fulfillmentText}'")
            callback(queryResult.fulfillmentText)
        }

        bidiStream = null
        queryInput = null
    }

    // endregion
}