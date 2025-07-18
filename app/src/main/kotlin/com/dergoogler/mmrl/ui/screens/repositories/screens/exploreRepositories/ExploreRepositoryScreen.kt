package com.dergoogler.mmrl.ui.screens.repositories.screens.exploreRepositories

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.model.online.ExploreRepository
import com.dergoogler.mmrl.ui.component.Cover
import com.dergoogler.mmrl.ui.component.HorizontalDividerWithText
import com.dergoogler.mmrl.ui.component.NavigateUpTopBar
import com.dergoogler.mmrl.ui.component.listItem.ListButtonItem
import com.dergoogler.mmrl.ui.component.listItem.ListItem
import com.dergoogler.mmrl.ui.providable.LocalNavController
import com.dergoogler.mmrl.ui.providable.LocalPanicArguments
import com.dergoogler.mmrl.ui.providable.LocalSnackbarHost
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.dergoogler.mmrl.ui.screens.repositories.screens.exploreRepositories.items.MemberCard
import com.dergoogler.mmrl.ui.screens.repositories.screens.main.FailureDialog
import com.dergoogler.mmrl.ext.none
import com.dergoogler.mmrl.ext.panicMoshiParcelable
import com.dergoogler.mmrl.viewmodel.RepositoriesViewModel
import dev.dergoogler.mmrl.compat.core.LocalUriHandler
import com.dergoogler.mmrl.ext.fadingEdge
import com.dergoogler.mmrl.ext.fillWidthOfParent
import com.dergoogler.mmrl.ext.isNotNullOrEmpty
import com.dergoogler.mmrl.ext.nullable
import com.dergoogler.mmrl.ext.systemBarsPaddingEnd
import com.dergoogler.mmrl.ext.toDecodedUrl
import com.dergoogler.mmrl.ui.component.listItem.dsl.List
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.ButtonItem
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.Item
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Title
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Icon
import com.dergoogler.mmrl.ui.component.scaffold.Scaffold
import kotlinx.coroutines.launch

val listItemContentPaddingValues = PaddingValues(vertical = 16.dp, horizontal = 16.dp)

@Composable
fun ExploreRepositoryScreen(
    viewModel: RepositoriesViewModel = hiltViewModel(),
) {
    val scope = rememberCoroutineScope()
    val arguments = LocalPanicArguments.current
    val snackbarHostState = LocalSnackbarHost.current
    val navController = LocalNavController.current
    val repo = arguments.panicMoshiParcelable<ExploreRepository>("repo")
    val browser = LocalUriHandler.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val userPreferences = LocalUserPreferences.current
    val repositoriesMenu = userPreferences.repositoriesMenu

    val context = LocalContext.current

    var failure by remember { mutableStateOf(false) }
    var message: String by remember { mutableStateOf("") }

    if (failure) FailureDialog(
        name = repo.url,
        message = message,
        onClose = {
            failure = false
            message = ""
        })

    val onAdd: () -> Unit = {
        viewModel.insert(url = repo.url, onSuccess = {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.repo_added),
                    duration = SnackbarDuration.Short
                )
            }
        }, onFailure = { e ->
            failure = true
            message = e.stackTraceToString()
        })
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            NavigateUpTopBar(
                colors = TopAppBarDefaults.topAppBarColors().copy(
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    containerColor = Color.Transparent
                ),
                title = "",
                scrollBehavior = scrollBehavior,
                navController = navController,
            )
        },
        contentWindowInsets = WindowInsets.none
    ) { innerPadding ->
        this@Scaffold.ResponsiveContent {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item(span = { GridItemSpan(2) }) {
                    List(
                        modifier = Modifier
                            .let {
                                if (repositoriesMenu.showCover && repo.hasCover) {
                                    Modifier
                                } else {
                                    it.padding(innerPadding)
                                }
                            }
                            .fillMaxWidth()
                            .fillWidthOfParent(16.dp)
                    ) {
                        repo.cover.nullable(repositoriesMenu.showCover) {
                            if (it.isNotEmpty()) {
                                Cover(
                                    modifier = Modifier.fadingEdge(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black
                                            ),
                                            startY = Float.POSITIVE_INFINITY,
                                            endY = 0f
                                        )
                                    ),
                                    url = it,
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .systemBarsPaddingEnd()
                        ) {

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {

                                    Text(
                                        text = repo.name.toDecodedUrl(),
                                        style = MaterialTheme.typography.titleLarge,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        modifier = Modifier.clickable(
                                            onClick = {
                                                browser.openUri(repo.url)
                                            }
                                        ),
                                        text = repo.url,
                                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.surfaceTint),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                repo.submission.nullable {
                                    OutlinedButton(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f),
                                        onClick = {
                                            browser.openUri(it)
                                        }
                                    ) {
                                        Text(
                                            text = stringResource(
                                                id = R.string.submit_module
                                            ),
                                            maxLines = 1
                                        )
                                    }
                                }

                                Button(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    onClick = onAdd
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.repo_add_dialog_add),
                                        maxLines = 1
                                    )
                                }
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 16.dp),
                                thickness = 0.9.dp
                            )


                            repo.description.nullable {
                                this@List.Item {
                                    Title(R.string.about_this_repository)
                                }

                                Text(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    text = it.toDecodedUrl(force = true),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            List {
                                repo.modulesCount.nullable {
                                    Item {
                                        Icon(painter = painterResource(id = R.drawable.keyframes))
                                        Title(
                                            pluralStringResource(
                                                id = R.plurals.module_count_explore_repo,
                                                count = it,
                                                it
                                            )
                                        )
                                    }
                                }

                                repo.donate.nullable {
                                    ButtonItem(
                                        onClick = {
                                            browser.openUri(it)
                                        }
                                    ) {
                                        Icon(painter = painterResource(id = R.drawable.currency_dollar))
                                        Title(R.string.view_module_donate)
                                    }
                                }
                            }
                        }
                    }
                }

                if (repo.members.isNotNullOrEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        HorizontalDividerWithText(
                            text = stringResource(R.string.team),
                            thickness = 0.9.dp
                        )
                    }

                    items(
                        items = repo.members,
                        key = { it.name }
                    ) {
                        MemberCard(member = it)
                    }
                }
            }
        }
    }
}