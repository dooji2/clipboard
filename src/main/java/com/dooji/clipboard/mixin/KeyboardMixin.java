package com.dooji.clipboard.mixin;

import com.dooji.clipboard.ClipboardHelper;
import com.dooji.clipboard.manager.ClipboardConfig;
import com.dooji.clipboard.manager.ClipboardManager;

import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {

    @Inject(method = "getClipboard", at = @At("HEAD"), cancellable = true)
    private void onGetClipboard(CallbackInfoReturnable<String> cir) {
        ClipboardConfig config = ClipboardManager.getConfig();
        if (!config.enabled) return;

        String clipboardText = null;

        if ("custom".equals(config.prioritize)) {
            clipboardText = ClipboardManager.getLastEntry();
        } else {
            clipboardText = ClipboardHelper.getSystemClipboardText();
        }

        if (clipboardText != null && !clipboardText.isEmpty()) {
            cir.setReturnValue(clipboardText);
        }
    }

    @Inject(method = "setClipboard", at = @At("HEAD"), cancellable = true)
    private void onSetClipboard(String clipboard, CallbackInfo ci) {
        ClipboardConfig config = ClipboardManager.getConfig();
        
        if (!config.enabled) return;

        if (clipboard != null && !clipboard.isEmpty()) {
            ClipboardHelper.copyToSystemClipboard(clipboard);
            ClipboardManager.addEntry(clipboard);
        }

        ci.cancel();
    }
}