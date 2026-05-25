package com.rakizz.student.presentation.common.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.rakizz.student.R

@Composable
fun RakizzLogoIcon(
    modifier: Modifier = Modifier
) {
    // simple reusable logo component
    // this lets us use the same Rakizz icon everywhere in the app
    Image(
        painter = painterResource(id = R.drawable.rakizz_glass_icon),
        contentDescription = "Rakizz logo",
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}