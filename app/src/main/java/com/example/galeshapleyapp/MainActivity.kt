
package com.example.galeshapleyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.galeshapleyapp.ui.theme.GaleShapleyAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GaleShapleyAppTheme {
                GaleShapleyScreen()
            }
        }
    }
}

// Genera listas válidas de preferencias aleatorias.
private fun generatePreferences(n: Int): List<List<Int>> {
    return List(n) {
        (0 until n).shuffled()
    }
}

// Convierte las preferencias al formato del algoritmo.
private fun toPreferenceArray(
    preferences: List<List<Int>>
): Array<IntArray> {
    return preferences.map { it.toIntArray() }.toTypedArray()
}

// Intercambia dos posiciones dentro de una lista de preferencias.
private fun movePreference(
    preferences: List<List<Int>>,
    row: Int,
    position: Int,
    direction: Int
): List<List<Int>> {
    val target = position + direction

    if (target !in preferences[row].indices) {
        return preferences
    }

    return preferences.mapIndexed { index, list ->
        if (index != row) {
            list
        } else {
            list.toMutableList().apply {
                val temp = this[position]
                this[position] = this[target]
                this[target] = temp
            }
        }
    }
}

@Composable
fun GaleShapleyScreen() {

    var vertexCount by remember { mutableIntStateOf(3) }
    var proposingSide by remember { mutableStateOf("A") }
    var result by remember { mutableStateOf<MatchingResult?>(null) }

    var preferencesA by remember {
        mutableStateOf(generatePreferences(3))
    }

    var preferencesB by remember {
        mutableStateOf(generatePreferences(3))
    }

    var editing by remember { mutableStateOf(false) }
    var selectedSide by remember { mutableStateOf("A") }
    var selectedVertex by remember { mutableIntStateOf(0) }

    val blue = Color(0xFF2979FF)
    val orange = Color(0xFFFF9800)
    val green = Color(0xFF008577)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            "Gale-Shapley",
            fontSize = 28.sp,
            color = Color(0xFF172B4D)
        )

        Text(
            "Simulador de emparejamiento estable",
            fontSize = 14.sp,
            color = Color.DarkGray
        )

        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(Modifier.padding(16.dp)) {

                Text("Configuración", fontSize = 20.sp)

                Spacer(Modifier.height(12.dp))

                Text("Vértices por conjunto: $vertexCount")

                Slider(
                    value = vertexCount.toFloat(),
                    onValueChange = { value ->
                        val newCount = value.toInt().coerceIn(2, 6)

                        if (newCount != vertexCount) {
                            vertexCount = newCount
                            preferencesA = generatePreferences(newCount)
                            preferencesB = generatePreferences(newCount)
                            result = null
                        }
                    },
                    valueRange = 2f..6f,
                    steps = 3
                )

                Spacer(Modifier.height(8.dp))

                Text("¿Qué conjunto propone?")

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = proposingSide == "A",
                        onClick = {
                            proposingSide = "A"
                            result = null
                        }
                    )
                    Text("Conjunto A")

                    Spacer(Modifier.width(16.dp))

                    RadioButton(
                        selected = proposingSide == "B",
                        onClick = {
                            proposingSide = "B"
                            result = null
                        }
                    )
                    Text("Conjunto B")
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Botones de generación y edición.

        // Preferencias y editor de un solo vértice.
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(Modifier.padding(16.dp)) {

                Text("Preferencias", fontSize = 20.sp)

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        preferencesA = generatePreferences(vertexCount)
                        preferencesB = generatePreferences(vertexCount)
                        result = null
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Generar nuevas preferencias")
                }

                OutlinedButton(
                    onClick = {
                        editing = !editing
                        result = null
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (editing) "Finalizar edición"
                        else "Editar preferencias"
                    )
                }

                Spacer(Modifier.height(12.dp))

                if (editing) {

                    Text("Selecciona el conjunto")

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedSide == "A",
                            onClick = {
                                selectedSide = "A"
                                selectedVertex = 0
                            }
                        )
                        Text("A")

                        Spacer(Modifier.width(16.dp))

                        RadioButton(
                            selected = selectedSide == "B",
                            onClick = {
                                selectedSide = "B"
                                selectedVertex = 0
                            }
                        )
                        Text("B")
                    }

                    Spacer(Modifier.height(8.dp))

                    Text("Selecciona el vértice")

                    // Selector horizontal de vértices.
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.spacedBy(4.dp)
                    ) {
                        for (i in 0 until vertexCount) {
                            val isSelected = selectedVertex == i

                            if (isSelected) {
                                Button(
                                    onClick = {
                                        selectedVertex = i
                                    },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("${i + 1}")
                                }
                            } else {
                                OutlinedButton(
                                    onClick = {
                                        selectedVertex = i
                                    },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("${i + 1}")
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    val currentPreferences =
                        if (selectedSide == "A") {
                            preferencesA
                        } else {
                            preferencesB
                        }

                    val currentList =
                        currentPreferences[selectedVertex]

                    Text(
                        "Orden de $selectedSide${selectedVertex + 1}",
                        fontSize = 18.sp
                    )

                    Spacer(Modifier.height(8.dp))

                    currentList.forEachIndexed { position, target ->

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment =
                                Alignment.CenterVertically,
                            horizontalArrangement =
                                Arrangement.SpaceBetween
                        ) {

                            val targetPrefix =
                                if (selectedSide == "A") "B" else "A"

                            Text(
                                "${position + 1}. " +
                                        "$targetPrefix${target + 1}"
                            )

                            Row {
                                TextButton(
                                    onClick = {
                                        if (selectedSide == "A") {
                                            preferencesA = movePreference(
                                                preferencesA,
                                                selectedVertex,
                                                position,
                                                -1
                                            )
                                        } else {
                                            preferencesB = movePreference(
                                                preferencesB,
                                                selectedVertex,
                                                position,
                                                -1
                                            )
                                        }
                                        result = null
                                    },
                                    enabled = position > 0
                                ) {
                                    Text("↑")
                                }

                                TextButton(
                                    onClick = {
                                        if (selectedSide == "A") {
                                            preferencesA = movePreference(
                                                preferencesA,
                                                selectedVertex,
                                                position,
                                                1
                                            )
                                        } else {
                                            preferencesB = movePreference(
                                                preferencesB,
                                                selectedVertex,
                                                position,
                                                1
                                            )
                                        }
                                        result = null
                                    },
                                    enabled =
                                        position < currentList.lastIndex
                                ) {
                                    Text("↓")
                                }
                            }
                        }
                    }

                } else {

                    Text("Conjunto A", color = blue)

                    preferencesA.forEachIndexed { index, pref ->
                        Text(
                            "A${index + 1}: " +
                                    pref.joinToString(", ") {
                                        "B${it + 1}"
                                    }
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text("Conjunto B", color = orange)

                    preferencesB.forEachIndexed { index, pref ->
                        Text(
                            "B${index + 1}: " +
                                    pref.joinToString(", ") {
                                        "A${it + 1}"
                                    }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Button(
                onClick = {
                    result = galeShapley(
                        toPreferenceArray(preferencesA),
                        toPreferenceArray(preferencesB),
                        proposingSide
                    )
                }
            ) {
                Text("Ejecutar")
            }

            OutlinedButton(
                onClick = { result = null }
            ) {
                Text("Reiniciar")
            }
        }

        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(Modifier.padding(16.dp)) {

                Text("Grafo bipartito", fontSize = 20.sp)

                Spacer(Modifier.height(12.dp))

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {

                    val leftX = size.width * 0.20f
                    val rightX = size.width * 0.80f
                    val spacing = size.height / (vertexCount + 1)

                    result?.partners?.forEachIndexed { a, b ->
                        drawLine(
                            color = green,
                            start = Offset(
                                leftX,
                                spacing * (a + 1)
                            ),
                            end = Offset(
                                rightX,
                                spacing * (b + 1)
                            ),
                            strokeWidth = 6f
                        )
                    }

                    for (i in 0 until vertexCount) {

                        val y = spacing * (i + 1)

                        drawCircle(
                            color = blue,
                            radius = 22f,
                            center = Offset(leftX, y)
                        )

                        drawCircle(
                            color = orange,
                            radius = 22f,
                            center = Offset(rightX, y)
                        )

                        drawContext.canvas.nativeCanvas.apply {

                            val paint = android.graphics.Paint().apply {
                                color = android.graphics.Color.WHITE
                                textSize = 22f
                                textAlign =
                                    android.graphics.Paint.Align.CENTER
                                isAntiAlias = true
                            }

                            drawText(
                                "A${i + 1}",
                                leftX,
                                y + 7f,
                                paint
                            )

                            drawText(
                                "B${i + 1}",
                                rightX,
                                y + 7f,
                                paint
                            )
                        }
                    }
                }
            }
        }

        result?.let { matching ->

            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(Modifier.padding(16.dp)) {

                    Text(
                        "Emparejamiento final",
                        fontSize = 20.sp
                    )

                    Spacer(Modifier.height(8.dp))

                    matching.partners.forEachIndexed { a, b ->
                        Text("A${a + 1} ↔ B${b + 1}")
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "Propuestas: ${matching.proposalCount}",
                        color = green
                    )

                    Text(
                        "Emparejamiento estable",
                        color = green
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(Modifier.padding(16.dp)) {

                    Text(
                        "Bitácora del algoritmo",
                        fontSize = 20.sp
                    )

                    Spacer(Modifier.height(8.dp))

                    matching.steps.forEach { step ->
                        Text("• $step", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

// Editor individual de preferencias.
@Composable
private fun PreferenceEditor(
    owner: String,
    targetPrefix: String,
    preferences: List<Int>,
    onMove: (Int, Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            "Orden de $owner",
            fontSize = 13.sp,
            color = Color.Gray
        )

        preferences.forEachIndexed { position, target ->

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    "${position + 1}. $targetPrefix${target + 1}",
                    fontSize = 14.sp
                )

                Row {

                    TextButton(
                        onClick = {
                            onMove(position, -1)
                        },
                        enabled = position > 0
                    ) {
                        Text("↑")
                    }

                    TextButton(
                        onClick = {
                            onMove(position, 1)
                        },
                        enabled = position < preferences.lastIndex
                    ) {
                        Text("↓")
                    }
                }
            }
        }
    }
}
