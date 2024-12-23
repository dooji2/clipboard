package com.dooji.clipboard;

import com.dooji.clipboard.manager.ClipboardConfig;
import com.dooji.clipboard.manager.ClipboardManager;
import com.dooji.clipboard.ui.ClipboardScreen;
import com.dooji.omnilib.OmnilibClient;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import org.lwjgl.glfw.GLFW;

public class Clipboard implements ModInitializer {
    public static final String MOD_ID = "clipboard";
    private static final Identifier LAMP_ON = Identifier.of("minecraft", "textures/block/redstone_lamp_on.png");

    private static KeyBinding openClipboardKey;

    @Override
    public void onInitialize() {
        ClipboardManager.initialize();
        ClipboardConfig config = ClipboardManager.getConfig();
        registerKeyBindings();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (config.firstTime && client.player != null) {
                String openKey = openClipboardKey.getBoundKeyLocalizedText().getString();
                createToast("clipboard.welcome.title", "clipboard.welcome.message", openKey, LAMP_ON);
                playClickSound(client);
                config.firstTime = false;
                config.save();
            }

            if (openClipboardKey.wasPressed()) {
                openClipboardScreen(client, config);
            }
        });
    }

    private void registerKeyBindings() {
        openClipboardKey = registerKey("key.clipboard.open", GLFW.GLFW_KEY_B);
    }

    private KeyBinding registerKey(String key, int keyCode) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(key, InputUtil.Type.KEYSYM, keyCode, "screen.clipboard.title"));
    }

    private void openClipboardScreen(MinecraftClient client, ClipboardConfig config) {
        client.setScreen(new ClipboardScreen(ClipboardManager.getHistory(), config));
    }

    private void createToast(String titleKey, String messageKey, String placeholder, Object icon) {
        Text title = Text.translatable(titleKey);
        Text description = Text.translatable(messageKey, placeholder);

        OmnilibClient.showToast(
                title,
                description,
                6000,
                0xFFFFFF,
                0xAAAAAA,
                null,
                icon instanceof Identifier ? (Identifier) icon : null,
                icon instanceof ItemStack ? (ItemStack) icon : null,
                16,
                170,
                32
        );
    }

    private void playClickSound(MinecraftClient client) {
        SoundManager soundManager = client.getSoundManager();
        soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }
}