package com.weijia.chatbuddy.models

import com.weijia.chatbuddy.ChatBuddyApplication
import com.weijia.chatbuddy.viewmodels.ChatViewModel

object DependencyContainer {
    fun provideChatViewModel(application: ChatBuddyApplication): ChatViewModel {
        return ChatViewModel(application.database.chatMessageDao())
    }
}