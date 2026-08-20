package com.HcmDz.ElecPilot

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.HcmDz.ElecPilot.R
import com.HcmDz.ElecPilot.ui.screens.MotorFormDialog
import com.HcmDz.ElecPilot.ui.screens.MainScreen
import com.HcmDz.ElecPilot.ui.screens.StatisticsDialog
import com.HcmDz.ElecPilot.ui.screens.BackupBrowserDialog
import com.HcmDz.ElecPilot.ui.screens.BackupSettingsDialog
import com.HcmDz.ElecPilot.ui.screens.CloudBackupBrowserDialog
import com.HcmDz.ElecPilot.ui.screens.CloudBackupSettingsDialog
import com.HcmDz.ElecPilot.ui.screens.StatisticsData
import com.HcmDz.ElecPilot.ui.screens.computePlcStatistics
import com.HcmDz.ElecPilot.ui.screens.computeStatistics
import com.HcmDz.ElecPilot.ui.theme.ElecPilotTheme
import com.HcmDz.ElecPilot.ui.viewmodel.MotorViewModel
import com.HcmDz.ElecPilot.ui.viewmodel.PlcViewModel
import com.HcmDz.ElecPilot.ui.views.plc.PlcScreen
import com.HcmDz.ElecPilot.util.ExcelUtil
import com.HcmDz.ElecPilot.util.BackupScheduler
import com.HcmDz.ElecPilot.util.BackupManager
import com.HcmDz.ElecPilot.util.BackupResult
import com.HcmDz.ElecPilot.util.CloudBackupManager
import com.HcmDz.ElecPilot.util.CloudBackupResult
import com.HcmDz.ElecPilot.util.NotificationHelper
import com.HcmDz.ElecPilot.util.CloudBackupScheduler
import com.HcmDz.ElecPilot.util.RcloneDriveService
import com.HcmDz.ElecPilot.util.UpdateManager
import com.HcmDz.ElecPilot.util.resolveLanguage
import com.HcmDz.ElecPilot.data.getBackupPreferences
import com.HcmDz.ElecPilot.data.getCloudBackupPreferences
import com.HcmDz.ElecPilot.data.saveCloudBackupPreferences
import com.HcmDz.ElecPilot.data.SyncStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.concurrent.thread

class MainActivity : ComponentActivity() {

    private val viewModel: MotorViewModel by viewModels()
    private val plcViewModel: PlcViewModel by viewModels()
    private var currentModule = AppModule.DEPARTS
    private val _isRestoring = MutableStateFlow(false)
    private val _isCloudRestoring = MutableStateFlow(false)
    private val _restorePhase = MutableStateFlow("")
    @Volatile
    private var currentRestoreJob: Job? = null

    private enum class AppModule { DEPARTS, PLC }

    private val supportedMimeTypes = arrayOf(
        "text/csv",
        "text/comma-separated-values",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "text/plain"
    )

    override fun attachBaseContext(newBase: Context) {
        val storedLang = newBase.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getString("app_language", "system") ?: "system"
        val resolvedLang = resolveLanguage(storedLang)
        val locale = Locale.forLanguageTag(resolvedLang)
        Locale.setDefault(locale)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    private val importLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            lifecycleScope.launch {
                try {
                    val result = withContext(Dispatchers.IO) {
                        ExcelUtil.detectFileTypeWithFormat(this@MainActivity, it)
                    }
                    when (currentModule) {
                        AppModule.DEPARTS -> {
                            when (result.type) {
                                ExcelUtil.FileType.DEPARTS -> viewModel.requestImportWithPreview(it, result.isExcel)
                                ExcelUtil.FileType.PLC -> viewModel.showSnackbar(getString(R.string.import_hint_switch_to_plc))
                                ExcelUtil.FileType.UNKNOWN -> viewModel.showSnackbar(getString(R.string.import_error_unknown))
                            }
                        }
                        AppModule.PLC -> {
                            when (result.type) {
                                ExcelUtil.FileType.PLC -> plcViewModel.requestImportWithPreview(it, result.isExcel)
                                ExcelUtil.FileType.DEPARTS -> plcViewModel.showSnackbar(getString(R.string.import_hint_switch_to_departs))
                                ExcelUtil.FileType.UNKNOWN -> plcViewModel.showSnackbar(getString(R.string.import_error_unknown))
                            }
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ElecPilot", "Import failed", e)
                    viewModel.showSnackbar(getString(R.string.snackbar_import_error))
                }
            }
        }
    }

    private val importMultipleLauncher = registerForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) importAllFiles(uris)
    }

    private val exportLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        uri?.let {
            when (currentModule) {
                AppModule.DEPARTS -> exportCsv(it)
                AppModule.PLC -> exportPlcCsv(it)
            }
        }
    }

    private val exportExcelLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri: Uri? ->
        uri?.let {
            when (currentModule) {
                AppModule.DEPARTS -> exportExcel(it)
                AppModule.PLC -> exportPlcExcel(it)
            }
        }
    }

    // === Queue-based launchers for exporting both databases ===
    private val csvExportQueue = mutableListOf<Pair<String, (Uri) -> Unit>>()
    private val excelExportQueue = mutableListOf<Pair<String, (Uri) -> Unit>>()
    private var csvLauncherActive = false
    private var excelLauncherActive = false

    private val exportCsvQueueLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        csvLauncherActive = false
        if (uri != null && csvExportQueue.isNotEmpty()) {
            val (_, action) = csvExportQueue.removeAt(0)
            action(uri)
            processCsvQueue()
        } else {
            csvExportQueue.clear()
        }
    }

    private val exportExcelQueueLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri: Uri? ->
        excelLauncherActive = false
        if (uri != null && excelExportQueue.isNotEmpty()) {
            val (_, action) = excelExportQueue.removeAt(0)
            action(uri)
            processExcelQueue()
        } else {
            excelExportQueue.clear()
        }
    }

    private fun processCsvQueue() {
        if (csvExportQueue.isNotEmpty() && !csvLauncherActive) {
            csvLauncherActive = true
            exportCsvQueueLauncher.launch(csvExportQueue.first().first)
        }
    }

    private fun processExcelQueue() {
        if (excelExportQueue.isNotEmpty() && !excelLauncherActive) {
            excelLauncherActive = true
            exportExcelQueueLauncher.launch(excelExportQueue.first().first)
        }
    }

    private fun exportCsvBoth() {
        csvExportQueue.clear()
        val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val motorSnapshot = viewModel.getCurrentMotors()
        val plcSnapshot = plcViewModel.getCurrentPlcs()

        csvExportQueue.add(getString(R.string.export_filename_departs, dateStr) to { uri ->
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    contentResolver.openOutputStream(uri)?.use {
                        ExcelUtil.exportToCsvStream(it, motorSnapshot)
                    } ?: throw Exception("Cannot open output stream")
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    android.util.Log.e("ElecPilot", "CSV export failed", e)
                    runOnUiThread {
                        viewModel.showSnackbar(getString(R.string.snackbar_export_error))
                        plcViewModel.showSnackbar(getString(R.string.snackbar_export_error))
                    }
                }
            }
        })
        csvExportQueue.add(getString(R.string.export_filename_plc, dateStr) to { uri ->
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    contentResolver.openOutputStream(uri)?.use {
                        ExcelUtil.exportPlcToCsvStream(it, plcSnapshot)
                    } ?: throw Exception("Cannot open output stream")
                    runOnUiThread {
                        val msg = getString(R.string.export_multiple_csv_summary, motorSnapshot.size, plcSnapshot.size)
                        viewModel.showSnackbar(msg)
                        plcViewModel.showSnackbar(msg)
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    android.util.Log.e("ElecPilot", "PLC CSV export failed", e)
                    runOnUiThread {
                        viewModel.showSnackbar(getString(R.string.snackbar_export_error))
                        plcViewModel.showSnackbar(getString(R.string.snackbar_export_error))
                    }
                }
            }
        })
        processCsvQueue()
    }

    private fun exportExcelBoth() {
        excelExportQueue.clear()
        val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val motorSnapshot = viewModel.getCurrentMotors()
        val plcSnapshot = plcViewModel.getCurrentPlcs()

        excelExportQueue.add(getString(R.string.export_filename_departs_xlsx, dateStr) to { uri ->
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    contentResolver.openOutputStream(uri)?.use {
                        ExcelUtil.exportToExcelStream(it, motorSnapshot)
                    } ?: throw Exception("Cannot open output stream")
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    android.util.Log.e("ElecPilot", "Excel export failed", e)
                    runOnUiThread {
                        viewModel.showSnackbar(getString(R.string.snackbar_export_error))
                        plcViewModel.showSnackbar(getString(R.string.snackbar_export_error))
                    }
                }
            }
        })
        excelExportQueue.add(getString(R.string.export_filename_plc_xlsx, dateStr) to { uri ->
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    contentResolver.openOutputStream(uri)?.use {
                        ExcelUtil.exportPlcToExcelStream(it, plcSnapshot)
                    } ?: throw Exception("Cannot open output stream")
                    runOnUiThread {
                        val msg = getString(R.string.export_multiple_excel_summary, motorSnapshot.size, plcSnapshot.size)
                        viewModel.showSnackbar(msg)
                        plcViewModel.showSnackbar(msg)
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    android.util.Log.e("ElecPilot", "PLC Excel export failed", e)
                    runOnUiThread {
                        viewModel.showSnackbar(getString(R.string.snackbar_export_error))
                        plcViewModel.showSnackbar(getString(R.string.snackbar_export_error))
                    }
                }
            }
        })
        processExcelQueue()
    }

    private fun exportTemplateBoth() {
        excelExportQueue.clear()
        val storedLang = getSharedPreferences("settings", Context.MODE_PRIVATE).getString("app_language", "system") ?: "system"
        val lang = resolveLanguage(storedLang)
        excelExportQueue.add(getString(R.string.template_filename_departs, lang) to { uri ->
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    contentResolver.openOutputStream(uri)?.use {
                        ExcelUtil.exportTemplateMotorToExcelStream(it, lang)
                    } ?: throw Exception("Cannot open output stream")
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    android.util.Log.e("ElecPilot", "Motor template failed", e)
                    runOnUiThread {
                        viewModel.showSnackbar(getString(R.string.snackbar_export_error))
                    }
                }
            }
        })
        excelExportQueue.add(getString(R.string.template_filename_plc, lang) to { uri ->
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    contentResolver.openOutputStream(uri)?.use {
                        ExcelUtil.exportTemplatePlcToExcelStream(it, lang)
                    } ?: throw Exception("Cannot open output stream")
                    runOnUiThread {
                        viewModel.showSnackbar(getString(R.string.snackbar_template_success))
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    android.util.Log.e("ElecPilot", "PLC template failed", e)
                    runOnUiThread {
                        viewModel.showSnackbar(getString(R.string.snackbar_export_error))
                    }
                }
            }
        })
        processExcelQueue()
    }

    private fun exportFileName(): String {
        val df = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return when (currentModule) {
            AppModule.DEPARTS -> getString(R.string.export_filename_departs, df.format(Date()))
            AppModule.PLC -> getString(R.string.export_filename_plc, df.format(Date()))
        }
    }

    private fun exportFileNameXlsx(): String {
        val df = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return when (currentModule) {
            AppModule.DEPARTS -> getString(R.string.export_filename_departs_xlsx, df.format(Date()))
            AppModule.PLC -> getString(R.string.export_filename_plc_xlsx, df.format(Date()))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable("currentModule", currentModule)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RcloneDriveService.init(this)
        savedInstanceState?.let {
            currentModule = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getSerializable("currentModule", AppModule::class.java) ?: AppModule.DEPARTS
            } else {
                @Suppress("DEPRECATION")
                it.getSerializable("currentModule") as? AppModule ?: AppModule.DEPARTS
            }
        }
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            )
        }

        BackupScheduler.rescheduleIfNeeded(this)
        CloudBackupScheduler.rescheduleIfNeeded(this)

        setContent {
            val prefs = remember { applicationContext.getSharedPreferences("settings", Context.MODE_PRIVATE) }
            var themeMode by remember { mutableStateOf(prefs.getString("theme_mode", "system") ?: "system") }

            val drawerState = rememberDrawerState(DrawerValue.Closed)
            val scope = rememberCoroutineScope()

            ElecPilotTheme(themeMode = themeMode) {
                var module by remember { mutableStateOf(currentModule) }
                var showAddDialog by remember { mutableStateOf(false) }
                var showAboutDialog by remember { mutableStateOf(false) }
                var showStatisticsDialog by remember { mutableStateOf(false) }
                var showClearAllDialog by remember { mutableStateOf(false) }

                var showLangDialog by remember { mutableStateOf(false) }
                var showBackupDialog by remember { mutableStateOf(false) }
                var showBackupBrowser by remember { mutableStateOf(false) }

                var showCloudBackupDialog by remember { mutableStateOf(false) }
                var showCloudBackupBrowser by remember { mutableStateOf(false) }

                var showUpdateDialog by remember { mutableStateOf(false) }
                var updateInfo by remember { mutableStateOf<UpdateManager.UpdateInfo?>(null) }
                var updateDownloadProgress by remember { mutableStateOf(-1f) }
                var showUpdateSnackBar by remember { mutableStateOf("") }

                LaunchedEffect(Unit) {
                    if (UpdateManager.shouldAutoCheck(applicationContext)) {
                        when (val result = UpdateManager.checkForUpdate(BuildConfig.VERSION_NAME)) {
                            is UpdateManager.UpdateResult.Found -> {
                                updateInfo = result.info
                                showUpdateDialog = true
                                UpdateManager.recordCheck(applicationContext)
                            }
                            is UpdateManager.UpdateResult.UpToDate -> {
                                UpdateManager.recordCheck(applicationContext)
                            }
                            is UpdateManager.UpdateResult.Error -> { /* silent — no record */ }
                        }
                    }
                }

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    gesturesEnabled = true,
                    drawerContent = {
                        ModalDrawerSheet {
                            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    text = stringResource(R.string.drawer_title),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.drawer_subtitle),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(8.dp))
                                MenuItem(
                                    icon = Icons.Default.FileUpload,
                                    text = stringResource(R.string.menu_import),
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        importMultipleLauncher.launch(supportedMimeTypes)
                                    }
                                )
                                MenuItem(
                                    icon = Icons.Default.FileDownload,
                                    text = stringResource(R.string.menu_export_csv),
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        exportCsvBoth()
                                    }
                                )
                                MenuItem(
                                    icon = Icons.Default.FileDownload,
                                    text = stringResource(R.string.menu_export_excel),
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        exportExcelBoth()
                                    }
                                )
                                MenuItem(
                                    icon = Icons.Default.Description,
                                    text = stringResource(R.string.menu_download_template),
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        exportTemplateBoth()
                                    }
                                )
                                MenuItem(
                                    icon = Icons.Default.Assessment,
                                    text = stringResource(R.string.menu_statistics),
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        showStatisticsDialog = true
                                    }
                                )
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                MenuItem(
                                    icon = Icons.Default.Backup,
                                    text = stringResource(R.string.menu_backup),
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        showBackupDialog = true
                                    }
                                )
                                MenuItem(
                                    icon = Icons.Default.Cloud,
                                    text = stringResource(R.string.menu_cloud_backup),
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        showCloudBackupDialog = true
                                    }
                                )
                                MenuItem(
                                    icon = Icons.Default.Delete,
                                    text = stringResource(R.string.menu_clear_all),
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        showClearAllDialog = true
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(8.dp))
                                MenuItem(
                                    icon = Icons.Default.Language,
                                    text = stringResource(R.string.menu_language),
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        showLangDialog = true
                                    }
                                )
                                MenuItem(
                                    icon = Icons.Default.Info,
                                    text = stringResource(R.string.menu_about),
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        showAboutDialog = true
                                    }
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Column(modifier = Modifier.padding(start = 8.dp)) {
                                    Text(
                                        text = stringResource(R.string.dialog_about_version, BuildConfig.VERSION_NAME),
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Hcmdz/ElecPilot"))
                                            startActivity(intent)
                                        }
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_github),
                                            contentDescription = "GitHub",
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = stringResource(R.string.drawer_developed_by),
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://hcmdz.github.io/ElecPilot/privacy/"))
                                            startActivity(intent)
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Privacy Policy",
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = stringResource(R.string.drawer_privacy_policy),
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://hcmdz.github.io/ElecPilot/terms/"))
                                            startActivity(intent)
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Description,
                                            contentDescription = "Terms of Service",
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = stringResource(R.string.drawer_terms_of_service),
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            TextButton(onClick = { module = AppModule.DEPARTS; currentModule = AppModule.DEPARTS }) {
                                Text(
                                    text = stringResource(R.string.tab_departs),
                                    fontWeight = if (module == AppModule.DEPARTS) FontWeight.Bold else FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                            Text(
                                text = stringResource(R.string.tab_separator),
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                            )
                            TextButton(onClick = { module = AppModule.PLC; currentModule = AppModule.PLC }) {
                                Text(
                                    text = stringResource(R.string.tab_plc),
                                    fontWeight = if (module == AppModule.PLC) FontWeight.Bold else FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }

                        when (module) {
                            AppModule.DEPARTS -> {
                                MainScreen(
                                    viewModel = viewModel,
                                    themeMode = themeMode,
                                    onToggleDarkMode = {
                                        themeMode = when (themeMode) {
                                            "system" -> "dark"
                                            "dark" -> "light"
                                            "light" -> "materialYou"
                                            else -> "system"
                                        }
                                        prefs.edit().putString("theme_mode", themeMode).apply()
                                    },
                                    onMenuClick = { scope.launch {
                                        if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                    } },
                                    onAdd = { showAddDialog = true },
                                    onImport = { importLauncher.launch(supportedMimeTypes) },
                                    onExportCsv = { exportLauncher.launch(exportFileName()) },
                                    onExportExcel = { exportExcelLauncher.launch(exportFileNameXlsx()) }
                                )
                            }
                            AppModule.PLC -> {
                                PlcScreen(
                                    viewModel = plcViewModel,
                                    themeMode = themeMode,
                                    onToggleDarkMode = {
                                        themeMode = when (themeMode) {
                                            "system" -> "dark"
                                            "dark" -> "light"
                                            "light" -> "materialYou"
                                            else -> "system"
                                        }
                                        prefs.edit().putString("theme_mode", themeMode).apply()
                                    },
                                    onMenuClick = { scope.launch {
                                        if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                    } },
                                    onImport = { importLauncher.launch(supportedMimeTypes) },
                                    onExportCsv = { exportLauncher.launch(exportFileName()) },
                                    onExportExcel = { exportExcelLauncher.launch(exportFileNameXlsx()) }
                                )
                            }
                        }
                    }

                    if (showAddDialog) {
                        MotorFormDialog(
                            onDismiss = { showAddDialog = false },
                            onConfirm = { motor ->
                                viewModel.addMotor(motor)
                                showAddDialog = false
                                viewModel.showSnackbar(getString(R.string.snackbar_depart_added))
                            }
                        )
                    }
                    if (showAboutDialog) {
                        AboutDialog(
                            onDismiss = { showAboutDialog = false },
                            onCheckUpdate = {
                                showAboutDialog = false
                                scope.launch {
                                    when (val result = UpdateManager.checkForUpdate(BuildConfig.VERSION_NAME)) {
                                        is UpdateManager.UpdateResult.Found -> {
                                            updateInfo = result.info
                                            showUpdateDialog = true
                                            UpdateManager.recordCheck(applicationContext)
                                        }
                                        is UpdateManager.UpdateResult.UpToDate -> {
                                            showUpdateSnackBar = getString(R.string.update_up_to_date)
                                            UpdateManager.recordCheck(applicationContext)
                                        }
                                        is UpdateManager.UpdateResult.Error -> {
                                            showUpdateSnackBar = getString(R.string.update_check_failed)
                                        }
                                    }
                                }
                            }
                        )
                    }
                    if (showUpdateDialog && updateInfo != null) {
                        UpdateDialog(
                            info = updateInfo!!,
                            downloadProgress = updateDownloadProgress,
                            onDownload = {
                                updateDownloadProgress = 0f
                                val job = scope.launch {
                                    val file = UpdateManager.downloadApk(
                                        applicationContext,
                                        updateInfo!!.downloadUrl,
                                        updateInfo!!.fileName
                                    ) { progress ->
                                        updateDownloadProgress = progress
                                    }
                                    if (file != null) {
                                        UpdateManager.installApk(applicationContext, file)
                                    } else {
                                        showUpdateSnackBar = getString(R.string.update_download_failed)
                                    }
                                    showUpdateDialog = false
                                    updateDownloadProgress = -1f
                                    UpdateManager.downloadJob = null
                                }
                                UpdateManager.downloadJob = job
                            },
                            onDismiss = {
                                UpdateManager.cancelDownload()
                                showUpdateDialog = false
                                updateDownloadProgress = -1f
                            }
                        )
                    }
                    if (showLangDialog) {
                        val currentLang = prefs.getString("app_language", "system") ?: "system"
                        LanguageDialog(
                            currentLang = currentLang,
                            onLanguageSelected = { lang ->
                                prefs.edit().putString("app_language", lang).apply()
                                showLangDialog = false
                                recreate()
                            },
                            onDismiss = { showLangDialog = false }
                        )
                    }
                    LaunchedEffect(showUpdateSnackBar) {
                        if (showUpdateSnackBar.isNotEmpty()) {
                            android.widget.Toast.makeText(
                                applicationContext,
                                showUpdateSnackBar,
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                            showUpdateSnackBar = ""
                        }
                    }
                    if (showStatisticsDialog) {
                        val motors by viewModel.motors.collectAsState()
                        val plcResults by plcViewModel.plcResults.collectAsState()
                        val naLabel = getString(R.string.statistics_na)
                        var statsResult by remember { mutableStateOf<StatisticsData?>(null as StatisticsData?) }
                        LaunchedEffect(motors, plcResults, module) {
                            statsResult = withContext(Dispatchers.Default) {
                                if (module == AppModule.DEPARTS) {
                                    computeStatistics(motors.map { it.item }, naLabel)
                                } else {
                                    computePlcStatistics(plcResults.map { it.item }, naLabel)
                                }
                            }
                        }
                        statsResult?.let { stats ->
                            val totalLabel = if (module == AppModule.DEPARTS)
                                getString(R.string.tab_departs) else getString(R.string.statistics_plc)
                            val sectionLabels = if (module == AppModule.DEPARTS) {
                                listOf(
                                    getString(R.string.statistics_by_atelier),
                                    getString(R.string.statistics_by_type),
                                    getString(R.string.statistics_by_type_depart),
                                    getString(R.string.statistics_by_tgbt)
                                )
                            } else {
                                listOf(
                                    getString(R.string.statistics_by_atelier),
                                    getString(R.string.statistics_by_dp),
                                    getString(R.string.statistics_by_carte),
                                    getString(R.string.statistics_by_position)
                                )
                            }
                            StatisticsDialog(
                                stats = stats,
                                totalLabel = totalLabel,
                                sectionLabels = sectionLabels,
                                onDismiss = { showStatisticsDialog = false }
                            )
                        }
                    }

                    val isLoadingMotor by viewModel.isLoading.collectAsState()
                    val isLoadingPlc by plcViewModel.isLoading.collectAsState()
                    val isLoading = isLoadingMotor || isLoadingPlc
                    if (isLoading) {
                        AlertDialog(
                            onDismissRequest = {},
                            title = { Text(stringResource(R.string.snackbar_operation_pending)) },
                            text = {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            },
                            confirmButton = {}
                        )
                    }

                    if (showClearAllDialog) {
                        AlertDialog(
                            onDismissRequest = { showClearAllDialog = false },
                            title = { Text(stringResource(R.string.dialog_confirm_title), fontWeight = FontWeight.Bold) },
                            text = { Text(stringResource(R.string.dialog_clear_all_confirm)) },
                            confirmButton = {
                                TextButton(onClick = {
                                    showClearAllDialog = false
                                    lifecycleScope.launch {
                                        try {
                                            val motorCount = viewModel.count()
                                            val plcCount = plcViewModel.count()
                                            if (motorCount == 0 && plcCount == 0) {
                                                viewModel.showSnackbar(getString(R.string.snackbar_no_data_to_delete))
                                            } else {
                                                coroutineScope {
                                                    launch { viewModel.deleteAllMotorsNow() }
                                                    launch { plcViewModel.deleteAllPlcNow() }
                                                }
                                                viewModel.showSnackbar(getString(R.string.snackbar_all_data_deleted))
                                            }
                                        } catch (e: Exception) {
                                            android.util.Log.e("ElecPilot", "Clear all failed", e)
                                            viewModel.showSnackbar(getString(R.string.snackbar_export_error))
                                        }
                                    }
                                }) { Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error) }
                            },
                            dismissButton = {
                                TextButton(onClick = { showClearAllDialog = false }) {
                                    Text(stringResource(R.string.close))
                                }
                            }
                        )
                    }

                    if (showBackupDialog) {
                        BackupSettingsDialog(
                            onDismiss = { showBackupDialog = false },
                            onBackupNow = {
                                showBackupDialog = false
                                lifecycleScope.launch {
                                    val motors = viewModel.getCurrentMotors()
                                    val plcs = plcViewModel.getCurrentPlcs()
                                    val result = BackupManager.performBackup(
                                        applicationContext, motors, plcs
                                    )
                                    when (result) {
                                        is BackupResult.Success -> {
                                            viewModel.showSnackbar(
                                                getString(R.string.backup_success, result.motorCount, result.plcCount)
                                            )
                                        }
                                        is BackupResult.Error -> {
                                            viewModel.showSnackbar(
                                                getString(R.string.backup_error, result.message)
                                            )
                                        }
                                    }
                                }
                            },
                            onBrowse = {
                                showBackupDialog = false
                                showBackupBrowser = true
                            }
                        )
                    }

                    if (showBackupBrowser) {
                        BackupBrowserDialog(
                            onDismiss = {
                                showBackupBrowser = false
                                showBackupDialog = true
                            },
                            isRestoring = _isRestoring.collectAsState().value,
                            onRestore = { fileInfo ->
                                lifecycleScope.launch {
                                    _isRestoring.value = true
                                    viewModel.setLoading(true)
                                    plcViewModel.setLoading(true)
                                    try {
                                        val uri = withContext(Dispatchers.IO) {
                                            BackupManager.getBackupFileUri(this@MainActivity, fileInfo)
                                        }
                                        if (uri != null) {
                                            var motorsAdded = 0
                                            var plcAdded = 0
                                            var unknownType = false
                                            withContext(Dispatchers.IO) {
                                                val result = ExcelUtil.detectFileTypeWithFormat(this@MainActivity, uri)
                                                when (result.type) {
                                                    ExcelUtil.FileType.DEPARTS -> {
                                                        val motors = ExcelUtil.importMotorsFromUri(this@MainActivity, uri, result.isExcel)
                                                        if (motors.isNotEmpty()) {
                                                            motorsAdded = viewModel.importMotorsDirect(motors)
                                                        }
                                                    }
                                                    ExcelUtil.FileType.PLC -> {
                                                        val plcs = ExcelUtil.importPlcsFromUri(this@MainActivity, uri, result.isExcel)
                                                        if (plcs.isNotEmpty()) {
                                                            plcAdded = plcViewModel.importPlcDirect(plcs)
                                                        }
                                                    }
                                                    ExcelUtil.FileType.UNKNOWN -> {
                                                        unknownType = true
                                                    }
                                                }
                                            }
                                            if (unknownType) {
                                                viewModel.showSnackbar(getString(R.string.import_error_unknown))
                                            } else {
                                                val msg = getString(R.string.backup_restore_success, motorsAdded, plcAdded)
                                                viewModel.showSnackbar(msg)
                                                plcViewModel.showSnackbar(msg)
                                            }
                                        } else {
                                            viewModel.showSnackbar(getString(R.string.backup_restore_error))
                                        }
                                    } catch (e: CancellationException) {
                                        throw e
                                    } catch (e: Exception) {
                                        viewModel.showSnackbar(getString(R.string.backup_restore_error))
                                    } finally {
                                        _isRestoring.value = false
                                        viewModel.setLoading(false)
                                        plcViewModel.setLoading(false)
                                    }
                                }
                            }
                        )
                    }

                    if (showCloudBackupDialog) {
                        CloudBackupSettingsDialog(
                            onDismiss = { showCloudBackupDialog = false },
                            onBackupNow = {
                                showCloudBackupDialog = false
                                lifecycleScope.launch {
                                    val motors = viewModel.getCurrentMotors()
                                    val plcs = plcViewModel.getCurrentPlcs()
                                    val cloudPrefs = applicationContext.getCloudBackupPreferences()
                                    val result = CloudBackupManager.performCloudBackup(
                                        applicationContext, motors, plcs,
                                        cloudPrefs.cloudFormat, cloudPrefs.cloudFolderName
                                    )
                                    when (result) {
                                        is CloudBackupResult.Success -> {
                                            applicationContext.saveCloudBackupPreferences(
                                                cloudPrefs.copy(
                                                    lastCloudBackupTime = System.currentTimeMillis(),
                                                    lastCloudBackupFiles = result.fileCount,
                                                    lastSyncStatus = SyncStatus.SUCCESS
                                                )
                                            )
                                            viewModel.showSnackbar(
                                                getString(R.string.cloud_backup_success, result.motorCount, result.plcCount)
                                            )
                                            NotificationHelper.showBackupSuccess(applicationContext, result.motorCount, result.plcCount)
                                        }
                                        is CloudBackupResult.Error -> {
                                            val updatedPrefs = cloudPrefs.copy(
                                                lastCloudBackupTime = System.currentTimeMillis(),
                                                lastSyncStatus = SyncStatus.FAILED
                                            )
                                            applicationContext.saveCloudBackupPreferences(updatedPrefs)
                                             viewModel.showSnackbar(
                                                getString(R.string.cloud_backup_error)
                                            )
                                            NotificationHelper.showBackupError(applicationContext)
                                        }
                                    }
                                }
                            },
                            onBrowse = {
                                showCloudBackupDialog = false
                                showCloudBackupBrowser = true
                            }
                        )
                    }

                    if (showCloudBackupBrowser) {
                        CloudBackupBrowserDialog(
                            onDismiss = {
                                showCloudBackupBrowser = false
                                showCloudBackupDialog = true
                            },
                            isRestoring = _isCloudRestoring.collectAsState().value,
                            restorePhase = _restorePhase.collectAsState().value,
                            onRestore = { fileInfo ->
                                currentRestoreJob?.cancel()
                                currentRestoreJob = lifecycleScope.launch {
                                    val thisJob = this
                                    _restorePhase.value = "Downloading..."
                                    _isCloudRestoring.value = true
                                    viewModel.setLoading(true)
                                    plcViewModel.setLoading(true)
                                    try {
                                        val cloudPrefs = applicationContext.getCloudBackupPreferences()
                                        val result = CloudBackupManager.performCloudRestore(
                                            applicationContext, fileInfo.id, fileInfo.name,
                                            onPhase = { phase -> _restorePhase.value = phase },
                                            onProgress = { p ->
                                                _restorePhase.value = "Downloading... ${p.percent.toInt()}% - ${p.speedBytesPerSec / 1024} KB/s"
                                            }
                                        )
                                        when (result) {
                                            is com.HcmDz.ElecPilot.util.CloudRestoreResult.Success -> {
                                                applicationContext.saveCloudBackupPreferences(
                                                    cloudPrefs.copy(
                                                        lastCloudBackupTime = System.currentTimeMillis(),
                                                        lastSyncStatus = SyncStatus.SUCCESS
                                                    )
                                                )
                                                val msg = getString(R.string.backup_restore_success, result.motorsAdded, result.plcAdded)
                                                viewModel.showSnackbar(msg)
                                                plcViewModel.showSnackbar(msg)
                                                viewModel.setSearchQuery("")
                                                plcViewModel.setSearchQuery("")
                                            }
                                            is com.HcmDz.ElecPilot.util.CloudRestoreResult.Error -> {
                                                val updatedPrefs = cloudPrefs.copy(
                                                    lastSyncStatus = SyncStatus.FAILED
                                                )
                                                applicationContext.saveCloudBackupPreferences(updatedPrefs)
                                                viewModel.showSnackbar(getString(R.string.backup_restore_error))
                                            }
                                        }
                                    } catch (e: CancellationException) {
                                        viewModel.showSnackbar(getString(R.string.cloud_operation_error_generic))
                                        throw e
                                    } catch (e: Exception) {
                                        android.util.Log.e("CloudRestore", "Unexpected error", e)
                                        viewModel.showSnackbar(getString(R.string.backup_restore_error))
                                    } finally {
                                        _isCloudRestoring.value = false
                                        _restorePhase.value = ""
                                        viewModel.setLoading(false)
                                        plcViewModel.setLoading(false)
                                        if (currentRestoreJob === thisJob) currentRestoreJob = null
                                    }
                                }
                            },
                            onCancelRestore = {
                                currentRestoreJob?.cancel()
                            }
                        )
                    }
                }
            }
        }

        lifecycleScope.launch {
            merge(viewModel.allMotors, plcViewModel.allPlc)
                .debounce(60_000)
                .distinctUntilChangedBy { list ->
                    list.size * 31 + list.sumOf { it.hashCode() }
                }
                .collect {
                    if (_isRestoring.value || _isCloudRestoring.value) return@collect
                    val currentPrefs = applicationContext.getBackupPreferences()
                    if (currentPrefs.enabled && currentPrefs.frequencyHours == 0) {
                        BackupScheduler.triggerOneTime(applicationContext)
                    }
                }
        }
    }

    private fun importAllFiles(uris: List<Uri>) {
        lifecycleScope.launch {
            viewModel.setLoading(true)
            plcViewModel.setLoading(true)
            val stats = ImportStats()
            try {
                withContext(Dispatchers.IO) {
                    for (uri in uris) {
                        try {
                            val result = ExcelUtil.detectFileTypeWithFormat(this@MainActivity, uri)
                            when (result.type) {
                                ExcelUtil.FileType.DEPARTS -> {
                                    val motors = ExcelUtil.importMotorsFromUri(this@MainActivity, uri, result.isExcel)
                                    if (motors.isNotEmpty()) {
                                        stats.motorsAdded += viewModel.importMotorsDirect(motors)
                                    }
                                }
                                ExcelUtil.FileType.PLC -> {
                                    val plcs = ExcelUtil.importPlcsFromUri(this@MainActivity, uri, result.isExcel)
                                    if (plcs.isNotEmpty()) {
                                        stats.plcAdded += plcViewModel.importPlcDirect(plcs)
                                    }
                                }
                                ExcelUtil.FileType.UNKNOWN -> {
                                    stats.wrongTypeCount++
                                }
                            }
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            android.util.Log.e("ElecPilot", "Error importing file: ${uri.lastPathSegment}", e)
                            stats.errorsCount++
                        }
                    }
                }
            } finally {
                viewModel.setLoading(false)
                plcViewModel.setLoading(false)
            }
            val message = buildImportSummaryMessage(stats)
            if (message.isNotBlank()) {
                viewModel.showSnackbar(message)
                plcViewModel.showSnackbar(message)
            }
        }
    }

    private data class ImportStats(
        var motorsAdded: Int = 0,
        var plcAdded: Int = 0,
        var wrongTypeCount: Int = 0,
        var errorsCount: Int = 0
    )

    private fun buildImportSummaryMessage(stats: ImportStats): String {
        val sb = StringBuilder()
        when {
            stats.motorsAdded > 0 || stats.plcAdded > 0 -> {
                sb.append(getString(R.string.import_summary_combined, stats.motorsAdded, stats.plcAdded))
            }
            stats.wrongTypeCount > 0 -> {
                sb.append(getString(R.string.import_no_data))
                sb.append("\n")
                sb.append(getString(R.string.import_wrong_type_detail, stats.wrongTypeCount))
                return sb.toString()
            }
            stats.errorsCount > 0 -> {
                sb.append(getString(R.string.import_errors_detail, stats.errorsCount))
                return sb.toString()
            }
            else -> return ""
        }
        if (stats.wrongTypeCount > 0) {
            sb.append("\n")
            sb.append(getString(R.string.import_wrong_type_detail, stats.wrongTypeCount))
        }
        if (stats.errorsCount > 0) {
            sb.append("\n")
            sb.append(getString(R.string.import_errors_detail, stats.errorsCount))
        }
        return sb.toString()
    }

    private fun exportCsv(uri: Uri) {
        thread(name = "export-csv") {
            try {
                val motors = viewModel.getCurrentMotors()
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    ExcelUtil.exportToCsvStream(outputStream, motors)
                } ?: throw Exception("Cannot open output stream")
                runOnUiThread {
                    viewModel.showSnackbar(getString(R.string.snackbar_export_success_departs, motors.size))
                }
            } catch (e: Exception) {
                android.util.Log.e("ElecPilot", "CSV export failed", e)
                runOnUiThread {
                    viewModel.showSnackbar(getString(R.string.snackbar_export_error))
                }
            }
        }
    }

    private fun exportPlcCsv(uri: Uri) {
        thread(name = "export-plc-csv") {
            try {
                val plcList = plcViewModel.getCurrentPlcs()
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    ExcelUtil.exportPlcToCsvStream(outputStream, plcList)
                } ?: throw Exception("Cannot open output stream")
                runOnUiThread {
                    plcViewModel.showSnackbar(getString(R.string.snackbar_export_success_plc, plcList.size))
                }
            } catch (e: Exception) {
                android.util.Log.e("ElecPilot", "PLC CSV export failed", e)
                runOnUiThread {
                    plcViewModel.showSnackbar(getString(R.string.snackbar_export_error))
                }
            }
        }
    }

    private fun exportExcel(uri: Uri) {
        thread(name = "export-excel") {
            try {
                val motors = viewModel.getCurrentMotors()
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    ExcelUtil.exportToExcelStream(outputStream, motors)
                } ?: throw Exception("Cannot open output stream")
                runOnUiThread {
                    viewModel.showSnackbar(getString(R.string.snackbar_export_success_departs, motors.size))
                }
            } catch (e: Exception) {
                android.util.Log.e("ElecPilot", "Excel export failed", e)
                runOnUiThread {
                    viewModel.showSnackbar(getString(R.string.snackbar_export_error))
                }
            }
        }
    }

    private fun exportPlcExcel(uri: Uri) {
        thread(name = "export-plc-excel") {
            try {
                val plcList = plcViewModel.getCurrentPlcs()
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    ExcelUtil.exportPlcToExcelStream(outputStream, plcList)
                } ?: throw Exception("Cannot open output stream")
                runOnUiThread {
                    plcViewModel.showSnackbar(getString(R.string.snackbar_export_success_plc, plcList.size))
                }
            } catch (e: Exception) {
                android.util.Log.e("ElecPilot", "PLC Excel export failed", e)
                runOnUiThread {
                    plcViewModel.showSnackbar(getString(R.string.snackbar_export_error))
                }
            }
        }
    }
}

@Composable
private fun AboutDialog(onDismiss: () -> Unit, onCheckUpdate: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.dialog_about_title),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(R.string.dialog_about_body),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.dialog_about_developer),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onCheckUpdate) {
                    Icon(
                        Icons.Default.SystemUpdate,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.update_check_updates))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

@Composable
private fun UpdateDialog(
    info: UpdateManager.UpdateInfo,
    downloadProgress: Float,
    onDownload: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (downloadProgress < 0f) onDismiss() },
        title = {
            Text(
                text = stringResource(R.string.update_available_title),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.update_available_body, info.versionName, info.releaseNotes),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (downloadProgress >= 0f) {
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = { downloadProgress },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.update_downloading, (downloadProgress * 100).toInt()),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            if (downloadProgress < 0f) {
                TextButton(onClick = onDownload) {
                    Text(stringResource(R.string.update_download))
                }
            }
        },
        dismissButton = {
            if (downloadProgress < 0f) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.update_later))
                }
            }
        }
    )
}

@Composable
private fun LanguageDialog(
    currentLang: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val languages = listOf(
        "system" to stringResource(R.string.language_system),
        "en" to stringResource(R.string.language_english),
        "fr" to stringResource(R.string.language_french),
        "ar" to stringResource(R.string.language_arabic)
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_language_title)) },
        text = {
            Column {
                languages.forEach { (code, label) ->
                    TextButton(
                        onClick = { onLanguageSelected(code) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = label,
                            fontWeight = if (code == currentLang) FontWeight.Bold else FontWeight.Normal,
                            color = if (code == currentLang)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

@Composable
private fun MenuItem(icon: ImageVector, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = text, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, fontSize = 14.sp)
    }
}
