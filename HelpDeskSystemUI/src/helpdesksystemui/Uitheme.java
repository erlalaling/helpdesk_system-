package helpdesksystemui;

import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.border.Border;

public final class Uitheme {

    private Uitheme() {
    }

    // Core Application Palette (Preserved from Dashboard & Sarmiento Campus Theme)
    public static final Color SIDEBAR_BG = new Color(30, 41, 59);
    public static final Color SIDEBAR_HOVER = new Color(51, 65, 85);
    public static final Color SIDEBAR_ACTIVE = new Color(37, 99, 235);
    public static final Color SIDEBAR_TEXT = new Color(241, 245, 249);
    public static final Color SIDEBAR_MUTED = new Color(148, 163, 184);

    public static final Color CONTENT_BG = new Color(241, 245, 249);
    public static final Color CARD_BG = Color.WHITE;
    public static final Color HEADER_BG = Color.WHITE;
    public static final Color BORDER_COLOR = new Color(226, 232, 240);

    public static final Color TEXT_PRIMARY = new Color(15, 23, 42);
    public static final Color TEXT_SECONDARY = new Color(100, 116, 139);

    public static final Color PRIMARY = new Color(37, 99, 235);
    public static final Color PRIMARY_HOVER = new Color(29, 78, 216);
    public static final Color DANGER = new Color(220, 38, 38);

    // Summary Card & Status Accents
    public static final Color ACCENT_TOTAL = new Color(37, 99, 235);
    public static final Color ACCENT_OPEN = new Color(234, 88, 12);
    public static final Color ACCENT_IN_PROGRESS = new Color(202, 138, 4);
    public static final Color ACCENT_RESOLVED = new Color(22, 163, 74);
    public static final Color ACCENT_CLOSED = new Color(100, 116, 139);

    // Badge Background & Foreground Colors
    public static final Color BADGE_HIGH_BG = new Color(254, 226, 226);
    public static final Color BADGE_HIGH_FG = new Color(185, 28, 28);

    public static final Color BADGE_MEDIUM_BG = new Color(254, 243, 199);
    public static final Color BADGE_MEDIUM_FG = new Color(180, 83, 9);

    public static final Color BADGE_LOW_BG = new Color(224, 242, 254);
    public static final Color BADGE_LOW_FG = new Color(3, 105, 161);

    public static final Color BADGE_OPEN_BG = new Color(255, 237, 213);
    public static final Color BADGE_OPEN_FG = new Color(194, 65, 12);

    public static final Color BADGE_PROGRESS_BG = new Color(254, 249, 195);
    public static final Color BADGE_PROGRESS_FG = new Color(161, 98, 7);

    public static final Color BADGE_RESOLVED_BG = new Color(220, 252, 231);
    public static final Color BADGE_RESOLVED_FG = new Color(21, 128, 61);

    public static final Color BADGE_CLOSED_BG = new Color(241, 245, 249);
    public static final Color BADGE_CLOSED_FG = new Color(71, 85, 105);

    // Typography
    public static final Font FONT_BRAND = new Font("SansSerif", Font.BOLD, 16);
    public static final Font FONT_HEADER = new Font("SansSerif", Font.BOLD, 20);
    public static final Font FONT_SECTION = new Font("SansSerif", Font.BOLD, 15);
    public static final Font FONT_CARD_TITLE = new Font("SansSerif", Font.BOLD, 11);
    public static final Font FONT_CARD_VALUE = new Font("SansSerif", Font.BOLD, 26);
    public static final Font FONT_BODY = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("SansSerif", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font FONT_BADGE = new Font("SansSerif", Font.BOLD, 11);

    public static Border createCardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)
        );
    }

    public static Border createInputBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)
        );
    }

    public static Border createErrorInputBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DANGER, 2),
                BorderFactory.createEmptyBorder(6, 9, 6, 9)
        );
    }
}