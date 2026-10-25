package com.vrddhih.app.features.finance

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.vrddhih.app.R
import com.vrddhih.app.core.ui.components.FeaturePlaceholder

@Composable
fun FinanceScreen(modifier: Modifier = Modifier) {
    FeaturePlaceholder(
        eyebrow = stringResource(R.string.finance_eyebrow),
        title = stringResource(R.string.finance_title),
        icon = Icons.Outlined.AccountBalanceWallet,
        description = stringResource(R.string.finance_description),
        phaseLabel = stringResource(R.string.finance_phase),
        plannedCapabilities = stringArrayResource(R.array.finance_capabilities).toList(),
        modifier = modifier,
    )
}
