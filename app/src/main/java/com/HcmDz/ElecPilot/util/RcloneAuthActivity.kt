package com.HcmDz.ElecPilot.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.HcmDz.ElecPilot.R
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.CopyOnWriteArrayList

class RcloneAuthActivity : Activity() {

    companion object {
        const val EXTRA_REMOTE_TYPE = "remote_type"
        const val EXTRA_CONFIG_FILE = "config_file"
        const val EXTRA_RCLONE_BINARY = "rclone_binary"
        const val RESULT_AUTH_COMPLETE = 2
        private val _tokenChannel = Channel<String?>(Channel.CONFLATED)
        val tokenFlow: Flow<String?> = _tokenChannel.receiveAsFlow()
        @Volatile
        var tokenResult: String? = null
        @Volatile
        var emailResult: String? = null

        fun clearToken() {
            tokenResult = null
            _tokenChannel.trySend(null)
        }
    }

    private var webView: WebView? = null
    private var progressBar: ProgressBar? = null
    private var statusText: TextView? = null
    private val handler = Handler(Looper.getMainLooper())
    private var rcloneProcess: Process? = null
    private var authThread: Thread? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tokenResult = null
        emailResult = null
        _tokenChannel.trySend(null)

        val remoteType = intent.getStringExtra(EXTRA_REMOTE_TYPE) ?: "drive"
        val configFile = intent.getStringExtra(EXTRA_CONFIG_FILE) ?: ""
        val rcloneBinary = intent.getStringExtra(EXTRA_RCLONE_BINARY) ?: ""

        if (rcloneBinary.isEmpty()) {
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        statusText = TextView(this).apply {
            text = "Starting authentication..."
            setPadding(32, 24, 32, 24)
            textSize = 16f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#1A73E8"))
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        layout.addView(statusText!!)

        progressBar = ProgressBar(this).apply {
            isIndeterminate = true
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        layout.addView(progressBar!!)

        webView = WebView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0, 1f
            )
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = false
            settings.allowContentAccess = false
            settings.setMixedContentMode(android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW)
            settings.setSafeBrowsingEnabled(true)
            settings.userAgentString =
                "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val url = request?.url?.toString() ?: return true
                    val host = request.url?.host?.lowercase() ?: return true
                    val allowedHosts = listOf(
                        "127.0.0.1", "localhost",
                        "accounts.google.com", "oauth2.googleapis.com",
                        "login.microsoftonline.com", "login.live.com"
                    )
                    if (allowedHosts.any { host == it || host.endsWith(".$it") }) return false
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    } catch (_: Exception) {}
                    return true
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    progressBar!!.progress = newProgress
                    progressBar!!.visibility =
                        if (newProgress < 100) android.view.View.VISIBLE else android.view.View.GONE
                }
            }
        }
        layout.addView(webView!!)

        setContentView(layout)

        startAuthFlow(rcloneBinary, remoteType, configFile)
    }

    private fun startAuthFlow(rcloneBinary: String, remoteType: String, configFile: String) {
        authThread = Thread {
            try {
                val command = mutableListOf(rcloneBinary, "authorize", remoteType, "--auth-no-open-browser", "--config", configFile)
                val process = ProcessBuilder(command)
                    .directory(cacheDir)
                    .redirectErrorStream(false)
                    .apply {
                        environment()["TMPDIR"] = cacheDir.absolutePath
                        environment()["HOME"] = filesDir.parent ?: packageName
                    }
                    .start()

                rcloneProcess = process

                val stderrBuffer = CopyOnWriteArrayList<String>()
                val stderrThread = Thread {
                    try {
                        BufferedReader(InputStreamReader(process.errorStream)).use { reader ->
                            var line: String?
                            while (reader.readLine().also { line = it } != null) {
                                line?.let {
                                    stderrBuffer.add(it)
                                }
                            }
                        }
                    } catch (_: Exception) {}
                }
                stderrThread.isDaemon = true
                stderrThread.start()

                val stdoutBuffer = StringBuffer()
                val stdoutThread = Thread {
                    try {
                        BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                            var line: String?
                            while (reader.readLine().also { line = it } != null) {
                                line?.let {
                                    stdoutBuffer.appendLine(it)
                                }
                            }
                        }
                    } catch (_: Exception) {}
                }
                stdoutThread.isDaemon = true
                stdoutThread.start()

                var authUrl: String? = null
                val deadline = System.currentTimeMillis() + 15_000
                while (authUrl == null && System.currentTimeMillis() < deadline) {
                    authUrl = getAuthUrl(stderrBuffer)
                    if (authUrl == null) Thread.sleep(500)
                }
                if (authUrl == null) {
                    handler.post {
                        statusText!!.text = "Failed to start authentication"
                        setResult(RESULT_CANCELED)
                        finish()
                    }
                    return@Thread
                }

                handler.post {
                    statusText!!.text = "Authenticating with cloud provider..."
                    webView!!.loadUrl(authUrl)
                }

                val finished = process.waitFor(180, java.util.concurrent.TimeUnit.SECONDS)
                val rawToken = stdoutBuffer.toString().trim()
                val cleanToken = rawToken
                    .replace(Regex(".*--->\\s*"), "")
                    .replace(Regex("\\s*<---.*"), "")
                    .trim()

                if (cleanToken.isNotEmpty() && cleanToken.startsWith("{")) {
                    tokenResult = cleanToken
                    _tokenChannel.trySend(cleanToken)
                    emailResult = remoteType.replaceFirstChar { it.uppercase() }
                    handler.post {
                        statusText!!.text = "Authentication complete!"
                        setResult(RESULT_AUTH_COMPLETE)
                        finish()
                    }
                } else {
                    handler.post {
                        statusText!!.text = "Authentication failed"
                        setResult(RESULT_CANCELED)
                        finish()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ElecPilot", "Auth failed", e)
                handler.post {
                    statusText!!.text = getString(R.string.auth_error_generic)
                    setResult(RESULT_CANCELED)
                    finish()
                }
            }
        }.also { it.start() }
    }

    private fun getAuthUrl(stderrLines: List<String>): String? {
        val urlRegex = Regex("http://127\\.0\\.0\\.1:\\d+/auth\\S*")
        return stderrLines.firstNotNullOfOrNull { line ->
            urlRegex.find(line)?.value
        }
    }

    @Suppress("DEPRECATION", "GestureBackNavigation")
    override fun onBackPressed() {
        rcloneProcess?.destroy()
        setResult(RESULT_CANCELED)
        super.onBackPressed()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        authThread?.interrupt()
        rcloneProcess?.destroy()
        intent.getStringExtra(EXTRA_CONFIG_FILE)?.let { RcloneDriveService.deleteConfigTemp(it) }
        webView?.destroy()
        super.onDestroy()
    }
}
