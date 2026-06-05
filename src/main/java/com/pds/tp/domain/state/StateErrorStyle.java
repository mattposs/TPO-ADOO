package com.pds.tp.domain.state;

public final class StateErrorStyle {
    private StateErrorStyle() {
    }

    public static IllegalStateException invalidTransition(String message) {
        validate(message);
        return new IllegalStateException(message);
    }

    public static String genericRejectedOperation(String statusName) {
        String safeStatus = statusName == null ? "Desconocido" : statusName;
        return "Operacion no permitida en estado " + safeStatus + ".";
    }

    private static void validate(String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Los mensajes de error de estado no pueden ser vacios.");
        }
        if (!message.trim().endsWith(".")) {
            throw new IllegalArgumentException("Los mensajes de error de estado deben finalizar con punto.");
        }
    }
}

