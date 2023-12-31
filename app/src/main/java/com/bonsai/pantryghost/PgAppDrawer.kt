package com.bonsai.pantryghost

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.bonsai.pantryghost.utils.Gaps
import com.bonsai.pantryghost.utils.Paddings
import kotlinx.coroutines.launch

@Composable
fun PgAppDrawer(
    navController: NavHostController = rememberNavController(),
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                drawerState = drawerState,
                menuItems = DrawerParams.drawerButtons,
                defaultPick = NavRoute.HomeRoute
            ) { onUserPickedOption ->
                navController.navigate(onUserPickedOption.name)
            }
        }
    ) {
        PgNavHost(
            navController = navController,
            drawerState = drawerState
        )
    }
}

// T for generic type to be used for the picking
@Composable
fun AppDrawerContent(
    drawerState: DrawerState,
    menuItems: List<AppDrawerItemInfo>,
    defaultPick: NavRoute,
    onClick: (NavRoute) -> Unit
) {
    // default home destination to avoid duplication
    var currentPick by remember { mutableStateOf(defaultPick) }
    val coroutineScope = rememberCoroutineScope()

    ModalDrawerSheet {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // header image on top of the drawer
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "Main app icon",
                    modifier = Modifier.size(dimensionResource(R.dimen.app_icon_size))
                )
                // column of options to pick from for user
                LazyColumn(
                    modifier = Modifier.padding(horizontal = Paddings.small()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // generates on demand the required composables
                    items(menuItems) { item ->
                        // custom UI representation of the button
                        AppDrawerItem(item = item) { navOption ->

                            // if it is the same - ignore the click
                            if (currentPick == navOption) {
                                return@AppDrawerItem
                            }

                            currentPick = navOption

                            // close the drawer after clicking the option
                            coroutineScope.launch {
                                drawerState.close()
                            }

                            // navigate to the required screen
                            onClick(navOption)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppDrawerItem(item: AppDrawerItemInfo, onClick: (options: NavRoute) -> Unit) =
    // making surface clickable causes to show the appropriate splash animation
    Surface(
        color = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.width(dimensionResource(R.dimen.app_icon_size)),
        onClick = { onClick(item.drawerOption) },
        shape = RoundedCornerShape(50),
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(Paddings.medium())
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                modifier = Modifier.size(dimensionResource(R.dimen.icon_size_medium))
            )
            Spacer(modifier = Modifier.width(Gaps.large()))
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
        }
    }

// base data container for the button creation
// takes in the resources IDs
data class AppDrawerItemInfo(
    val drawerOption: NavRoute,
    val title: String,
    val icon: ImageVector,
)

// list of the buttons
object DrawerParams {
    val drawerButtons = arrayListOf(
        AppDrawerItemInfo(
            NavRoute.HomeRoute,
            "Home",
            Icons.Filled.Home,
        ),
        AppDrawerItemInfo(
            NavRoute.FoodRoute,
            "Foods",
            Icons.Filled.ShoppingCart,
        ),
        AppDrawerItemInfo(
            NavRoute.DayRoute,
            "Day",
            Icons.Filled.Favorite,
        ),
    )
}