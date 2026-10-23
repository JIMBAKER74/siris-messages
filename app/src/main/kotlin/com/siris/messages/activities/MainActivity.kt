package com.siris.messages.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.provider.Telephony
import android.text.TextUtils
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.siris.mobile.messages.common.extensions.*
import com.siris.mobile.messages.common.extensions.isQPlus
import com.siris.mobile.messages.common.helpers.*
import com.siris.mobile.messages.common.models.FAQItem
import com.siris.mobile.messages.common.models.Release
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import com.siris.messages.BuildConfig
import com.siris.messages.R
import com.siris.messages.adapters.ConversationsAdapter
import com.siris.messages.adapters.SearchResultsAdapter
import com.siris.messages.databinding.ActivityMainBinding
import com.siris.messages.extensions.*
import com.siris.messages.helpers.SEARCHED_MESSAGE_ID
import com.siris.messages.helpers.THREAD_ID
import com.siris.messages.helpers.THREAD_TITLE
import com.siris.messages.models.Conversation
import com.siris.messages.models.Events
import com.siris.messages.models.Message
import com.siris.messages.models.SearchResult

class MainActivity : SimpleActivity(TAG = "${MainActivity::class.simpleName}") {
    private val MAKE_DEFAULT_APP_REQUEST = 1

    private var storedTextColor = 0
    private var storedFontSize = 0
    private var lastSearchedText = ""
    private var bus: EventBus? = null
    private var wasProtectionHandled = false

    private val binding by viewBinding(ActivityMainBinding::inflate)

    @SuppressLint("InlinedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        onAllPermissionsGranted = {
            initMessenger()
            bus = EventBus.getDefault()
            try {
                bus!!.register(this)
            } catch (ignored: Exception) {
            }
        }

        isMaterialActivity = true
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        appLaunched(BuildConfig.APPLICATION_ID)
        setupOptionsMenu()
        refreshMenuItems()

        updateMaterialActivityViews(
            mainCoordinatorLayout = binding.mainCoordinator,
            nestedView = binding.conversationsList,
            useTransparentNavigation = true,
            useTopSearchMenu = true
        )

        if (savedInstanceState == null) {
            checkAndDeleteOldRecycleBinMessages()
            handleAppPasswordProtection {
                wasProtectionHandled = it
                if (it) {
                    clearAllMessagesIfNeeded {
                        loadMessages()
                    }
                } else {
                    finish()
                }
            }
        }

        if (checkAppSideloading()) {
            return
        }
    }

    override fun onResume() {
        super.onResume()
        LOG_DEBUG("onResume()")
        updateMenuColors()
        refreshMenuItems()

        getOrCreateConversationsAdapter().apply {
            if (storedTextColor != getProperTextColor()) {
                updateTextColor(getProperTextColor())
            }

            if (storedFontSize != config.fontSize) {
                updateFontSize()
            }

            updateDrafts()
        }

        updateTextColors(binding.mainCoordinator)
        binding.searchHolder.setBackgroundColor(getProperBackgroundColor())

        val properPrimaryColor = getProperPrimaryColor()
        binding.noConversationsPlaceholder2.setTextColor(properPrimaryColor)
        binding.noConversationsPlaceholder2.underlineText()
        binding.conversationsFastscroller.updateColors(properPrimaryColor)
        binding.conversationsProgressBar.setIndicatorColor(properPrimaryColor)
        binding.conversationsProgressBar.trackColor = properPrimaryColor.adjustAlpha(LOWER_ALPHA)
        checkShortcut()
        (binding.conversationsFab.layoutParams as? CoordinatorLayout.LayoutParams)?.bottomMargin =
            navigationBarHeight + resources.getDimension(com.siris.mobile.messages.common.R.dimen.activity_margin).toInt()
    }

    override fun onPause() {
        super.onPause()
        LOG_DEBUG("onPause()")
        storeStateVariables()
    }

    override fun onDestroy() {
        super.onDestroy()
        LOG_DEBUG("onDestroy()")
        bus?.unregister(this)
    }

    override fun onBackPressed() {
        LOG_DEBUG("onBackPressed()")
        if (binding.mainMenu.isSearchOpen) {
            binding.mainMenu.closeSearch()
        } else {
            super.onBackPressed()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        LOG_DEBUG("onSaveInstanceState(outState=$outState)")
        outState.putBoolean(WAS_PROTECTION_HANDLED, wasProtectionHandled)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        LOG_DEBUG("onRestoreInstanceState(savedInstanceState=$savedInstanceState)")
        wasProtectionHandled = savedInstanceState.getBoolean(WAS_PROTECTION_HANDLED, false)

        if (!wasProtectionHandled) {
            handleAppPasswordProtection {
                wasProtectionHandled = it
                if (it) {
                    loadMessages()
                } else {
                    finish()
                }
            }
        } else {
            loadMessages()
        }
    }

    private fun setupOptionsMenu() {
        LOG_DEBUG("setupOptionsMenu()")

        binding.mainMenu.getToolbar().inflateMenu(R.menu.menu_main)
        binding.mainMenu.toggleHideOnScroll(true)
        binding.mainMenu.setupMenu()

        binding.mainMenu.onSearchClosedListener = {
            fadeOutSearch()
        }

        binding.mainMenu.onSearchTextChangedListener = { text ->
            if (text.isNotEmpty()) {
                if (binding.searchHolder.alpha < 1f) {
                    binding.searchHolder.fadeIn()
                }
            } else {
                fadeOutSearch()
            }
            searchTextChanged(text)
        }

        binding.mainMenu.getToolbar().setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.show_recycle_bin -> launchRecycleBin()
                R.id.show_archived -> launchArchivedConversations()
                R.id.settings -> launchSettings()
                R.id.about -> launchAbout()
                else -> return@setOnMenuItemClickListener false
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun refreshMenuItems() {
        LOG_DEBUG("refreshMenuItems()")
        binding.mainMenu.getToolbar().menu.apply {
            findItem(R.id.show_recycle_bin).isVisible = config.useRecycleBin
            findItem(R.id.show_archived).isVisible = config.isArchiveAvailable
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        LOG_DEBUG("onActivityResult(requestCode=$requestCode, resultCode=$resultCode, resultData=$resultData)")

        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == MAKE_DEFAULT_APP_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                requestPermissions()
            } else {
                finish()
            }
        }
    }

    /**
     * Saves current global state variables to locale variables.
     */
    private fun storeStateVariables() {
        LOG_DEBUG("storeStateVariables()")
        storedTextColor = getProperTextColor()
        storedFontSize = config.fontSize
    }

    private fun updateMenuColors() {
        LOG_DEBUG("updateMenuColors()")
        updateStatusbarColor(getProperBackgroundColor())
        binding.mainMenu.updateColors()
    }

    private fun loadMessages() {
        if (isQPlus()) {
            LOG_DEBUG("loadMessages(): isQPlus")
            val roleManager = getSystemService(RoleManager::class.java)
            if (roleManager!!.isRoleAvailable(RoleManager.ROLE_SMS)) {
                if (roleManager.isRoleHeld(RoleManager.ROLE_SMS)) {
                    requestPermissions()
                } else {
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
                    startActivityForResult(intent, MAKE_DEFAULT_APP_REQUEST)
                }
            } else {
                toast(com.siris.mobile.messages.common.R.string.unknown_error_occurred)
                finish()
            }
        } else {
            LOG_DEBUG("loadMessages()")
            if (Telephony.Sms.getDefaultSmsPackage(this) == packageName) {
                requestPermissions()
            } else {
                val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName)
                startActivityForResult(intent, MAKE_DEFAULT_APP_REQUEST)
            }
        }
    }

    private fun initMessenger() {
        LOG_DEBUG("initMessenger()")
        checkWhatsNewDialog()
        storeStateVariables()
        getCachedConversations()

        binding.noConversationsPlaceholder2.setOnClickListener {
            launchNewConversation()
        }

        /**
         * This is the plus sign on the bottom right of the screen. Clicking this button allows a new conversation to be started.
         */
        binding.conversationsFab.setOnClickListener {
            launchNewConversation()
        }
    }

    private fun getCachedConversations() {
        LOG_DEBUG("getCachedConversations()")
        ensureBackgroundThread {
            val conversations = try {
                conversationsDB.getNonArchived().toMutableList() as ArrayList<Conversation>
            } catch (e: Exception) {
                ArrayList()
            }

            val archived = try {
                conversationsDB.getAllArchived()
            } catch (e: Exception) {
                listOf()
            }

            updateUnreadCountBadge(conversations)
            runOnUiThread {
                setupConversations(conversations, cached = true)
                getNewConversations((conversations + archived).toMutableList() as ArrayList<Conversation>)
            }
            conversations.forEach {
                clearExpiredScheduledMessages(it.threadId)
            }
        }
    }

    private fun getNewConversations(cachedConversations: ArrayList<Conversation>) {
        LOG_DEBUG("getNewConversations(cachedConversations=$cachedConversations)")

        val privateCursor = getMyContactsCursor(favoritesOnly = false, withPhoneNumbersOnly = true)
        ensureBackgroundThread {
            val privateContacts = MyContactsContentProvider.getSimpleContacts(this, privateCursor)
            val conversations = getConversations(privateContacts = privateContacts)

            conversations.forEach { clonedConversation ->
                val threadIds = cachedConversations.map { it.threadId }
                if (!threadIds.contains(clonedConversation.threadId)) {
                    conversationsDB.insertOrUpdate(clonedConversation)
                    cachedConversations.add(clonedConversation)
                }
            }

            cachedConversations.forEach { cachedConversation ->
                val threadId = cachedConversation.threadId

                val isTemporaryThread = cachedConversation.isScheduled
                val isConversationDeleted = !conversations.map { it.threadId }.contains(threadId)
                if (isConversationDeleted && !isTemporaryThread) {
                    conversationsDB.deleteThreadId(threadId)
                }

                val newConversation = conversations.find { it.phoneNumber == cachedConversation.phoneNumber }
                if (isTemporaryThread && newConversation != null) {
                    // delete the original temporary thread and move any scheduled messages to the new thread
                    conversationsDB.deleteThreadId(threadId)
                    messagesDB.getScheduledThreadMessages(threadId)
                        .forEach { message ->
                            messagesDB.insertOrUpdate(message.copy(threadId = newConversation.threadId))
                        }
                    insertOrUpdateConversation(newConversation, cachedConversation)
                }
            }

            cachedConversations.forEach { cachedConv ->
                val conv = conversations.find {
                    it.threadId == cachedConv.threadId && !Conversation.areContentsTheSame(cachedConv, it)
                }
                if (conv != null) {
                    val lastModified = maxOf(cachedConv.date, conv.date)
                    val conversation = conv.copy(date = lastModified)
                    insertOrUpdateConversation(conversation)
                }
            }

            val allConversations = conversationsDB.getNonArchived() as ArrayList<Conversation>
            runOnUiThread {
                setupConversations(allConversations)
            }

            if (config.appRunCount == 1) {
                conversations.map { it.threadId }.forEach { threadId ->
                    val messages = getMessages(threadId, getImageResolutions = false, includeScheduledMessages = false)
                    messages.chunked(30).forEach { currentMessages ->
                        messagesDB.insertMessages(*currentMessages.toTypedArray())
                    }
                }
            }
        }
    }

    private fun getOrCreateConversationsAdapter(): ConversationsAdapter {
        LOG_DEBUG("getOrCreateConversationsAdapter()")

        var currAdapter = binding.conversationsList.adapter
        if (currAdapter == null) {
            hideKeyboard()
            currAdapter = ConversationsAdapter(
                activity = this,
                recyclerView = binding.conversationsList,
                onRefresh = { notifyDatasetChanged() },
                itemClick = { handleConversationClick(it) }
            )

            binding.conversationsList.adapter = currAdapter
            if (areSystemAnimationsEnabled) {
                binding.conversationsList.scheduleLayoutAnimation()
            }
        }
        return currAdapter as ConversationsAdapter
    }

    private fun setupConversations(conversations: ArrayList<Conversation>, cached: Boolean = false) {
        LOG_DEBUG("setupConversations(conversations.size=${conversations.size}, cached=$cached)")

        val sortedConversations = conversations.sortedWith(
            compareByDescending<Conversation> { config.pinnedConversations.contains(it.threadId.toString()) }
                .thenByDescending { it.date }
        ).toMutableList() as ArrayList<Conversation>

        if (cached && config.appRunCount == 1) {
            // there are no cached conversations on the first run so we show the loading placeholder and progress until we are done loading from telephony
            showOrHideProgress(conversations.isEmpty())
        } else {
            showOrHideProgress(false)
            showOrHidePlaceholder(conversations.isEmpty())
        }

        try {
            getOrCreateConversationsAdapter().apply {
                updateConversations(sortedConversations) {
                    if (!cached) {
                        showOrHidePlaceholder(currentList.isEmpty())
                    }
                }
            }
        } catch (ignored: Exception) {
        }
    }

    private fun showOrHideProgress(show: Boolean) {
        LOG_DEBUG("showOrHideProgress(show=$show)")
        if (show) {
            binding.conversationsProgressBar.show()
            binding.noConversationsPlaceholder.beVisible()
            binding.noConversationsPlaceholder.text = getString(R.string.loading_messages)
        } else {
            binding.conversationsProgressBar.hide()
            binding.noConversationsPlaceholder.beGone()
        }
    }

    private fun showOrHidePlaceholder(show: Boolean) {
        LOG_DEBUG("showOrHidePlaceholder(show=$show)")
        binding.conversationsFastscroller.beGoneIf(show)
        binding.noConversationsPlaceholder.beVisibleIf(show)
        binding.noConversationsPlaceholder.text = getString(R.string.no_conversations_found)
        binding.noConversationsPlaceholder2.beVisibleIf(show)
    }

    private fun fadeOutSearch() {
        LOG_DEBUG("fadeOutSearch()")
        binding.searchHolder.animate().alpha(0f).setDuration(SHORT_ANIMATION_DURATION).withEndAction {
            binding.searchHolder.beGone()
            searchTextChanged("", true)
        }.start()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun notifyDatasetChanged() {
        LOG_DEBUG("notifyDatasetChanged()")
        getOrCreateConversationsAdapter().notifyDataSetChanged()
    }

    /**
     * Launches [ThreadActivity] which is displays the conversation history (the actual chat screen).
     */
    private fun handleConversationClick(any: Any) {
        LOG_DEBUG("handleConversationClick(any=$any)")

        Intent(this, ThreadActivity::class.java).apply {
            val conversation = any as Conversation
            putExtra(THREAD_ID, conversation.threadId)
            putExtra(THREAD_TITLE, conversation.title)
            putExtra(WAS_PROTECTION_HANDLED, wasProtectionHandled)
            startActivity(this)
        }
    }

    private fun launchNewConversation() {
        LOG_DEBUG("launchNewConversation()")
        hideKeyboard()
        Intent(this, NewConversationActivity::class.java).apply {
            startActivity(this)
        }
    }

    @SuppressLint("NewApi")
    private fun checkShortcut() {
        LOG_DEBUG("checkShortcut()")
        val appIconColor = config.appIconColor
        if (isNougatMR1Plus() && config.lastHandledShortcutColor != appIconColor) {
            val newConversation = getCreateNewContactShortcut(appIconColor)

            val manager = getSystemService(ShortcutManager::class.java)
            try {
                manager.dynamicShortcuts = listOf(newConversation)
                config.lastHandledShortcutColor = appIconColor
            } catch (ignored: Exception) {
            }
        }
    }

    @SuppressLint("NewApi")
    private fun getCreateNewContactShortcut(appIconColor: Int): ShortcutInfo {
        LOG_DEBUG("getCreateNewContactShortcut(appIconColor=$appIconColor)")
        val newEvent = getString(R.string.new_conversation)
        val drawable = resources.getDrawable(com.siris.mobile.messages.common.R.drawable.shortcut_plus)
        (drawable as LayerDrawable).findDrawableByLayerId(com.siris.mobile.messages.common.R.id.shortcut_plus_background).applyColorFilter(appIconColor)
        val bmp = drawable.convertToBitmap()

        val intent = Intent(this, NewConversationActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        return ShortcutInfo.Builder(this, "new_conversation")
            .setShortLabel(newEvent)
            .setLongLabel(newEvent)
            .setIcon(Icon.createWithBitmap(bmp))
            .setIntent(intent)
            .build()
    }

    private fun searchTextChanged(text: String, forceUpdate: Boolean = false) {
        LOG_DEBUG("searchTextChanged(text=$text, forceUpdate=$forceUpdate)")
        if (!binding.mainMenu.isSearchOpen && !forceUpdate) {
            return
        }

        lastSearchedText = text
        binding.searchPlaceholder2.beGoneIf(text.length >= 2)
        if (text.length >= 2) {
            ensureBackgroundThread {
                val searchQuery = "%$text%"
                val messages = messagesDB.getMessagesWithText(searchQuery)
                val conversations = conversationsDB.getConversationsWithText(searchQuery)
                if (text == lastSearchedText) {
                    showSearchResults(messages, conversations, text)
                }
            }
        } else {
            binding.searchPlaceholder.beVisible()
            binding.searchResultsList.beGone()
        }
    }

    private fun showSearchResults(messages: List<Message>, conversations: List<Conversation>, searchedText: String) {
        LOG_DEBUG("showSearchResults(messages=$messages, conversations=$conversations, searchedText=$searchedText)")

        val searchResults = ArrayList<SearchResult>()
        conversations.forEach { conversation ->
            val date = (conversation.date * 1000L).formatDateOrTime(this, true, true)
            val searchResult = SearchResult(-1, conversation.title, conversation.phoneNumber, date, conversation.threadId, conversation.photoUri)
            searchResults.add(searchResult)
        }

        messages.sortedByDescending { it.id }.forEach { message ->
            var recipient = message.senderName
            if (recipient.isEmpty() && message.participants.isNotEmpty()) {
                val participantNames = message.participants.map { it.name }
                recipient = TextUtils.join(", ", participantNames)
            }

            val date = (message.date * 1000L).formatDateOrTime(this, true, true)
            val searchResult = SearchResult(message.id, recipient, message.body, date, message.threadId, message.senderPhotoUri)
            searchResults.add(searchResult)
        }

        runOnUiThread {
            binding.searchResultsList.beVisibleIf(searchResults.isNotEmpty())
            binding.searchPlaceholder.beVisibleIf(searchResults.isEmpty())

            val currAdapter = binding.searchResultsList.adapter
            if (currAdapter == null) {
                SearchResultsAdapter(this, searchResults, binding.searchResultsList, searchedText) {
                    hideKeyboard()
                    Intent(this, ThreadActivity::class.java).apply {
                        putExtra(THREAD_ID, (it as SearchResult).threadId)
                        putExtra(THREAD_TITLE, it.title)
                        putExtra(SEARCHED_MESSAGE_ID, it.messageId)
                        startActivity(this)
                    }
                }.apply {
                    binding.searchResultsList.adapter = this
                }
            } else {
                (currAdapter as SearchResultsAdapter).updateItems(searchResults, searchedText)
            }
        }
    }

    private fun launchRecycleBin() {
        LOG_DEBUG("launchRecycleBin()")
        hideKeyboard()
        startActivity(Intent(applicationContext, RecycleBinConversationsActivity::class.java))
    }

    private fun launchArchivedConversations() {
        LOG_DEBUG("launchArchivedConversations()")
        hideKeyboard()
        startActivity(Intent(applicationContext, ArchivedConversationsActivity::class.java))
    }

    private fun launchSettings() {
        LOG_DEBUG("launchSettings()")
        hideKeyboard()
        startActivity(Intent(applicationContext, SettingsActivity::class.java))
    }

    private fun launchAbout() {
        LOG_DEBUG("launchAbout()")
        val licenses = LICENSE_EVENT_BUS or LICENSE_SMS_MMS or LICENSE_INDICATOR_FAST_SCROLL

        val faqItems = arrayListOf(
            FAQItem(R.string.faq_2_title, R.string.faq_2_text),
            FAQItem(R.string.faq_3_title, R.string.faq_3_text),
            FAQItem(com.siris.mobile.messages.common.R.string.faq_9_title_commons, com.siris.mobile.messages.common.R.string.faq_9_text_commons)
        )

        if (!resources.getBoolean(com.siris.mobile.messages.common.R.bool.hide_google_relations)) {
            faqItems.add(FAQItem(com.siris.mobile.messages.common.R.string.faq_2_title_commons, com.siris.mobile.messages.common.R.string.faq_2_text_commons))
            faqItems.add(FAQItem(com.siris.mobile.messages.common.R.string.faq_6_title_commons, com.siris.mobile.messages.common.R.string.faq_6_text_commons))
        }

        startAboutActivity(R.string.app_name, licenses, BuildConfig.VERSION_NAME, faqItems, true)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun refreshMessages(event: Events.RefreshMessages) {
        LOG_DEBUG("refreshMessages(event=$event)")
        initMessenger()
    }

    /**
     * Checks to see if there is a new app version released (meaning there exists release version greater then the current app version). If
     * there does exist new versions, show a Dialog telling user about new version.
     */
    private fun checkWhatsNewDialog() {
        LOG_DEBUG("checkWhatsNewDialog()")
        arrayListOf<Release>().apply {
            checkWhatsNew(this, BuildConfig.VERSION_CODE)
        }
    }
}
