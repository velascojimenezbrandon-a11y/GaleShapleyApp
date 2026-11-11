
package com.example.galeshapleyapp

data class MatchingResult(
    val partners: IntArray,
    val steps: List<String>,
    val proposalCount: Int
)

fun galeShapley(
    preferencesA: Array<IntArray>,
    preferencesB: Array<IntArray>,
    proposingSide: String = "A"
): MatchingResult {

    val n = preferencesA.size

    require(n > 0 && preferencesB.size == n) {
        "Los conjuntos deben tener el mismo tamaño"
    }

    require(proposingSide == "A" || proposingSide == "B") {
        "El conjunto proponente debe ser A o B"
    }

    val expected = (0 until n).toSet()

    for (row in preferencesA + preferencesB) {
        require(row.size == n && row.toSet() == expected) {
            "Las preferencias deben ser permutaciones válidas"
        }
    }

    val proposers = if (proposingSide == "A") {
        preferencesA
    } else {
        preferencesB
    }

    val receivers = if (proposingSide == "A") {
        preferencesB
    } else {
        preferencesA
    }

    val proposerLabel = proposingSide
    val receiverLabel = if (proposingSide == "A") "B" else "A"

    val partnerProposer = IntArray(n) { -1 }
    val partnerReceiver = IntArray(n) { -1 }
    val nextProposal = IntArray(n)

    val receiverRank = Array(n) { IntArray(n) }

    for (r in 0 until n) {
        for (rank in 0 until n) {
            val p = receivers[r][rank]
            receiverRank[r][p] = rank
        }
    }

    val free = ArrayDeque<Int>()

    for (p in 0 until n) {
        free.addLast(p)
    }

    val steps = mutableListOf<String>()
    var proposals = 0

    while (free.isNotEmpty()) {

        val p = free.removeFirst()
        val r = proposers[p][nextProposal[p]]

        nextProposal[p]++
        proposals++

        steps.add(
            "$proposerLabel${p + 1} propone a " +
                    "$receiverLabel${r + 1}"
        )

        val current = partnerReceiver[r]

        if (current == -1) {

            partnerProposer[p] = r
            partnerReceiver[r] = p

            steps.add(
                "$receiverLabel${r + 1} acepta a " +
                        "$proposerLabel${p + 1}"
            )

        } else if (
            receiverRank[r][p] < receiverRank[r][current]
        ) {

            partnerProposer[current] = -1
            free.addLast(current)

            partnerProposer[p] = r
            partnerReceiver[r] = p

            steps.add(
                "$receiverLabel${r + 1} prefiere a " +
                        "$proposerLabel${p + 1} y rechaza a " +
                        "$proposerLabel${current + 1}"
            )

        } else {

            free.addLast(p)

            steps.add(
                "$receiverLabel${r + 1} rechaza a " +
                        "$proposerLabel${p + 1}"
            )
        }
    }

    val partnersA = if (proposingSide == "A") {
        partnerProposer
    } else {
        partnerReceiver
    }

    return MatchingResult(
        partners = partnersA,
        steps = steps,
        proposalCount = proposals
    )
}
