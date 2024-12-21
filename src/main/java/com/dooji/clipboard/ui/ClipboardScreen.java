package com.dooji.clipboard.ui;

import com.dooji.clipboard.ClipboardItem;
import com.dooji.clipboard.manager.ClipboardManager;
import com.dooji.clipboard.manager.ClipboardConfig;
import com.dooji.omnilib.OmnilibClient;
import com.dooji.omnilib.ui.OmniListWidget;
import com.dooji.omnilib.ui.OmniField;
import com.dooji.omnilib.ui.OmniButton;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Identifier;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClipboardScreen extends Screen {
    private OmniField searchField;
    private OmniListWidget listWidget;
    private ClipboardConfig config;
    private List<ClipboardItem> originalList;
    private List<ClipboardItem> filteredList;
    private String searchQuery = "";

    private static final Identifier LAMP_OFF = Identifier.of("minecraft", "textures/block/redstone_lamp.png");

    public ClipboardScreen(List<ClipboardItem> items, ClipboardConfig config) {
        super(Text.of("Clipboard"));
        
        this.originalList = new ArrayList<>(items);
        this.filteredList = new ArrayList<>(originalList);
        this.config = config;
    }

    @Override
    protected void init() {
        super.init();

        int topPadding = 40;
        int fieldWidth = 200;
        int fieldX = (this.width - fieldWidth) / 2;
        this.searchField = OmnilibClient.createOmniField(
                this.textRenderer,
                fieldX,
                topPadding,
                fieldWidth,
                20,
                Text.translatable("screen.clipboard.search"),
                this.searchQuery,
                this::onSearchChanged
        );
        this.addDrawableChild(this.searchField);

        int listTop = topPadding + 30;
        int listBottom = this.height;
        int listWidth = this.width;
        int itemWidth = listWidth - 140;
        int itemHeight = 60;
        this.listWidget = OmnilibClient.createOmniListWidget(
                this.client,
                listWidth,
                listBottom - listTop,
                listTop,
                listBottom,
                itemWidth,
                itemHeight,
                20,
                5,
                null,
                null,
                0x88000000,
                0x99555555,
                0x88000000,
                0x88888888,
                0xCC888888,
                this::onItemClicked
        );

        setFilteredItems();
        this.addSelectableChild(this.listWidget);
    }

    private void onSearchChanged(String query) {
        this.searchQuery = query;
        setFilteredItems();
    }

    private void setFilteredItems() {
        List<String> terms = List.of(this.searchQuery.toLowerCase().split("\\s+"));
        this.filteredList = this.originalList.stream()
                .filter(item -> terms.stream().allMatch(term ->
                        item.getText().toLowerCase().contains(term) ||
                        item.getDate().toLowerCase().contains(term)))
                .collect(Collectors.toList());

        List<String> entryContents = this.filteredList.stream()
                .map(ClipboardItem::getText)
                .collect(Collectors.toList());

        List<List<OmniButton>> entryButtons = this.filteredList.stream().map(item -> List.of(
                OmnilibClient.createOmniButton(
                        0, 
                        0, 
                        20, 
                        30,
                        Identifier.of("clipboard", "textures/gui/copy.png"),
                        0x80AAAAAA, 
                        0x99AAAAAA,
                        () -> onItemCopied(item.getText())
                ),
                OmnilibClient.createOmniButton(
                        0,
                        0, 
                        20, 
                        30,
                        Identifier.of("clipboard", "textures/gui/delete.png"),
                        0x80FF5555, 
                        0x99FF5555,
                        () -> onItemDeleted(item.getText())
                )
        )).collect(Collectors.toList());

        List<String> entryFooters = this.filteredList.stream()
                .map(item -> Text.translatable("screen.clipboard.recorded_on", item.getDate()).getString())
                .collect(Collectors.toList());

        this.listWidget.setItems(entryContents, entryButtons, entryFooters);
    }

    private void onItemClicked(int index) {
        if (!config.enabled) {
            OmnilibClient.showToast(
                Text.translatable("clipboard.disabled.title"),
                Text.translatable("clipboard.disabled.message"),
                4000L,
                0xFFFFFF,
                0xAAAAAA,
                null,
                LAMP_OFF,
                null,
                16,
                170,
                32
            );

            return;
        }

        ClipboardItem item = filteredList.get(index);
        boolean shiftDown = Screen.hasShiftDown();
        if (shiftDown) {
            ClipboardManager.removeEntry(item.getText());
        } else {
            ClipboardManager.addEntry(item.getText(), ClipboardManager.DATE_FORMAT.format(new java.util.Date()));
        }

        this.originalList = ClipboardManager.getHistory();
        setFilteredItems();
    }

    private void onItemCopied(String itemText) {
        if (!config.enabled) {
            OmnilibClient.showToast(
                Text.translatable("clipboard.disabled.title"),
                Text.translatable("clipboard.disabled.message"),
                4000L,
                0xFFFFFF,
                0xAAAAAA,
                null,
                LAMP_OFF,
                null,
                16,
                170,
                32
            );

            return;
        }

        ClipboardManager.addEntry(itemText, ClipboardManager.DATE_FORMAT.format(new java.util.Date()));

        this.originalList = ClipboardManager.getHistory();
        setFilteredItems();
    }

    private void onItemDeleted(String itemText) {
        if (!config.enabled) {
            OmnilibClient.showToast(
                Text.translatable("clipboard.disabled.title"),
                Text.translatable("clipboard.disabled.message"),
                4000L,
                0xFFFFFF,
                0xAAAAAA,
                null,
                LAMP_OFF,
                null,
                16,
                170,
                32
            );
            
            return;
        }

        ClipboardManager.removeEntry(itemText);

        this.originalList = ClipboardManager.getHistory();
        setFilteredItems();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            this.close();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int titleHeight = this.textRenderer.fontHeight;
        int titleY = (this.searchField.getY() - titleHeight) / 2;
        int titleWidth = this.textRenderer.getWidth(this.title);

        context.drawText(this.textRenderer, this.title, (this.width - titleWidth) / 2, titleY, 0xFFFFFF, false);

        if (this.listWidget != null) {
            this.listWidget.render(context, mouseX, mouseY, delta);
        }
    }
}