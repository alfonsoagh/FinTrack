package com.pascm.fintrack.model;

public class CreditCard {
    private String bank;
    private String label;
    private String brand; // "visa" o "mastercard"
    private String panLast4; // últimos 4 dígitos
    private double limit; // límite de crédito
    private double balance; // saldo usado
    private CardGradient gradient;

    public CreditCard(String bank, String label, String brand, String panLast4,
                      double limit, double balance, CardGradient gradient) {
        this.bank = bank;
        this.label = label;
        this.brand = brand;
        this.panLast4 = panLast4;
        this.limit = limit;
        this.balance = balance;
        this.gradient = gradient;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getPanLast4() {
        return panLast4;
    }

    public void setPanLast4(String panLast4) {
        this.panLast4 = panLast4;
    }

    public double getLimit() {
        return limit;
    }

    public void setLimit(double limit) {
        this.limit = limit;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public CardGradient getGradient() {
        return gradient;
    }

    public void setGradient(CardGradient gradient) {
        this.gradient = gradient;
    }

    // Calcula el porcentaje de uso
    public float getUsagePercentage() {
        if (limit <= 0) return 0;
        return (float) Math.min(100, Math.max(0, (balance / limit) * 100));
    }

    // Determina el nivel de uso
    public UsageLevel getUsageLevel() {
        float pct = getUsagePercentage();
        if (pct < 35) return UsageLevel.LOW;
        if (pct < 70) return UsageLevel.MEDIUM;
        return UsageLevel.HIGH;
    }

    // Enum para los niveles de uso
    public enum UsageLevel {
        LOW("Bajo uso", 0xFF22C55E),      // verde
        MEDIUM("Uso medio", 0xFFF59E0B),  // amarillo/ámbar
        HIGH("Uso alto", 0xFFEF4444);     // rojo

        private final String label;
        private final int color;

        UsageLevel(String label, int color) {
            this.label = label;
            this.color = color;
        }

        public String getLabel() {
            return label;
        }

        public int getColor() {
            return color;
        }
    }

    // Enum para los gradientes de colores
    public enum CardGradient {
        VIOLET(0xFF7C3AED, 0xFFA855F7, 0xFF6D28D9),          // Aurora: violet-500, fuchsia-500, violet-700
        SKY_BLUE(0xFF2563EB, 0xFF0EA5E9, 0xFF22D3EE),        // Ocean: blue-600, sky-500, cyan-400
        SUNSET(0xFFF59E0B, 0xFFFB923C, 0xFFEF4444),          // Sunset/Mango: amber-500, orange-400, red-500
        EMERALD(0xFF059669, 0xFF10B981, 0xFF14B8A6),         // Emerald: emerald-600, emerald-500, teal-500
        ROYAL(0xFF1E40AF, 0xFF3730A3, 0xFF7C3AED),           // Royal: blue-700, indigo-700, violet-500
        SILVER(0xFFF3F4F6, 0xFFE5E7EB, 0xFF9CA3AF),          // Silver (plateado): gray-100, gray-200, gray-400
        ONYX(0xFF111827, 0xFF000000, 0xFF111827),            // Onyx (negro): gray-900, black, gray-900
        CRIMSON(0xFFDC2626, 0xFFEF4444, 0xFF991B1B),         // Crimson (rojo): red-600, red-500, red-800
        GOLD(0xFFF59E0B, 0xFFFBBF24, 0xFFEAB308);            // Gold (amarillo/dorado): amber-500, amber-300, yellow-500

        private final int startColor;
        private final int centerColor;
        private final int endColor;

        CardGradient(int startColor, int centerColor, int endColor) {
            this.startColor = startColor;
            this.centerColor = centerColor;
            this.endColor = endColor;
        }

        public int getStartColor() {
            return startColor;
        }

        public int getCenterColor() {
            return centerColor;
        }

        public int getEndColor() {
            return endColor;
        }
    }
}
