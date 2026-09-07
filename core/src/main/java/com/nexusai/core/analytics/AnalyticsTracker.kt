package com.nexusai.core.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsTracker @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val analytics: FirebaseAnalytics by lazy {
        FirebaseAnalytics.getInstance(context)
    }

    fun logEvent(name: String, params: Bundle? = null) {
        analytics.logEvent(name, params)
    }

    fun logScreenView(screenName: String) {
        val params = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_NAME, screenName)
        }
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, params)
    }

    fun logChatMessage(providerId: String, messageLength: Int) {
        val params = Bundle().apply {
            putString("provider_id", providerId)
            putInt("message_length", messageLength)
        }
        analytics.logEvent("chat_message_sent", params)
    }

    fun logProviderAdded(providerType: String, providerName: String) {
        val params = Bundle().apply {
            putString("provider_type", providerType)
            putString("provider_name", providerName)
        }
        analytics.logEvent("provider_added", params)
    }

    fun logChainExecuted(chainId: String, stepCount: Int, durationMs: Long) {
        val params = Bundle().apply {
            putString("chain_id", chainId)
            putInt("step_count", stepCount)
            putLong("duration_ms", durationMs)
        }
        analytics.logEvent("chain_executed", params)
    }

    fun logExport(format: String, messageCount: Int) {
        val params = Bundle().apply {
            putString("format", format)
            putInt("message_count", messageCount)
        }
        analytics.logEvent("export_completed", params)
    }

    fun logFeatureUsed(featureName: String) {
        val params = Bundle().apply {
            putString("feature_name", featureName)
        }
        analytics.logEvent("feature_used", params)
    }

    fun logError(message: String, throwable: Throwable? = null) {
        FirebaseCrashlytics.getInstance().log(message)
        if (throwable != null) {
            FirebaseCrashlytics.getInstance().recordException(throwable)
        }
    }

    fun setUserId(userId: String) {
        analytics.setUserId(userId)
        FirebaseCrashlytics.getInstance().setUserId(userId)
    }
}
