package com.legend.main;

import mdlaf.themes.MaterialLiteTheme;

import java.awt.*;

public class MaterialOrientalFontsTheme extends MaterialLiteTheme {

    @Override
    protected void installFonts() {

        /*this.fontBold = new javax.swing.plaf.FontUIResource("Noto Sans", Font.BOLD,14);
        this.fontItalic = new javax.swing.plaf.FontUIResource("Noto Sans", Font.ITALIC,14);
        this.fontMedium = new javax.swing.plaf.FontUIResource("Noto Sans", Font.PLAIN,14);
        this.fontRegular = new javax.swing.plaf.FontUIResource("Noto Sans", Font.PLAIN,14);*/

        this.fontBold = new javax.swing.plaf.FontUIResource("宋体", Font.BOLD,14);
        this.fontItalic = new javax.swing.plaf.FontUIResource("宋体", Font.ITALIC,14);
        this.fontMedium = new javax.swing.plaf.FontUIResource("宋体", Font.PLAIN,14);
        this.fontRegular = new javax.swing.plaf.FontUIResource("宋体", Font.PLAIN,14);
    }
}