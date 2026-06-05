package com.pds.tp.domain.shared;

import java.util.Map;

public final class RankScale {
    private static final Map<String, Integer> RANK_VALUES = Map.of(
            "HIERRO", 1,
            "BRONCE", 2,
            "PLATA", 3,
            "ORO", 4,
            "PLATINO", 5,
            "DIAMANTE", 6,
            "RADIANTE", 7
    );

    private RankScale() {
    }

    public static int toValue(String rank) {
        if (rank == null || rank.isBlank()) {
            return 0;
        }
        return RANK_VALUES.getOrDefault(rank.trim().toUpperCase(), 0);
    }
}

