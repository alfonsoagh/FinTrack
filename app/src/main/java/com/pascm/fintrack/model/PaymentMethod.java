package com.pascm.fintrack.model;

/**
 * Representa un método de pago disponible para transacciones
 */
public class PaymentMethod {

    public enum Type {
        CASH,           // Efectivo
        CREDIT_CARD,    // Tarjeta de crédito
        DEBIT_CARD      // Tarjeta de débito
    }

    private final Type type;
    private final long entityId; // ID de la tarjeta (0 para efectivo)
    private final String displayName;
    private final String details; // Ej: "•••• 1234"

    // Constructor para efectivo
    public PaymentMethod() {
        this.type = Type.CASH;
        this.entityId = 0;
        this.displayName = "Efectivo";
        this.details = "Pago en efectivo";
    }

    // Constructor para tarjetas
    public PaymentMethod(Type type, long entityId, String displayName, String details) {
        this.type = type;
        this.entityId = entityId;
        this.displayName = displayName;
        this.details = details;
    }

    public Type getType() {
        return type;
    }

    public long getEntityId() {
        return entityId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDetails() {
        return details;
    }

    @Override
    public String toString() {
        return displayName + (details != null && !details.isEmpty() ? " - " + details : "");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PaymentMethod that = (PaymentMethod) obj;
        return entityId == that.entityId && type == that.type;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (int) (entityId ^ (entityId >>> 32));
        return result;
    }
}
