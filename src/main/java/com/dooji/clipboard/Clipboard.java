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
    private static final Identifier LAMP_OFF = Identifier.of("minecraft", "textures/block/redstone_lamp.png");

    private static KeyBinding openClipboardKey;
    private static KeyBinding togglePersistentKey;
    private static KeyBinding togglePrioritizationKey;
    private static KeyBinding toggleEnabledKey;

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

            while (togglePersistentKey.wasPressed()) {
                togglePersistent(client, config);
            }

            while (togglePrioritizationKey.wasPressed()) {
                togglePrioritization(client, config);
            }

            while (toggleEnabledKey.wasPressed()) {
                toggleEnabled(client, config);
            }
        });
    }

    // you'll have to deal with this mess until I make a gui config thingy in omnilib
    private void registerKeyBindings() {
        openClipboardKey = registerKey("key.clipboard.open", GLFW.GLFW_KEY_B);
        togglePersistentKey = registerKey("key.clipboard.toggle_persistent", GLFW.GLFW_KEY_O);
        togglePrioritizationKey = registerKey("key.clipboard.toggle_prioritization", GLFW.GLFW_KEY_Z);
        toggleEnabledKey = registerKey("key.clipboard.toggle_enabled", GLFW.GLFW_KEY_I);
    }

    private KeyBinding registerKey(String key, int keyCode) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(key, InputUtil.Type.KEYSYM, keyCode, "screen.clipboard.title"));
    }

    private void togglePersistent(MinecraftClient client, ClipboardConfig config) {
        config.persistent = !config.persistent;
        config.save();

        createToast(
                "clipboard.persistent.title",
                "clipboard.persistent.message." + (config.persistent ? "enabled" : "disabled"),
                new ItemStack(Items.WRITABLE_BOOK)
        );
        
        playClickSound(client);
    }

    private void togglePrioritization(MinecraftClient client, ClipboardConfig config) {
        config.prioritize = config.prioritize.equals("custom") ? "system" : "custom";
        config.save();

        ItemStack icon = config.prioritize.equals("custom") ? new ItemStack(Items.ENCHANTED_BOOK) : new ItemStack(Items.BOOK);
        createToast(
                "clipboard.prioritization.title",
                "clipboard.prioritization.message." + config.prioritize,
                icon
        );

        playClickSound(client);
    }

    private void toggleEnabled(MinecraftClient client, ClipboardConfig config) {
        config.enabled = !config.enabled;
        config.save();

        createToast(
                "clipboard.toggle.title." + (config.enabled ? "enabled" : "disabled"),
                "clipboard.toggle.message." + (config.enabled ? "enabled" : "disabled"),
                config.enabled ? LAMP_ON : LAMP_OFF
        );

        playClickSound(client);
    }

    private void openClipboardScreen(MinecraftClient client, ClipboardConfig config) {
        client.setScreen(new ClipboardScreen(ClipboardManager.getHistory(), config));
    }

    private void createToast(String titleKey, String messageKey, Object icon) {
        createToast(titleKey, messageKey, "", icon);
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