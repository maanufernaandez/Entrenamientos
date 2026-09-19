package com.example.entrenamientos.logic

/**
 * Reglas de participación por cuartos (infantil y minibasket) para montar los
 * quintetos. Lógica pura: recibe ids de jugadores y los quintetos ya guardados
 * (uno por cuarto) y decide qué jugadores son obligatorios o están bloqueados.
 */
object LineupRules {

    /** En infantil las reglas de participación solo se aplican con 8 o más convocados. */
    private fun infantilRulesApply(rules: CategoryRules, summonedCount: Int): Boolean =
        rules.isInfantil && summonedCount >= 8

    /**
     * Jugadores que DEBEN estar en el cuarto [currentQuarter] (1-based).
     *
     * - Infantil (3.er cuarto): quienes no han jugado ni el 1.º ni el 2.º.
     * - Minibasket: en el 5.º, quienes no han jugado nada; en el 6.º, quienes
     *   han jugado menos de 2 cuartos.
     */
    fun forcedPlayers(
        rules: CategoryRules,
        summonedIds: List<Long>,
        lineups: List<List<Long>>,
        currentQuarter: Int
    ): Set<Long> {

        if (
            infantilRulesApply(rules, summonedIds.size) &&
            currentQuarter == 3 &&
            lineups.size == 2
        ) {
            return summonedIds
                .filter { id -> !lineups[0].contains(id) && !lineups[1].contains(id) }
                .toSet()
        }

        if (rules.isMini) {
            return summonedIds
                .filter { id ->
                    val played = lineups.count { it.contains(id) }

                    (currentQuarter == 5 && played == 0) ||
                            (currentQuarter == 6 && played < 2)
                }
                .toSet()
        }

        return emptySet()
    }

    /**
     * Si el jugador [playerId] NO puede jugar el cuarto [currentQuarter] según la
     * normativa. [currentSelection] son los jugadores ya elegidos para este cuarto.
     */
    fun isBanned(
        rules: CategoryRules,
        playerId: Long,
        summonedIds: List<Long>,
        lineups: List<List<Long>>,
        currentQuarter: Int,
        currentSelection: Set<Long>
    ): Boolean =
        when {
            rules.is3x3 -> false

            rules.isMini ->
                isBannedInMini(playerId, summonedIds, lineups, currentQuarter, currentSelection)

            rules.isInfantil && infantilRulesApply(rules, summonedIds.size) ->
                // No puede jugar el 3.er cuarto quien ya jugó el 1.º y el 2.º.
                currentQuarter == 3 &&
                        lineups.size >= 2 &&
                        lineups[0].contains(playerId) &&
                        lineups[1].contains(playerId)

            else -> false
        }

    private fun isBannedInMini(
        playerId: Long,
        summonedIds: List<Long>,
        lineups: List<List<Long>>,
        currentQuarter: Int,
        currentSelection: Set<Long>
    ): Boolean {

        val playedTotal = lineups.count { it.contains(playerId) }

        // Cuartos jugados por otro jugador contando la selección del cuarto actual.
        fun playedWithSelection(id: Long): Int =
            lineups.count { it.contains(id) } +
                    if (currentSelection.contains(id)) 1 else 0

        return when {

            currentQuarter <= 5 ->
                if (summonedIds.size == 8) {
                    when {
                        playedTotal >= 4 -> true

                        playedTotal == 3 ->
                            summonedIds.any { other ->
                                other != playerId && playedWithSelection(other) >= 4
                            }

                        else -> false
                    }
                } else {
                    playedTotal >= 3
                }

            currentQuarter == 6 ->
                when (summonedIds.size) {
                    8 ->
                        lineups.take(5).count { it.contains(playerId) } >= 4
                    in 13..15 ->
                        playedTotal >= 3 &&
                                summonedIds.any { other ->
                                    other != playerId && playedWithSelection(other) < 3
                                }
                    else -> false
                }

            else -> false
        }
    }
}