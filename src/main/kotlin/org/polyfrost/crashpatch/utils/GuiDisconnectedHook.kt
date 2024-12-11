package org.polyfrost.crashpatch.utils

import org.polyfrost.crashpatch.crashes.CrashHelper.scanReport
import org.polyfrost.crashpatch.mixin.AccessorGuiDisconnected
import net.minecraft.client.gui.GuiDisconnected
import net.minecraft.client.gui.GuiScreen
import org.polyfrost.crashpatch.CrashPatchConfig
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.polyfrost.crashpatch.gui.CrashUI
import org.polyfrost.utils.v1.dsl.mc

object GuiDisconnectedHook {

    @JvmStatic
    fun onGUIDisplay(screen: GuiScreen?, ci: CallbackInfo) {
        if (screen is GuiDisconnected && CrashPatchConfig.disconnectCrashPatch) {
            val gui = screen as AccessorGuiDisconnected
            val scan = scanReport(gui.message.formattedText, true)
            if (scan != null && scan.solutions.size > 1) {
                ci.cancel()
                mc.displayGuiScreen(CrashUI(gui.message.formattedText, null, gui.reason, CrashUI.GuiType.DISCONNECT).create())
            }
        }
    }

}