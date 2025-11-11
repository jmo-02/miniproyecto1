package com.example.miniproyecto1.view.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.miniproyecto1.R
import com.example.miniproyecto1.repository.InventoryRepository
import com.example.miniproyecto1.utils.SessionManager
import com.example.miniproyecto1.view.MainActivity
import kotlinx.coroutines.runBlocking
import java.text.NumberFormat
import java.util.Locale

class InventoryWidgetProvider : AppWidgetProvider() {

    companion object {
        private const val ACTION_TOGGLE_VISIBILITY = "com.example.miniproyecto1.widget.ACTION_TOGGLE_VISIBILITY"
        private const val ACTION_MANAGE = "com.example.miniproyecto1.widget.ACTION_MANAGE"
        private const val PREFS = "inventory_widget_prefs"
        private const val KEY_VISIBLE = "key_visible"

        private val formatter: NumberFormat = NumberFormat.getNumberInstance(Locale("es", "CO")).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }

        private fun isVisible(context: Context): Boolean =
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_VISIBLE, false)

        private fun setVisible(context: Context, visible: Boolean) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_VISIBLE, visible)
                .apply()
        }

        private fun buildTogglePI(context: Context): PendingIntent {
            val intent = Intent(context, InventoryWidgetProvider::class.java).apply {
                action = ACTION_TOGGLE_VISIBILITY
            }
            return PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        private fun buildManagePI(context: Context): PendingIntent {
            val intent = Intent(context, InventoryWidgetProvider::class.java).apply {
                action = ACTION_MANAGE
            }
            return PendingIntent.getBroadcast(
                context,
                1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        private fun calculateTotalBlocking(context: Context): Double = runBlocking {
            val repo = InventoryRepository(context)
            val items = repo.getListInventory()
            val totalLong = items.sumOf { it.price.toLong() * it.quantity.toLong() }
            totalLong.toDouble()
        }

        private fun updateWidget(context: Context, manager: AppWidgetManager, widgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_inventory)
            val loggedIn = SessionManager(context).isLoggedIn()
            val visible = isVisible(context)

            if (!loggedIn) {
                // Usuario no autenticado: mostrar mensaje
                views.setTextViewText(R.id.inv_balance, context.getString(R.string.inv_login_hint))
                views.setImageViewResource(R.id.inv_toggle_eye, R.drawable.ic_eye_open)
                // Clics: el ojo y gestionar llevan a login
                views.setOnClickPendingIntent(R.id.inv_toggle_eye, buildManagePI(context))
                views.setOnClickPendingIntent(R.id.inv_manage_icon, buildManagePI(context))
                views.setOnClickPendingIntent(R.id.inv_manage_text, buildManagePI(context))
            } else {
                // Usuario autenticado: alterna visibilidad
                val balanceText = if (visible) {
                    "$ ${formatter.format(calculateTotalBlocking(context))}"
                } else {
                    "$ ****"
                }
                views.setTextViewText(R.id.inv_balance, balanceText)

                val eyeIcon = if (visible) R.drawable.ic_eye_closed else R.drawable.ic_eye_open
                views.setImageViewResource(R.id.inv_toggle_eye, eyeIcon)

                views.setOnClickPendingIntent(R.id.inv_toggle_eye, buildTogglePI(context))
                views.setOnClickPendingIntent(R.id.inv_manage_icon, buildManagePI(context))
                views.setOnClickPendingIntent(R.id.inv_manage_text, buildManagePI(context))
            }

            manager.updateAppWidget(widgetId, views)
        }

        private fun updateAll(context: Context) {
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(ComponentName(context, InventoryWidgetProvider::class.java))
            ids.forEach { updateWidget(context, mgr, it) }
        }

        // Llama a esto tras guardar/actualizar/eliminar inventario para refrescar el widget
        fun notifyInventoryChanged(context: Context) {
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(ComponentName(context, InventoryWidgetProvider::class.java))
            if (ids.isNotEmpty()) {
                val intent = Intent(context, InventoryWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
                context.sendBroadcast(intent)
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { Companion.updateWidget(context, appWidgetManager, it) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_TOGGLE_VISIBILITY -> {
                val loggedIn = SessionManager(context).isLoggedIn()
                if (loggedIn) {
                    setVisible(context, !isVisible(context))
                    updateAll(context)
                } else {
                    // Si no hay sesión, enviar al login
                    val launch = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        putExtra("navigateTo", "login")
                    }
                    context.startActivity(launch)
                }
            }
            ACTION_MANAGE -> {
                val launch = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra("navigateTo", "login")
                }
                context.startActivity(launch)
            }
        }
    }
}