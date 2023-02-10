package com.androiddev.social.ui

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Boosted(boostedBy: String?) {
    boostedBy?.let {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .background(colorScheme.primary.copy(alpha = .6f))
                .fillMaxWidth()
        ) {
            AssistChip(
                border = AssistChipDefaults.assistChipBorder(borderColor = colorScheme.tertiary),
                colors = AssistChipDefaults.assistChipColors(
                    leadingIconContentColor = colorScheme.secondary.copy(
                        alpha = .5f
                    ), labelColor = colorScheme.secondary.copy(alpha = .5f)
                ),
                shape = RoundedCornerShape(50, 50, 50, 50),
                onClick = { /* Do something! */ },
                label = { Text(boostedBy) },
                leadingIcon = {
                    Avatar(size = 24.dp)
                },
                trailingIcon = {
                    Image(
                        modifier = Modifier.height(24.dp),
                        painter = painterResource(R.drawable.rocket2),
                        contentDescription = "",
                        colorFilter = ColorFilter.tint(Color.White.copy(alpha = .5f)),
                    )
                }
            )

        }
    }
}