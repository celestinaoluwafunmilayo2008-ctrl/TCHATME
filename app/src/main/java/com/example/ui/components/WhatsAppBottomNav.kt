package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CircleNotifications
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.CircleNotifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WhatsAppGreenDark
import com.example.ui.theme.WhatsAppGreenLight
import com.example.ui.theme.WhatsAppGreenPrimary
import com.example.ui.viewmodel.HomeTab

@Composable
fun WhatsAppBottomNav(
    currentTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
    unreadChatCount: Int = 0,
    hasUnseenStatus: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.navigationBarsPadding()
        ) {
            // 1. Chats Tab
            NavigationBarItem(
                selected = currentTab == HomeTab.CHATS,
                onClick = { onTabSelected(HomeTab.CHATS) },
                icon = {
                    BadgedBox(
                        badge = {
                            if (unreadChatCount > 0) {
                                Badge(
                                    containerColor = WhatsAppGreenPrimary,
                                    contentColor = Color.White
                                ) {
                                    Text(
                                        text = if (unreadChatCount > 99) "99+" else unreadChatCount.toString(),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (currentTab == HomeTab.CHATS) Icons.Filled.Chat else Icons.Outlined.Chat,
                            contentDescription = "Chats"
                        )
                    }
                },
                label = {
                    Text(
                        text = "Chats",
                        fontWeight = if (currentTab == HomeTab.CHATS) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = WhatsAppGreenDark,
                    selectedTextColor = WhatsAppGreenDark,
                    indicatorColor = Color(0xFFD9FDD3),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag("nav_chats_tab")
            )

            // 2. Updates / Status Tab
            NavigationBarItem(
                selected = currentTab == HomeTab.UPDATES,
                onClick = { onTabSelected(HomeTab.UPDATES) },
                icon = {
                    BadgedBox(
                        badge = {
                            if (hasUnseenStatus) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(WhatsAppGreenPrimary, CircleShape)
                                )
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (currentTab == HomeTab.UPDATES) Icons.Filled.CircleNotifications else Icons.Outlined.CircleNotifications,
                            contentDescription = "Updates"
                        )
                    }
                },
                label = {
                    Text(
                        text = "Updates",
                        fontWeight = if (currentTab == HomeTab.UPDATES) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = WhatsAppGreenDark,
                    selectedTextColor = WhatsAppGreenDark,
                    indicatorColor = Color(0xFFD9FDD3),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag("nav_updates_tab")
            )

            // 3. Communities Tab
            NavigationBarItem(
                selected = currentTab == HomeTab.COMMUNITIES,
                onClick = { onTabSelected(HomeTab.COMMUNITIES) },
                icon = {
                    Icon(
                        imageVector = if (currentTab == HomeTab.COMMUNITIES) Icons.Filled.Groups else Icons.Filled.Groups,
                        contentDescription = "Communities"
                    )
                },
                label = {
                    Text(
                        text = "Communities",
                        fontWeight = if (currentTab == HomeTab.COMMUNITIES) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = WhatsAppGreenDark,
                    selectedTextColor = WhatsAppGreenDark,
                    indicatorColor = Color(0xFFD9FDD3),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag("nav_communities_tab")
            )

            // 4. Calls Tab
            NavigationBarItem(
                selected = currentTab == HomeTab.CALLS,
                onClick = { onTabSelected(HomeTab.CALLS) },
                icon = {
                    Icon(
                        imageVector = if (currentTab == HomeTab.CALLS) Icons.Filled.Call else Icons.Outlined.Call,
                        contentDescription = "Calls"
                    )
                },
                label = {
                    Text(
                        text = "Calls",
                        fontWeight = if (currentTab == HomeTab.CALLS) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = WhatsAppGreenDark,
                    selectedTextColor = WhatsAppGreenDark,
                    indicatorColor = Color(0xFFD9FDD3),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag("nav_calls_tab")
            )
        }
    }
}
