package com.astraclient;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/** Scrollable, typed configuration editor for one Astra module. */
public final class AstraModuleOptionsScreen extends Screen {
    private static final int PANEL = 0xFF111722;
    private static final int ROW = 0xFF1A2230;
    private static final int HOVER = 0xFF252D3F;
    private static final int WHITE = 0xFFF3F5FC;
    private static final int MUTED = 0xFF929BB0;
    private final Screen parent;
    private final String moduleId;
    private final String moduleName;
    private final String category;
    private int scrollOffset;
    private int activeSliderIndex = -1;
    private boolean capturingKey;
    private int capturedKeyIndex = -1;
    private int editingTextIndex = -1;
    private String textBuffer = "";
    private String originalTextValue = "";
    private int textCursor;

    public AstraModuleOptionsScreen(Screen parent, String moduleId, String moduleName, String category) {
        super(Text.literal("Astra Advanced Module Settings"));
        this.parent = parent;
        this.moduleId = moduleId;
        this.moduleName = moduleName;
        this.category = category == null ? "Utility" : category;
    }

    private List<AstraModuleOptions.Option> options() {
        return AstraModuleOptions.definitions(moduleId, category);
    }

    private int accent() { return AstraClient.accentColor(); }

    private int panelWidth() { return Math.max(1, Math.min(680, width - 20)); }
    private int panelHeight() { return Math.max(1, Math.min(500, height - 16)); }
    private int rowHeight() { return 48; }
    private int rowStep() { return 54; }
    private int listTop(int top) { return top + 91; }
    private int listBottom(int top, int panelH) { return top + panelH - 45; }
    private int maxScroll(int panelH) {
        int rows = options().size();
        return Math.max(0, rows * rowStep() - Math.max(1, listBottom((height-panelH)/2, panelH) - listTop((height-panelH)/2)));
    }

    private String value(AstraModuleOptions.Option option) {
        if (option.type() == AstraModuleOptions.Type.KEYBIND) {
            return Integer.toString(AstraClient.moduleKeybind(moduleId));
        }
        return AstraModuleOptions.normalize(option,
            AstraModuleOptions.value(moduleId, option.id(), option.defaultValue()));
    }

    private String displayValue(AstraModuleOptions.Option option) {
        String raw = value(option);
        return switch (option.type()) {
            case TOGGLE -> Boolean.parseBoolean(raw) ? "ON" : "OFF";
            case DROPDOWN, MODE -> raw;
            case SLIDER -> raw + (option.id().contains("range") ? " blocks" : "");
            case MULTI_SELECT -> raw.isBlank() ? "None" : raw.replace(",", "  •  ");
            case KEYBIND -> raw.equals("-1") ? "UNBOUND" : AstraClient.keyName(Integer.parseInt(raw));
            case TEXT -> raw.isBlank() ? "Click to enter…" : raw;
            case COLOR -> "#" + raw;
        };
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, 0xFF070A11);
        AstraUiEffects.renderAmbientBackdrop(ctx, width, height, System.currentTimeMillis(), accent());
        int pw = panelWidth(), ph = panelHeight();
        int x = (width-pw)/2, y = (height-ph)/2, a = accent();
        ctx.fill(x-2, y-2, x+pw+2, y+ph+2, 0x884C34A3);
        ctx.fill(x, y, x+pw, y+ph, PANEL);
        ctx.fill(x, y, x+pw, y+3, a);
        ctx.fill(x, y+3, x+pw, y+63, 0xFF0D121C);
        ctx.drawText(textRenderer, moduleName, x+18, y+13, WHITE, true);
        ctx.drawText(textRenderer, "ASTRA  /  ADVANCED CONFIGURATION", x+18, y+32, a, false);
        ctx.drawText(textRenderer, "Typed controls • saved automatically • Esc returns", x+18, y+49, MUTED, false);
        ctx.drawText(textRenderer, "TYPE", x+pw-170, y+32, MUTED, false);
        ctx.drawText(textRenderer, "VALUE", x+pw-104, y+32, MUTED, false);

        int listTop = listTop(y), listBottom = listBottom(y, ph);
        ctx.enableScissor(x+10, listTop, x+pw-10, listBottom);
        List<AstraModuleOptions.Option> options = options();
        for (int i=0; i<options.size(); i++) {
            AstraModuleOptions.Option option = options.get(i);
            int ry = listTop + i*rowStep() - scrollOffset;
            if (ry + rowHeight() < listTop || ry > listBottom) continue;
            boolean hover = mouseX >= x+14 && mouseX <= x+pw-14 && mouseY >= ry && mouseY <= ry+rowHeight();
            ctx.fill(x+14, ry, x+pw-14, ry+rowHeight(), hover ? HOVER : ROW);
            ctx.fill(x+14, ry, x+17, ry+rowHeight(), hover ? a : 0xFF30394A);
            ctx.drawText(textRenderer, option.label(), x+25, ry+7, WHITE, true);
            ctx.drawText(textRenderer, option.description(), x+25, ry+23, MUTED, false);
            String typeLabel = option.type().name().replace('_', ' ');
            ctx.drawText(textRenderer, typeLabel, x+pw-170, ry+8, MUTED, false);
            drawControl(ctx, option, i, x, y, pw, ry, mouseX, mouseY, a);
        }
        ctx.disableScissor();

        int footer = y+ph-35;
        ctx.fill(x+14, footer-4, x+pw-14, footer-3, 0xFF293244);
        ctx.drawText(textRenderer, (options.size()) + " settings  •  " + (scrollOffset > 0 ? "Scroll to browse" : "Right-click a module to return"),
            x+18, footer+7, MUTED, false);
        drawButton(ctx, x+pw-104, footer, 86, 24, "BACK", mouseX, mouseY, a);
    }

    private void drawControl(DrawContext ctx, AstraModuleOptions.Option option, int index,
                             int x, int top, int pw, int ry, int mouseX, int mouseY, int accent) {
        int controlX = x+pw-160, controlW = 130;
        String value = displayValue(option);
        switch (option.type()) {
            case SLIDER -> {
                String label = value;
                ctx.drawText(textRenderer, label, controlX, ry+5, accent, true);
                int barY = ry+28;
                ctx.fill(controlX, barY, controlX+controlW, barY+5, 0xFF30394A);
                double current = AstraModuleOptions.numberValue(moduleId, option.id(), Double.parseDouble(option.defaultValue()));
                double p = (current-option.min())/Math.max(0.00001, option.max()-option.min());
                int fill = (int)Math.round(Math.max(0,Math.min(1,p))*controlW);
                ctx.fill(controlX, barY, controlX+fill, barY+5, accent);
                ctx.fill(controlX+fill-3, barY-3, controlX+fill+3, barY+8, WHITE);
            }
            case TOGGLE -> {
                boolean enabled = Boolean.parseBoolean(value(option));
                ctx.fill(controlX+74, ry+12, controlX+130, ry+32, enabled ? 0xFF17443F : 0xFF303747);
                ctx.drawCenteredTextWithShadow(textRenderer, value, controlX+102, ry+18, enabled ? 0xFF69E6B8 : MUTED);
            }
            case COLOR -> {
                ctx.fill(controlX+3, ry+10, controlX+24, ry+31, colorHex(value));
                ctx.fill(controlX+2, ry+9, controlX+25, ry+32, accent);
                ctx.fill(controlX+4, ry+11, controlX+23, ry+30, colorHex(value));
                ctx.drawText(textRenderer, value, controlX+32, ry+17, WHITE, false);
            }
            default -> {
                ctx.fill(controlX, ry+9, controlX+controlW, ry+34, 0xFF252D3D);
                ctx.fill(controlX, ry+9, controlX+2, ry+34, accent);
                String label = option.type() == AstraModuleOptions.Type.TEXT && index == editingTextIndex
                    ? truncate(textBuffer.substring(0, textCursor) + "|" + textBuffer.substring(textCursor), 18)
                    : truncate(value, 18);
                ctx.drawText(textRenderer, label, controlX+8, ry+17, WHITE, false);
                ctx.drawText(textRenderer, "›", controlX+controlW-12, ry+16, accent, true);
            }
        }
    }

    private int colorHex(String hex) {
        try { return 0xFF000000 | (Integer.parseInt(hex.replace("#",""), 16) & 0x00FFFFFF); }
        catch (NumberFormatException ignored) { return 0xFFAA70FF; }
    }

    private String truncate(String value, int max) {
        if (value.length() <= max) return value;
        return value.substring(0, Math.max(1, max-1)) + "…";
    }

    private void drawButton(DrawContext ctx, int x, int y, int w, int h, String label,
                            int mouseX, int mouseY, int accent) {
        boolean hover = inside(mouseX, mouseY, x, y, w, h);
        ctx.fill(x, y, x+w, y+h, hover ? HOVER : ROW);
        ctx.fill(x, y, x+2, y+h, accent);
        ctx.drawCenteredTextWithShadow(textRenderer, label, x+w/2, y+(h-8)/2, WHITE);
    }

    private boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x+w && my >= y && my <= y+h;
    }

    private void updateSlider(AstraModuleOptions.Option option, double mouseX, int x, int pw) {
        int controlX = x+pw-160, controlW = 130;
        double progress = Math.max(0.0, Math.min(1.0, (mouseX-controlX)/controlW));
        double v = option.min() + progress*(option.max()-option.min());
        if (option.step() > 0) v = option.min() + Math.round((v-option.min())/option.step())*option.step();
        AstraModuleOptions.previewValue(moduleId, option, Double.toString(v));
    }

    private void cycleOption(AstraModuleOptions.Option option) {
        String current = value(option);
        switch (option.type()) {
            case TOGGLE -> AstraModuleOptions.setValue(moduleId, option, Boolean.toString(!Boolean.parseBoolean(current)));
            case DROPDOWN, MODE -> {
                int i = option.choices().indexOf(current);
                AstraModuleOptions.setValue(moduleId, option, option.choices().get((i+1+option.choices().size())%option.choices().size()));
            }
            case MULTI_SELECT -> {
                String[] currentValues = current.isBlank() ? new String[0] : current.split(",");
                String last = currentValues.length == 0 ? "" : currentValues[currentValues.length-1];
                int next = (option.choices().indexOf(last)+1+option.choices().size())%option.choices().size();
                String choice = option.choices().get(next);
                List<String> selected = new java.util.ArrayList<>(java.util.Arrays.asList(currentValues));
                if (selected.contains(choice)) selected.remove(choice); else selected.add(choice);
                AstraModuleOptions.setValue(moduleId, option, String.join(",", selected));
            }
            case COLOR -> {
                int i = option.choices().indexOf(current);
                AstraModuleOptions.setValue(moduleId, option, option.choices().get((i+1+option.choices().size())%option.choices().size()));
            }
            case TEXT -> {
                editingTextIndex = options().indexOf(option);
                textBuffer = current;
                originalTextValue = current;
                textCursor = textBuffer.length();
            }
            case KEYBIND -> {
                capturedKeyIndex = options().indexOf(option);
                capturingKey = true;
            }
            case SLIDER -> { }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);
        int pw=panelWidth(), ph=panelHeight(), x=(width-pw)/2, y=(height-ph)/2;
        if (!inside(mouseX, mouseY, x, y, pw, ph)) {
            commitTextEdit();
            client.setScreen(parent);
            return true;
        }
        if (editingTextIndex >= 0 && editingTextIndex < options().size()) {
            int editingY = listTop(y) + editingTextIndex * rowStep() - scrollOffset;
            if (!inside(mouseX, mouseY, x + 14, editingY, pw - 28, rowHeight())) {
                commitTextEdit();
            } else {
                int controlX = x + pw - 160;
                int relativeX = Math.max(0, (int) mouseX - (controlX + 8));
                int cursor = 0;
                int bestDistance = Integer.MAX_VALUE;
                for (int i = 0; i <= textBuffer.length(); i++) {
                    int atWidth = textRenderer.getWidth(textBuffer.substring(0, i));
                    int distance = Math.abs(atWidth - relativeX);
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        cursor = i;
                    }
                }
                textCursor = cursor;
                return true;
            }
        }
        int footer=y+ph-35;
        if (inside(mouseX,mouseY,x+pw-104,footer,86,24)) {
            commitTextEdit();
            client.setScreen(parent);
            return true;
        }
        int listTop=listTop(y), listBottom=listBottom(y,ph);
        if (mouseY < listTop || mouseY >= listBottom) return true;
        List<AstraModuleOptions.Option> all=options();
        int index=(int)Math.floor((mouseY-listTop+scrollOffset)/(double)rowStep());
        if(index<0||index>=all.size())return true;
        AstraModuleOptions.Option option=all.get(index);
        int ry=listTop+index*rowStep()-scrollOffset;
        if(!inside(mouseX,mouseY,x+14,ry,pw-28,rowHeight()))return true;
        if(option.type()==AstraModuleOptions.Type.SLIDER) {
            activeSliderIndex=index;
            updateSlider(option,mouseX,x,pw);
        } else if(option.type()==AstraModuleOptions.Type.KEYBIND) {
            if(index==0) { capturedKeyIndex=index; capturingKey=true; }
            else cycleOption(option);
        } else {
            cycleOption(option);
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX,double mouseY,int button,double deltaX,double deltaY) {
        if(button==0&&activeSliderIndex>=0) {
            List<AstraModuleOptions.Option> all=options();
            if(activeSliderIndex<all.size()) updateSlider(all.get(activeSliderIndex),mouseX,(width-panelWidth())/2,panelWidth());
            return true;
        }
        return super.mouseDragged(mouseX,mouseY,button,deltaX,deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX,double mouseY,int button) {
        if(activeSliderIndex>=0) AstraClient.saveConfig();
        activeSliderIndex=-1;
        return super.mouseReleased(mouseX,mouseY,button);
    }

    @Override
    public boolean charTyped(char chr,int modifiers) {
        if(editingTextIndex>=0&&!Character.isISOControl(chr)&&textBuffer.length()<64) {
            textBuffer = textBuffer.substring(0, textCursor) + chr + textBuffer.substring(textCursor);
            textCursor++;
            AstraModuleOptions.Option option=options().get(editingTextIndex);
            AstraModuleOptions.previewValue(moduleId,option,textBuffer);
            return true;
        }
        return super.charTyped(chr,modifiers);
    }

    private void commitTextEdit() {
        if (editingTextIndex >= 0 && editingTextIndex < options().size()) {
            AstraModuleOptions.setValue(moduleId, options().get(editingTextIndex), textBuffer);
        }
        editingTextIndex = -1;
    }

    @Override
    public boolean keyPressed(int keyCode,int scanCode,int modifiers) {
        if(capturingKey) {
            AstraModuleOptions.Option option=options().get(capturedKeyIndex);
            if(capturedKeyIndex==0) {
                AstraClient.setModuleKeybind(moduleId,keyCode==GLFW.GLFW_KEY_ESCAPE?GLFW.GLFW_KEY_UNKNOWN:keyCode);
            }
            capturingKey=false;
            capturedKeyIndex=-1;
            return true;
        }
        if(editingTextIndex>=0) {
            AstraModuleOptions.Option option=options().get(editingTextIndex);
            if(keyCode==GLFW.GLFW_KEY_ENTER) {
                AstraModuleOptions.setValue(moduleId,option,textBuffer);
                editingTextIndex=-1;
                return true;
            }
            if(keyCode==GLFW.GLFW_KEY_ESCAPE) {
                AstraModuleOptions.previewValue(moduleId,option,originalTextValue);
                editingTextIndex=-1;
                return true;
            }
            if(keyCode==GLFW.GLFW_KEY_BACKSPACE) {
                if(textCursor>0) {
                    textBuffer=textBuffer.substring(0,textCursor-1)+textBuffer.substring(textCursor);
                    textCursor--;
                    AstraModuleOptions.previewValue(moduleId,option,textBuffer);
                }
                return true;
            }
            if(keyCode==GLFW.GLFW_KEY_DELETE) {
                if(textCursor<textBuffer.length()) {
                    textBuffer=textBuffer.substring(0,textCursor)+textBuffer.substring(textCursor+1);
                    AstraModuleOptions.previewValue(moduleId,option,textBuffer);
                }
                return true;
            }
            if(keyCode==GLFW.GLFW_KEY_LEFT) { textCursor=Math.max(0,textCursor-1); return true; }
            if(keyCode==GLFW.GLFW_KEY_RIGHT) { textCursor=Math.min(textBuffer.length(),textCursor+1); return true; }
            if(keyCode==GLFW.GLFW_KEY_HOME) { textCursor=0; return true; }
            if(keyCode==GLFW.GLFW_KEY_END) { textCursor=textBuffer.length(); return true; }
            return true;
        }
        if(keyCode==GLFW.GLFW_KEY_ESCAPE||keyCode==GLFW.GLFW_KEY_RIGHT_SHIFT) {
            client.setScreen(parent);
            return true;
        }
        if(keyCode==GLFW.GLFW_KEY_UP) { scrollOffset=Math.max(0,scrollOffset-24); return true; }
        if(keyCode==GLFW.GLFW_KEY_DOWN) { scrollOffset=Math.min(maxScroll(panelHeight()),scrollOffset+24); return true; }
        return super.keyPressed(keyCode,scanCode,modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX,double mouseY,double horizontalAmount,double verticalAmount) {
        scrollOffset=Math.max(0,Math.min(maxScroll(panelHeight()),scrollOffset-(int)Math.round(verticalAmount*rowStep())));
        return true;
    }

    @Override
    public void close() { client.setScreen(parent); }

    @Override
    public boolean shouldPause() { return false; }
}
