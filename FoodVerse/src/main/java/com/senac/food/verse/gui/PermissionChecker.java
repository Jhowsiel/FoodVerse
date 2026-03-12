package com.senac.food.verse.gui;

import com.senac.food.verse.SessionContext;

import java.text.Normalizer;

final class PermissionChecker {

    private PermissionChecker() {
    }

    static boolean hasRole(SessionContext ctx, String role) {
        return ctx != null
                && ctx.getCargo() != null
                && normalizeRole(ctx.getCargo()).equals(normalizeRole(role));
    }

    static boolean hasOperationalRestaurantContext(SessionContext ctx) {
        return ctx != null && ctx.getCargo() != null && (!ctx.isAdmin() || ctx.adminTemContextoRestaurante());
    }

    static boolean canAccessRestaurantManagement(SessionContext ctx) {
        return ctx != null && ctx.isAdmin();
    }

    static boolean canManageRestaurantContext(SessionContext ctx) {
        return hasOperationalRestaurantContext(ctx) && (ctx.isAdmin() || hasRole(ctx, "gerente"));
    }

    static boolean canAccessInventory(SessionContext ctx) {
        return hasOperationalRestaurantContext(ctx)
                && (ctx.isAdmin() || hasRole(ctx, "gerente") || hasRole(ctx, "cozinheiro") || hasRole(ctx, "chef"));
    }

    static boolean canEditInventory(SessionContext ctx) {
        return canManageRestaurantContext(ctx);
    }

    static boolean canAccessKitchen(SessionContext ctx) {
        return hasOperationalRestaurantContext(ctx)
                && (ctx.isAdmin() || hasRole(ctx, "gerente") || hasRole(ctx, "cozinheiro") || hasRole(ctx, "chef"));
    }

    static boolean canAccessOrders(SessionContext ctx) {
        return hasOperationalRestaurantContext(ctx)
                && (ctx.isAdmin() || hasRole(ctx, "gerente") || hasRole(ctx, "atendente") || hasRole(ctx, "garçom"));
    }

    static boolean canAccessDeliveries(SessionContext ctx) {
        return hasOperationalRestaurantContext(ctx)
                && (ctx.isAdmin() || hasRole(ctx, "gerente") || hasRole(ctx, "atendente")
                || hasRole(ctx, "garçom") || hasRole(ctx, "entregador"));
    }

    static boolean canAccessTables(SessionContext ctx) {
        return hasOperationalRestaurantContext(ctx)
                && (ctx.isAdmin() || hasRole(ctx, "gerente") || hasRole(ctx, "atendente") || hasRole(ctx, "garçom"));
    }

    static boolean canCreateReservation(SessionContext ctx) {
        return canAccessTables(ctx);
    }

    static boolean canCancelReservation(SessionContext ctx) {
        return canManageRestaurantContext(ctx);
    }

    static boolean canAccessUsers(SessionContext ctx) {
        return ctx != null && ctx.getCargo() != null && (ctx.isAdmin() || hasRole(ctx, "gerente"));
    }

    static boolean canAccessModule(SessionContext ctx, String cardName) {
        if (cardName == null || "HOME".equals(cardName)) {
            return true;
        }
        return switch (cardName) {
            case "RESTAURANTES" -> canAccessRestaurantManagement(ctx);
            case "USUARIOS" -> canAccessUsers(ctx);
            case "MEU_RESTAURANTE", "CARDAPIO" -> canManageRestaurantContext(ctx);
            case "ESTOQUE", "ESTOQUE_COZ" -> canAccessInventory(ctx);
            case "MESAS" -> canAccessTables(ctx);
            case "PEDIDOS" -> canAccessOrders(ctx);
            case "KDS" -> canAccessKitchen(ctx);
            case "ENTREGAS" -> canAccessDeliveries(ctx);
            default -> false;
        };
    }

    static String buildBlockedModuleMessage(SessionContext ctx, String cardName) {
        if (ctx != null && ctx.isAdmin() && !ctx.adminTemContextoRestaurante()
                && !"RESTAURANTES".equals(cardName) && !"USUARIOS".equals(cardName)) {
            return "Selecione um restaurante antes de acessar este módulo.";
        }
        return "Seu perfil não possui permissão para acessar este módulo.";
    }

    private static String normalizeRole(String role) {
        String normalized = Normalizer.normalize(role == null ? "" : role, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}+", "").trim().toLowerCase();
    }
}
