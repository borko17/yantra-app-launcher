package com.coderGtm.yantra.commands.theme

import android.app.WallpaperManager
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import androidx.core.graphics.drawable.toBitmap
import com.coderGtm.yantra.R
import com.coderGtm.yantra.Themes
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.setSystemWallpaper
import com.coderGtm.yantra.terminal.Terminal
import com.coderGtm.yantra.toast
import androidx.core.content.edit

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "theme",
        helpTitle = terminal.activity.getString(R.string.cmd_theme_title),
        description = terminal.activity.getString(R.string.cmd_theme_help)
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size < 2) {
            output(terminal.activity.getString(R.string.specify_theme_or_action), terminal.theme.errorTextColor)
            return
        }
        if (args.size > 2) {
            output(terminal.activity.getString(R.string.command_takes_one_param, metadata.name), terminal.theme.errorTextColor)
            return
        }

        val name = args[1].trim().lowercase()
        when (name) {
            "-save" -> {
                saveCurrentTheme(terminal)
                return
            }
            "-export" -> {
                exportTheme(terminal)
                return
            }
            "-import" -> {
                importTheme(terminal)
                return
            }
            "-remove" -> {
                removeTheme(terminal)
                return
            }
            else -> {
                val savedTheme = getSavedTheme(terminal.preferenceObject, name)
                if (Themes.entries.any { it.name.lowercase() == name } || name == "custom" || savedTheme != "") {
                    if (savedTheme != "") {
                        setSavedTheme(terminal.activity, name, savedTheme)
                        return
                    }

                    if (name == "custom") {
                        output(
                            terminal.activity.getString(R.string.launching_custom_theme_designer),
                            terminal.theme.resultTextColor,
                            Typeface.ITALIC
                        )
                        openCustomThemeDesigner(terminal)
                        return
                    }
                    val theme = Themes.entries.first { it.name.lowercase() == name }
                    terminal.preferenceObject.edit { putInt("theme", theme.ordinal) }
                    if (terminal.preferenceObject.getBoolean("defaultWallpaper", true)) {
                        val wallpaperManager =
                            WallpaperManager.getInstance(terminal.activity.applicationContext)
                        val colorDrawable = ColorDrawable(terminal.theme.bgColor)
                        setSystemWallpaper(
                            wallpaperManager,
                            colorDrawable.toBitmap(
                                terminal.activity.resources.displayMetrics.widthPixels,
                                terminal.activity.resources.displayMetrics.heightPixels
                            )
                        )
                    }
                    toast(
                        terminal.activity.baseContext,
                        terminal.activity.getString(R.string.setting_theme_to, theme.name)
                    )
                    terminal.activity.recreate()
                } else output(
                    terminal.activity.getString(R.string.theme_not_found, name),
                    terminal.theme.errorTextColor
                )
            }
        }
    }
}