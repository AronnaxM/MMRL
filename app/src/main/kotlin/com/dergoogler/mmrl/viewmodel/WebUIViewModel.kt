package com.dergoogler.mmrl.viewmodel

import android.app.Application
import android.webkit.WebView
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Density
import com.dergoogler.mmrl.Compat
import com.dergoogler.mmrl.Platform
import com.dergoogler.mmrl.app.Const
import com.dergoogler.mmrl.datastore.developerMode
import com.dergoogler.mmrl.repository.LocalRepository
import com.dergoogler.mmrl.repository.ModulesRepository
import com.dergoogler.mmrl.repository.UserPreferencesRepository
import com.dergoogler.mmrl.ui.activity.webui.interfaces.ksu.AdvancedKernelSUAPI
import com.dergoogler.mmrl.ui.activity.webui.interfaces.ksu.BaseKernelSUAPI
import com.dergoogler.mmrl.ui.activity.webui.interfaces.mmrl.FileInterface
import com.dergoogler.mmrl.ui.activity.webui.interfaces.mmrl.MMRLInterface
import com.topjohnwu.superuser.Shell
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.dergoogler.mmrl.compat.ext.isLocalWifiUrl
import dev.dergoogler.mmrl.compat.viewmodel.MMRLViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.io.File


@HiltViewModel(assistedFactory = WebUIViewModel.Factory::class)
class WebUIViewModel @AssistedInject constructor(
    @Assisted val modId: String,
    application: Application,
    localRepository: LocalRepository,
    modulesRepository: ModulesRepository,
    userPreferencesRepository: UserPreferencesRepository,
) : MMRLViewModel(
    application,
    localRepository,
    modulesRepository,
    userPreferencesRepository
) {
    val isProviderAlive get() = Compat.isAlive

    val versionName: String
        get() = Compat.get("") {
            with(moduleManager) { version }
        }

    val versionCode: Int
        get() = Compat.get(-1) {
            with(moduleManager) { versionCode }
        }

    val platform: Platform
        get() = Compat.get(Platform.EMPTY) {
            platform
        }

    private val moduleDir = "/data/adb/modules/$modId"
    val webRoot = File("$moduleDir/webroot")

    private val sanitizedModId: String
        get() {
            return modId.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        }

    private val userPrefs = runBlocking { userPreferencesRepository.data.first() }

    private val sanitizedModIdWithFile
        get(): String {
            return "$${
                when {
                    sanitizedModId.length >= 2 -> sanitizedModId[0].uppercase() + sanitizedModId[1]
                    sanitizedModId.isNotEmpty() -> sanitizedModId[0].uppercase()
                    else -> ""
                }
            }File"
        }

    fun isDomainSafe(domain: String): Boolean {
        val default = Const.WEBUI_DOMAIN_SAFE_REGEX.matches(domain)
        return userPrefs.developerMode({ useWebUiDevUrl }, default) {
            webUiDevUrl.isLocalWifiUrl()
        }
    }

    val domainUrl
        get(): String {
            val default = "https://mui.kernelsu.org/index.html"
            return userPrefs.developerMode({ useWebUiDevUrl }, default) {
                webUiDevUrl
            }
        }

    val rootShell
        get(): Shell {
            return Compat.createRootShell(
                globalMnt = true,
                devMode = userPrefs.developerMode
            )
        }

    val allowedFsApi = modId in userPrefs.allowedFsModules
    val allowedKsuApi = modId in userPrefs.allowedKsuModules

    var topInset by mutableStateOf<Int?>(null)
        private set
    var bottomInset by mutableStateOf<Int?>(null)
        private set

    fun initInsets(density: Density, insets: WindowInsets) {
        topInset = (insets.getTop(density) / density.density).toInt()
        bottomInset = (insets.getBottom(density) / density.density).toInt()
    }

    fun destroyJavascriptInterfaces(webView: WebView) {
        webView.removeJavascriptInterface("ksu")
        webView.removeJavascriptInterface("$$sanitizedModId")
        if (allowedFsApi) {
            webView.removeJavascriptInterface(sanitizedModIdWithFile)
        }
    }

    fun createJavascriptInterfaces(
        webView: WebView,
        isDarkMode: Boolean,
    ) = webView.apply {
        addJavascriptInterface(
            if (allowedKsuApi) {
                AdvancedKernelSUAPI(context, this, moduleDir, userPrefs)
            } else {
                BaseKernelSUAPI(context, this, moduleDir)
            }, "ksu"
        )

        addJavascriptInterface(
            MMRLInterface(
                viewModel = this@WebUIViewModel,
                context = context,
                isDark = isDarkMode,
                webview = this
            ), "$$sanitizedModId"
        )

        if (allowedFsApi) {
            addJavascriptInterface(
                FileInterface(this, context),
                sanitizedModIdWithFile
            )
        }
    }


    @AssistedFactory
    interface Factory {
        fun create(
            modId: String,
        ): WebUIViewModel
    }
}