package com.dooji.clipboard.ui;

import com.dooji.omnilib.OmnilibClient;
import com.dooji.omnilib.ui.OmniField;
import com.dooji.omnilib.ui.OmniListWidget;
import com.dooji.omnilib.ui.OmniButton;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClipboardConfigScreen extends Screen {
    private final Screen parentScreen;
    private final String title;
    private final List<ClipboardOption> options;
    private final Runnable onSave;
    private final Runnable onCancel;
    private List<String> originalValues;
    private OmniField searchField;
    private OmniListWidget listWidget;
    private List<ClipboardOption> filteredOptions;
    private String searchQuery = "";

    public ClipboardConfigScreen(Screen parentScreen, String title, List<ClipboardOption> options, Runnable onSave, Runnable onCancel) {
        super(Text.translatable(title));
        this.parentScreen = parentScreen;
        this.title = title;
        this.options = new ArrayList<>(options);
        this.filteredOptions = new ArrayList<>(options);
        this.onSave = onSave;
        this.onCancel = onCancel;
        this.originalValues = options.stream()
                .map(ClipboardOption::getCurrentValue)
                .collect(Collectors.toList());
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
                Text.translatable("clipboard.config.search"),
                this.searchQuery,
                this::onSearchChanged
        );
        this.addDrawableChild(this.searchField);

        int buttonAreaHeight = 50;
        int buttonWidth = 100;
        int buttonHeight = 20;
        int buttonSpacing = 10;
        int buttonY = this.height - buttonAreaHeight / 2 - buttonHeight / 2;

        this.addDrawableChild(OmnilibClient.createOmniButton(
                (this.width - buttonWidth * 2 - buttonSpacing) / 2,
                buttonY,
                buttonWidth,
                buttonHeight,
                Text.translatable("clipboard.config.save"),
                0x80000000,
                0x80999999,
                0xFFFFFFFF,
                0xFFFFFF88,
                () -> {
                    this.onSave.run();
                    this.client.setScreen(this.parentScreen);
                }
        ));

        this.addDrawableChild(OmnilibClient.createOmniButton(
                (this.width + buttonSpacing) / 2,
                buttonY,
                buttonWidth,
                buttonHeight,
                Text.translatable("clipboard.config.cancel"),
                0x80000000,
                0x80999999,
                0xFFFFFFFF,
                0xFFFFFF88,
                () -> {
                    restoreOriginalValues();
                    this.onCancel.run();
                    this.client.setScreen(this.parentScreen);
                }
        ));

        int listTop = topPadding + 30;
        int listBottom = this.height - buttonAreaHeight - 10;
        int listWidth = this.width;
        int itemHeight = 30;

        this.listWidget = OmnilibClient.createOmniListWidget(
                this.client,
                listWidth,
                listBottom - listTop,
                listTop,
                listBottom,
                listWidth - 140,
                itemHeight,
                60,
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

    private void restoreOriginalValues() {
        for (int i = 0; i < options.size(); i++) {
            options.get(i).setValue(originalValues.get(i));
        }

        setFilteredItems();
    }

    private void onSearchChanged(String query) {
        this.searchQuery = query.toLowerCase();
        setFilteredItems();
    }

    private void setFilteredItems() {
        this.filteredOptions = this.options.stream()
                .filter(option -> option.getLabel().toLowerCase().contains(searchQuery))
                .collect(Collectors.toList());

        List<String> entryContents = this.filteredOptions.stream()
                .map(ClipboardOption::getLabel)
                .collect(Collectors.toList());

        List<List<OmniButton>> entryButtons = this.filteredOptions.stream().map(option -> List.of(
                OmnilibClient.createOmniButton(
                        0, 0, 80, 20,
                        Text.of(option.getCurrentValue()),
                        0x80000000,
                        0x80999999,
                        0xFFFFFFFF,
                        0xFFFFFF88,
                        () -> {
                            option.toggle();
                            setFilteredItems();
                        }
                )
        )).collect(Collectors.toList());

        List<String> entryFooters = this.filteredOptions.stream()
                .map(ClipboardOption::getDescription)
                .collect(Collectors.toList());

        this.listWidget.setItems(entryContents, entryButtons, entryFooters);
    }

    private void onItemClicked(int index) {
        ClipboardOption option = filteredOptions.get(index);

        option.toggle();
        setFilteredItems();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            this.client.setScreen(this.parentScreen);
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context);
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