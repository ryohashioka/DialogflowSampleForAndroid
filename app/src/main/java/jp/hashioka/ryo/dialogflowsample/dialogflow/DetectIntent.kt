package jp.hashioka.ryo.dialogflowsample.dialogflow

import android.content.Context
import android.util.Log
import com.google.api.gax.core.FixedCredentialsProvider
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.dialogflow.v2.*
import jp.hashioka.ryo.dialogflowsample.R

/**
 * Dialogflow の detectIntent に関するクラス
 */
open class DetectIntent (
    context: Context
) {

    companion object {
        private const val TAG = "DetectIntent"
        const val PROJECT_ID = "voice-recognition-trial-261200"
        const val LANGUAGE_CODE = "ja" // TODO: Dialogflow の言語コードはグローバル対応するときに設定ファイルで管理
        val SCOPE = listOf("https://www.googleapis.com/auth/cloud-platform")

        /**
         * セッションを取得する。
         * TODO : Dialogflow のセッションはクライアント毎にユニークとなるよう処理を記述する。
         */
        fun getSession() : String {
            return "hogehoge"
        }
    }

    protected val sessionsClient : SessionsClient
    private val contextClient : ContextsClient

    init {
        // 認証情報セット
        val credentials = GoogleCredentials
            .fromStream(context.resources.openRawResource(R.raw.credentials))
            .createScoped(SCOPE)
        sessionsClient = createSessions(credentials)
        contextClient = createContexts(credentials)
    }

    /**
     * SessionClient を作成する。
     */
    private fun createSessions(credentials: GoogleCredentials): SessionsClient {
        val sessionsSetting =
            SessionsSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                .build()
        return SessionsClient.create(sessionsSetting)
    }

    /**
     * ContextsClient を作成する。
     */
    private fun createContexts(credentials: GoogleCredentials) : ContextsClient {
        val contextsSettings =
            ContextsSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                .build()
        return ContextsClient.create(contextsSettings)
    }

    /**
     * detectIntent を実行し、その結果を返却
     * 指定されたテキストを送信するだけ。
     */
    fun send(text: String) : String {
        val request = DetectIntentRequest.newBuilder()
            .setQueryInput(
                QueryInput.newBuilder()
                    .setText(
                        TextInput.newBuilder()
                            .setText(text)
                            .setLanguageCode(LANGUAGE_CODE)
                    )
                    .build())
            .setSession(SessionName.format(PROJECT_ID, getSession()))
            .build()

        val res = sessionsClient.detectIntent(request)
        Log.d(TAG, "response result : ${res.queryResult}")
        return res.queryResult.fulfillmentText
    }

    /**
     * detectIntent を実行し、その結果を返却
     * context 指定可能
     */
    fun send(text: String, contexts: List<String>) : String {
        val queryParametersBuilder = QueryParameters.newBuilder()
        contexts.forEach {
            queryParametersBuilder
                .addContexts(
                    com.google.cloud.dialogflow.v2.Context.newBuilder()
                        .setName(ContextName.format(PROJECT_ID, getSession(), it))
                        .setLifespanCount(5) // TODO: context の Lifespan を動的にする。
                        .build()
                )
        }

        // Dialogflow に投げるテキスト、コンテキストなどセット
        val request = DetectIntentRequest.newBuilder()
            .setQueryParams(queryParametersBuilder.build())
            .setQueryInput(
                QueryInput.newBuilder()
                    .setText(
                        TextInput.newBuilder()
                            .setText(text)
                            .setLanguageCode(LANGUAGE_CODE)
                    )
                    .build())
            .setSession(SessionName.format(PROJECT_ID, getSession()))
            .build()

        val res = sessionsClient.detectIntent(request)
        Log.d(TAG, "response result : ${res.queryResult}")
        return res.queryResult.fulfillmentText
    }

    /**
     * context をリセットする。
     */
    fun resetContexts() {
        contextClient.deleteAllContexts(SessionName.format(PROJECT_ID, getSession()))
    }
}