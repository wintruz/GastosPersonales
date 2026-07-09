package com.example.gastospersonales.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.gastospersonales.model.Categoria
import com.example.gastospersonales.model.Gasto
import com.example.gastospersonales.util.FormatoMoneda
import com.example.gastospersonales.util.IconosCategoria

/**
 * Fila de un gasto en la lista. Muestra el ícono de su categoría, la
 * descripción, el nombre de la categoría y el monto. Es reutilizable y no
 * conoce el ViewModel: recibe los datos y un callback de clic.
 *
 * La categoría se recibe ya resuelta (puede ser null si no se encontró,
 * en cuyo caso se muestran valores neutros).
 *
 * La descripción se limita a 2 líneas (maxLines + TextOverflow.Ellipsis):
 * si el texto no cabe, Compose la corta y agrega "…" al final, sin volver
 * la fila más alta ni sobrecargar la pantalla con la descripción de un solo
 * gasto. No se agrega ningún estado ni evento nuevo para esto: es puramente
 * visual, y el toque en la fila sigue abriendo el detalle como siempre,
 * donde la descripción si se ve completa.
 */
@Composable
fun GastoItem(
    gasto: Gasto,
    categoria: Categoria?,
    moneda: String = "USD",
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Ícono de la categoría dentro de un cuadro suave.
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = IconosCategoria.desde(categoria?.icono ?: "dots"),
                contentDescription = categoria?.nombre ?: "Sin categoría",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }

        // Descripción + categoría.
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = gasto.descripcion,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = categoria?.nombre ?: "Sin categoría",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.width(8.dp))

        // Monto.
        Text(
            text = FormatoMoneda.formatear(gasto.monto, moneda),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}