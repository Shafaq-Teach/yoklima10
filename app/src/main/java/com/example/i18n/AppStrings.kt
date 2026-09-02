package com.example.i18n

object AppStrings {
    fun get(lang: Language): LocalizedStrings {
        return when (lang) {
            Language.UYGHUR -> UyghurStrings
            Language.ARABIC -> ArabicStrings
            Language.ENGLISH -> EnglishStrings
        }
    }
}

interface LocalizedStrings {
    val appTitle: String
    val appSubtitle: String
    val loginTitle: String
    val loginSubtitle: String
    val roleAdmin: String
    val roleGroupLead: String
    val loginAsAdmin: String
    val loginAsLead: String
    val username: String
    val password: String
    val loginButton: String
    val logoutButton: String
    val quickDemoLogin: String
    val selectRoleHint: String
    val loginError: String

    // Group Lead Screen
    val todayAttendance: String
    val memberRoster: String
    val attendanceHistory: String
    val groupHistory: String
    val markAllPresent: String
    val markAllPresentSuccess: String
    val date: String
    val selectDate: String
    val today: String
    val yesterday: String
    val totalMembers: String
    val presentCount: String
    val absentCount: String
    val lateCount: String
    val excusedCount: String
    val attendanceRate: String
    val notes: String
    val addNote: String
    val editNote: String
    val save: String
    val cancel: String
    val status: String

    // Sub-groups & Leaders
    val subGroup1: String
    val subGroup2: String
    val subGroup1Leader: String
    val subGroup2Leader: String
    val subGroup1LeaderLabel: String
    val subGroup2LeaderLabel: String
    val memberCountLabel: String
    val subGroupSelection: String
    val groupLeader: String
    val groupLeaderPlaceholder: String
    val contactAddress: String
    val phoneContact: String
    val telegramContact: String
    val whatsappContact: String
    val openTelegram: String
    val openWhatsapp: String
    val openWhatsApp: String get() = openWhatsapp
    val callPhone: String

    // Statuses
    val statusPresent: String
    val statusAbsent: String
    val statusLate: String
    val statusExcused: String

    // Members
    val addMember: String
    val addMemberTitle: String
    val editMember: String
    val deleteMember: String
    val memberName: String
    val selectSubGroup: String
    val memberStatus: String
    val statusActive: String
    val statusInactive: String
    val joinDate: String
    val noMembersInGroup: String
    val deleteMemberConfirmTitle: String
    val deleteMemberConfirmDesc: String
    val searchMemberPlaceholder: String

    // Admin Dashboard
    val adminDashboardTitle: String
    val adminSettingsTitle: String
    val overview: String
    val groupsOverview: String
    val analytics: String
    val exportData: String
    val groupLeadsSettings: String
    val allGroupsAttendanceRate: String
    val totalActiveMembers: String
    val lowestGroupWarning: String
    val topAbsenteesTitle: String
    val topAbsenteesSubtitle: String
    val topLatesTitle: String
    val groupRanking: String
    val resetPassword: String
    val newPassword: String
    val resetPasswordSuccess: String
    val editCredentials: String
    val displayName: String
    val editCredentialsSuccess: String
    val editGroupTitle: String
    val groupName: String
    val groupCode: String
    val leaderLoginName: String

    // Analytics (Daily, Monthly, Quarterly)
    val dailyAnalytics: String
    val monthlyAnalytics: String
    val quarterlyAnalytics: String
    val monthlyStats: String
    val quarterlyStats: String
    val selectMonth: String
    val selectQuarter: String
    val monthUnit: String
    val quarterUnit: String
    val absenceTimes: String
    val quarter1: String
    val quarter2: String
    val quarter3: String
    val quarter4: String

    // Themes & Night mode
    val darkMode: String
    val lightMode: String
    val theme: String
    val themes: String
    val switchTheme: String
    val themeBlue: String
    val themeClassicBlue: String
    val themeEmerald: String
    val themePurple: String
    val themeAmber: String

    // Export
    val exportTitle: String
    val exportCSV: String
    val exportSummaryText: String
    val copyToClipboard: String
    val copiedToClipboard: String
    val shareReport: String
    val exportDateRange: String
    val last7Days: String
    val last30Days: String
    val allTime: String

    // Common
    val language: String
    val switchToUyghur: String
    val switchToArabic: String
    val confirm: String
    val actions: String
    val filter: String
    val all: String
    val emptyAttendanceRecord: String
    val groupLeaderCardTitle: String
    val pressAgainToExit: String
    val editLockedAfter12Hours: String
    val editLockedPastDate: String

    // Requirement 2: Active Portals & Admin Control
    val activePortals: String
    val portalStatus: String
    val statusActivePort: String
    val statusInactivePort: String
    val statusSuspendedPort: String
    val suspendPortal: String
    val resumePortal: String
    val portalSuspendedWarning: String
    val lastActive: String
    val portalControlTitle: String

    // Requirement 3: Admin Group Creation
    val addNewGroup: String
    val deleteGroup: String
    val deleteGroupConfirm: String
    val initialPassword: String
    val groupCreatedSuccess: String

    // Requirement 4: Bulk Member Import & Image Upload
    val smartBatchImport: String
    val pasteListText: String
    val uploadListImage: String
    val parseAndPreview: String
    val batchImportConfirm: String
    val batchImportSuccess: String
    val pasteFormatHint: String
    val analyzingImage: String
    val clearInput: String

    // Requirement 5: Equipment / Weapon Inventory
    val equipmentInventory: String
    val equipmentName: String
    val totalCount: String
    val readyCount: String
    val notReadyCount: String
    val addEquipment: String
    val editEquipment: String
    val deleteEquipment: String
    val deleteEquipmentConfirm: String
    val equipmentReadinessRate: String
    val equipmentNotes: String
    val allGroupsEquipment: String
    val emptyEquipment: String
    val readyBadge: String
    val notReadyBadge: String

    // Requirement 1 (New): Daily Updates & Situation Reports (يېڭىلىقلار)
    val dailyUpdatesTab: String
    val dailyUpdatesTitle: String
    val addUpdate: String
    val editUpdate: String
    val deleteUpdate: String
    val deleteUpdateConfirm: String
    val updateTitle: String
    val updateContent: String
    val updatePriority: String
    val updatePriorityNormal: String
    val updatePriorityUrgent: String
    val updatePriorityImportant: String
    val allGroupsUpdate: String
    val noUpdatesYet: String
    val author: String
    val postUpdateSuccess: String

    // Requirement 2 & 3 (New): Executive Leadership Contacts & Other Contact Field
    val executiveContactsTitle: String
    val addExecutiveContact: String
    val editExecutiveContact: String
    val deleteExecutiveContact: String
    val deleteExecutiveContactConfirm: String
    val executiveName: String
    val executiveTitle: String
    val radioCommsContact: String
    val otherContact: String
    val otherContactHint: String
    val copyRadioSuccess: String
    val copyOtherSuccess: String

    // Emergency Notice (جىددىي ئۇقتۇرۇش)
    val emergencyNoticeTitle: String
    val broadcastEmergencyNotice: String
    val emergencyNoticeReceived: String
    val emergencyNoticeDismiss: String
    val emergencyAlertBanner: String
    val sendEmergencyNotice: String get() = broadcastEmergencyNotice
    val emergencyBroadcastPrompt: String get() = "بارلىق ياكى تاللانغان گۇرۇپپىلارغا دەرھال جىددىي ئۇقتۇرۇش ئەۋەتىش"
    val targetGroupLabel: String get() = "قايسى گۇرۇپپىغا يوللىنىدۇ:"
    val emergencyNoticeSubject: String get() = "ئۇقتۇرۇش تېمىسى"
    val emergencyNoticeSubjectHint: String get() = "مەسىلەن: جىددىي يىغىلىش ياكى تەييارلىق"
    val emergencyNoticeBody: String get() = "ئۇقتۇرۇش مەزمۇنى"
    val emergencyNoticeBodyHint: String get() = "جىددىي يەتكۈزۈلمەكچى بولغان تەپسىلىي مەزمۇن..."

    // Dynamic Sub-group & Group Details
    val addSubGroup: String
    val duplicateSubGroup: String
    val subGroupUnit: String
    val groupDetailsTitle: String
    val groupDetailsSubtitle: String

    // Duty Group (نۆۋبەتچى گۇرۇپپا)
    val dutyGroupTitle: String
    val currentDutyGroup: String
    val changeDutyGroup: String
    val selectDutyGroup: String
    val dutyGroupNotes: String
    val dutyGroupReadyPersonnel: String
    val dutyGroupStatusLabel: String

    // Member Full Details (ئەزانىڭ تولۇق مەلۇماتى)
    val memberDetailsTitle: String
    val memberAttendanceSummary: String
    val presentDays: String
    val absentDays: String
    val excusedDays: String
    val recentAttendanceHistory: String

    // Password Management & Removal (مەخپىي نومۇرنى چىقىرىۋېتىش / كىرگۈزۈش)
    val removePassword: String
    val setPassword: String
    val noPasswordSet: String
    val passwordRemovedSuccess: String
    val loginWithoutPassword: String

    // AI Data Export & Custom Reports (AI ئەقلىي مەلۇمات چىقىرىش)
    val aiDataExportTitle: String
    val aiPromptLabel: String
    val aiPromptHint: String
    val geminiApiKeyLabel: String
    val generateAiExport: String
    val aiExportGenerating: String
    val aiExportResultTitle: String
    val presetPrompt1: String
    val presetPrompt2: String
    val presetPrompt3: String

    // Save Feedback (Requirement 4)
    val savedSuccess: String
    val saveFailed: String

    // Notification Tracking & Timestamps (Requirement 1 & 2)
    val notificationTrackingTitle: String
    val statusNotReceived: String
    val statusReceived: String
    val statusAcknowledged: String
    val sentTime: String
    val receivedTime: String
    val acknowledgedTime: String
    val acknowledgeNoticeBtn: String
    val acknowledgedNoticeBanner: String
    val noticeCancelled: String get() = "ئۇقتۇرۇش ئەمەلدىن قالدى"
    val cancelNotice: String get() = "ئەمەلدىن قالدۇرۇش"
    val uncancelNotice: String get() = "ئەسلىگە كەلتۈرۈش"

    // Member Search Across Groups (Requirement 3)
    val searchAllMembers: String
    val searchMemberAcrossGroups: String
    val foundMembersCount: String

    // Equipment Per Group (Requirement 7)
    val selectGroupEquipment: String
    val allGroupsEquipmentLabel: String

    // AI Multimodal & Custom Task Execution (Requirement 6)
    val aiMemberAnalysisTitle: String
    val aiCustomPromptHint: String
    val aiExecuteTask: String
    val aiExecutionSuccess: String

    // Duty Group Attendance Details & Timestamps (Requirement 8)
    val dutyGroupAttendanceDetails: String
    val attendanceSubmittedAt: String
    val attendanceNotSubmittedYet: String
    val submittedAtLabel: String get() = attendanceSubmittedAt
    val filterByGroupTitle: String get() = selectGroupEquipment
    val dutySubGroupTitle: String
    val selectDutySubGroup: String
    val designatedDutySubGroup: String
    val dutySubGroupLeader: String
    val dutySubGroupMembers: String
    val switchDutyMainGroup: String
    val savedSuccessfully: String

    // Notice cancellation & AI Member import
    val noticeCancelledStatus: String
    val cancelNoticeAction: String
    val uncancelNoticeAction: String
    val noticeCancelledBanner: String
    val aiMemberImportTitle: String
    val aiMemberImportPromptHint: String
    val runAiExtractionBtn: String
    val parsingAiWait: String

    val delete: String get() = deleteUpdate
    val allGroups: String get() = "بارلىق گۇرۇپپىلار"
    val searchMemberHint: String get() = searchMemberPlaceholder
    val telegramHint: String get() = "@username"
    val whatsappHint: String get() = "+90555..."
    val groups: String get() = "گۇرۇپپىلار"

    // Bayraq 3 Leaders & Terminology
    val bayraqLeader: String get() = "بايراق مەسئۇلى"
    val bayraqErkan: String get() = "بايراق ئەركان"
    val bayraqIdari: String get() = "بايراق ئىدارى"
    val bayraqKeyLeadersTitle: String get() = "بايراق ئاساسىي رەھبەرلىكى (3 مەسئۇل)"
    val leaderStatusMission: String get() = "باشقا يەردە خىزمەتتە"
    val leaderStatusRest: String get() = "ئارام"
    val leaderAttendanceTitle: String get() = "رەھبەرلىك يوقلىمىسى"
    
    // Sanjaqs (1..4 & dynamic)
    val sanjaq: String get() = "سانجاق"
    val sanjaq1: String get() = "1-سانجاق"
    val sanjaq2: String get() = "2-سانجاق"
    val sanjaq3: String get() = "3-سانجاق"
    val sanjaq4: String get() = "4-سانجاق"
    val addSanjaq: String get() = "سانجاق قوشۇش"
    val sanjaqLeader: String get() = "سانجاق مەسئۇلى"
    val sanjaqDeputy: String get() = "نائىب سانجاق"
    val selectSanjaq: String get() = "سانجاق تاللاڭ"
    val selectedSanjaqsSummary: String get() = "تاللانغان سانجاقلار يىغىنچاق ستاتىستىكىسى"
    val allSanjaqs: String get() = "بارلىق سانجاقلار"
    val dutySanjaq: String get() = "نۆۋەتچى سانجاق"
    val dutySanjaqAttendance: String get() = "نۆۋەتچى سانجاق يوقلىمىسى ۋە نىسبىتى"

    // System Settings & Remote Device Control (⚙️)
    val systemAndDeviceSettings: String get() = "سىستېما ۋە ئۈسكۈنە تەڭشىكى"
    val toggleLeaderAttendanceVisibility: String get() = "بايراق 3 مەسئۇل يوقلىمىسىنى كۆرسىتىش"
    val leaderAttendanceVisibleDesc: String get() = "بۇ تاللاش ئېچىلغاندا بارلىق كۆزنەكلەردە 3 مەسئۇلنىڭ يوقلىمىسى كۆرۈنىدۇ"
    val connectedDevicesTitle: String get() = "ئېچىلغان تېلېفونلار ۋە ئۈسكۈنە باشقۇرۇش"
    val terminateDevice: String get() = "تېلېفوننى توختىتىش"
    val restoreDevice: String get() = "ئەسلىگە كەلتۈرۈش"
    val deviceTerminatedNotice: String get() = "بۇ تېلېفون باشقۇرغۇچى تەرىپىدىن توختىتىلدى"
    val activeDevicesCount: String get() = "ئېچىلغان ئۈسكۈنە سانى"
    val pullToRefresh: String get() = "ئاستىغا سىيرىپ يېڭىلاڭ"
    val refreshing: String get() = "يېڭىلىنىۋاتىدۇ..."
}

object UyghurStrings : LocalizedStrings {
    override val appTitle = "يوقلىما سىستېمىسى"
    override val appSubtitle = "ئەقلىي ۋە تەرتىپلىك يوقلىما باشقۇرۇش"
    override val loginTitle = "سىستېمىغا كىرىش"
    override val loginSubtitle = "ھوقۇق تۈرىڭىزنى تاللاپ مەخپىي نومۇر بىلەن كىرىڭ"
    override val roleAdmin = "باش باشقۇرغۇچى"
    override val roleGroupLead = "بايراق مەسئۇلى"
    override val loginAsAdmin = "باشقۇرغۇچى سۈپىتىدە كىرىش"
    override val loginAsLead = "بايراق مەسئۇلى سۈپىتىدە كىرىش"
    override val username = "ھېسابات نامى"
    override val password = "مەخپىي نومۇر"
    override val loginButton = "كىرىش"
    override val logoutButton = "چېكىنىش"
    override val quickDemoLogin = "تېز سىناق كىرىش (بىر چېكىش)"
    override val selectRoleHint = "رولىڭىزنى ۋە بايرىقىڭىزنى تاللاڭ"
    override val loginError = "ھېسابات نامى ياكى مەخپىي نومۇر خاتا!"

    override val todayAttendance = "بۈگۈنكى يوقلىما"
    override val memberRoster = "ئەزالار تىزىملىكى"
    override val attendanceHistory = "يوقلىما تارىخى"
    override val groupHistory = "يوقلىما تارىخى"
    override val markAllPresent = "ھەممىنى بار دەپ بەلگىلەش"
    override val markAllPresentSuccess = "بارلىق ئەزالار بار دەپ بېكىتىلدى"
    override val date = "كۈن تەرتىپى"
    override val selectDate = "ۋاقىت تاللاش"
    override val today = "بۈگۈن"
    override val yesterday = "تۈنۈگۈن"
    override val totalMembers = "ئومۇمىي ئەزا"
    override val presentCount = "كەلگەنلەر"
    override val absentCount = "كەلمىگەنلەر"
    override val lateCount = "كېچىككەنلەر"
    override val excusedCount = "رۇخسەت"
    override val attendanceRate = "يوقلىما نىسبىتى"
    override val notes = "إزاھات"
    override val addNote = "ئىزاھات يېزىش"
    override val editNote = "ئىزاھاتنى تەھرىرلەش"
    override val save = "ساقلاش"
    override val cancel = "بىكار قىلىش"
    override val status = "ھالىتى"

    override val subGroup1 = "1-سانجاق"
    override val subGroup2 = "2-سانجاق"
    override val subGroup1Leader = "1-سانجاق مەسئۇلى"
    override val subGroup2Leader = "2-سانجاق مەسئۇلى"
    override val subGroup1LeaderLabel = "1-سانجاق مەسئۇلى"
    override val subGroup2LeaderLabel = "2-سانجاق مەسئۇلى"
    override val memberCountLabel = "ئەزا"
    override val subGroupSelection = "سانجاق تاللاش"
    override val groupLeader = "بايراق مەسئۇلى"
    override val groupLeaderPlaceholder = "بايراق مەسئۇلى ئىسمىنى كىرگۈزۈڭ..."
    override val contactAddress = "ئالاقىلىشىش ئادرېسى / تېلېفون"
    override val phoneContact = "تېلېفون نومۇرى"
    override val telegramContact = "تېلېگرام ئالاقىسى"
    override val whatsappContact = "ۋاتسئاپ ئالاقىسى"
    override val openTelegram = "تېلېگرامدا ئېچىش"
    override val openWhatsapp = "ۋاتسئاپتا ئېچىش"
    override val callPhone = "تېلېفون قىلىش"

    override val statusPresent = "بار"
    override val statusAbsent = "يوق"
    override val statusLate = "كېچىككەن"
    override val statusExcused = "رۇخسەت"

    override val addMember = "ئەزا قوشۇش"
    override val addMemberTitle = "يېڭى ئەزا قوشۇش"
    override val editMember = "ئەزانى تەھرىرلەش"
    override val deleteMember = "ئەزانى ئۆچۈرۈش"
    override val memberName = "ئەزا ئىسمى"
    override val selectSubGroup = "سانجاق تاللاڭ"
    override val memberStatus = "ئەزا ھالىتى"
    override val statusActive = "ئاكتىپ"
    override val statusInactive = "ئاكتىپسىز"
    override val joinDate = "قوشۇلغان كۈنى"
    override val noMembersInGroup = "بۇ سانجاقتا تېخى ئەزا يوق"
    override val deleteMemberConfirmTitle = "ئەزانى ئۆچۈرەمسىز؟"
    override val deleteMemberConfirmDesc = "بۇ ئەزا ۋە ئۇنىڭغا مۇناسىۋەتلىك بارلىق يوقلىما خاتىرىلىرى ئۆچۈرۈلىدۇ."
    override val searchMemberPlaceholder = "ئەزا ئىسمى بويىچە ئىزدەش..."

    override val adminDashboardTitle = "باشقۇرغۇچى تاختىسى"
    override val adminSettingsTitle = "مەسئۇللار ۋە ھېساباتلار تەڭشىكى"
    override val overview = "ئومۇمىي كۆرۈنۈش"
    override val groupsOverview = "بايراق ۋە بۆلۈملەر ھالىتى"
    override val analytics = "تەھلىل ۋە ستاتىستىكا"
    override val exportData = "ئېكسپورت"
    override val groupLeadsSettings = "مەسئۇللار تەڭشىكى"
    override val allGroupsAttendanceRate = "پۈتۈن سىستېما يوقلىما نىسبىتى"
    override val totalActiveMembers = "ئاكتىپ ئەزالار سانى"
    override val lowestGroupWarning = "يوقلىما نىسبىتى تۆۋەن بايراق"
    override val topAbsenteesTitle = "كۆپ كەلمىگەنلەر (دىققەت تىزىملىكى)"
    override val topAbsenteesSubtitle = "يوقلىمىدا يوق بولۇش قېتىم سانى كۆپ كىشىلەر"
    override val topLatesTitle = "كۆپ كېچىككەنلەر"
    override val groupRanking = "بايراقلار يوقلىما رەت تەرتىپى"
    override val resetPassword = "مەخپىي نومۇرنى قايتا تەڭشەش"
    override val newPassword = "يېڭى مەخپىي نومۇر"
    override val resetPasswordSuccess = "مەخپىي نومۇر مۇۋەپپەقىيەتلىك ئۆزگەرتىلدى"
    override val editCredentials = "ھېسابات ئۇچۇرىنى ئۆزگەرتىش"
    override val displayName = "كۆرسىتىلىدىغان نامى"
    override val editCredentialsSuccess = "ھېسابات نامى ۋە مەخپىي نومۇر مۇۋەپپەقىيەتلىك ئۆزگەرتىلدى"
    override val editGroupTitle = "بايراق ئۇچۇرى"
    override val groupName = "بايراق نامى"
    override val groupCode = "بايراق كودى"
    override val leaderLoginName = "مەسئۇل كىرىش نامى"

    override val dailyAnalytics = "كۈندىلىك تەھلىل"
    override val monthlyAnalytics = "ئايلىق ستاتىستىكا"
    override val quarterlyAnalytics = "پەسىللىك ستاتىستىكا"
    override val monthlyStats = "ئايلىق ستاتىستىكا"
    override val quarterlyStats = "پەسىللىك ستاتىستىكا"
    override val selectMonth = "ئاي تاللاش"
    override val selectQuarter = "پەسىل تاللاش"
    override val monthUnit = "ئاي"
    override val quarterUnit = "پەسىل"
    override val absenceTimes = "قېتىم كەلمىگەن"
    override val quarter1 = "1-پەسىل (1-3-ئاي)"
    override val quarter2 = "2-پەسىل (4-6-ئاي)"
    override val quarter3 = "3-پەسىل (7-9-ئاي)"
    override val quarter4 = "4-پەسىل (10-12-ئاي)"

    override val darkMode = "كېچە ھالىتى"
    override val lightMode = "كۈندۈز ھالىتى"
    override val theme = "تېما"
    override val themes = "تېما تەڭشىكى"
    override val switchTheme = "تېما ئالماشتۇرۇش"
    override val themeBlue = "كۆك"
    override val themeClassicBlue = "كۆك"
    override val themeEmerald = "زۇمرەت يېشىل"
    override val themePurple = "سۆسۈن"
    override val themeAmber = "ئالتۇن قوڭۇر"

    override val exportTitle = "سانلىق مەلۇماتنى ئېكسپورت قىلىش"
    override val exportCSV = "CSV / Excel فورماتىدا چۈشۈرۈش"
    override val exportSummaryText = "تەپسىلىي يوقلىما دوكلاتىنى كۆچۈرۈش"
    override val copyToClipboard = "كۆچۈرۈۋېلىش"
    override val copiedToClipboard = "دوكلات چاپلاش تاختىسىغا كۆچۈرۈلدى"
    override val shareReport = "باشقا ئەپلەرگە ھەمبەھىرلەش"
    override val exportDateRange = "ئېكسپورت قىلىش دائىرىسى"
    override val last7Days = "ئەڭ يېڭى 7 كۈن"
    override val last30Days = "ئەڭ يېڭى 30 كۈن"
    override val allTime = "بارلىق خاتىرىلەر"

    override val language = "تىل تاللاش"
    override val switchToUyghur = "ئۇيغۇرچە"
    override val switchToArabic = "العربية"
    override val confirm = "جەزملەش"
    override val actions = "مەشغۇلاتلار"
    override val filter = "سۈزۈش"
    override val all = "ھەممىسى"
    override val emptyAttendanceRecord = "بۇ كۈنگە تېخى يوقلىما خاتىرىسى كىرگۈزۈلمىگەن"
    override val groupLeaderCardTitle = "بايراق مەسئۇلى تەڭشىكى"
    override val pressAgainToExit = "چىقىپ كېتىش ئۈچۈن قايتا بېسىڭ"
    override val editLockedAfter12Hours = "12 سائەتتىن ئېشىپ كەتكەن يوقلىمىنى پەقەت باش باشقۇرغۇچىلا ئۆزگەرتەلەيدۇ"
    override val editLockedPastDate = "بىر كۈن ئۆتۈپ كەتكەن (ئەتىسىگە تەۋە بولغان) يوقلىمىنى پەقەت باش باشقۇرغۇچىلا ئۆزگەرتەلەيدۇ"

    // Requirement 2: Active Portals & Admin Control
    override val activePortals = "ئىشلىتىلىۋاتقان كۆزنەكلەر ھالىتى"
    override val portalStatus = "كۆزنەك ھالىتى"
    override val statusActivePort = "ئاكتىپ ئىشلىتىلىۋاتىدۇ"
    override val statusInactivePort = "ئىشلىتىلمىگەن"
    override val statusSuspendedPort = "ۋاقىتلىق توختىتىلغان"
    override val suspendPortal = "كۆزنەكنى توختىتىش"
    override val resumePortal = "كۆزنەكنى قايتا قوزغىتىش"
    override val portalSuspendedWarning = "بۇ بايراق كۆزنىكى باش باشقۇرغۇچى تەرىپىدىن ۋاقىتلىق توختىتىلدى"
    override val lastActive = "ئەڭ ئاخىرقى ئاكتىپلىق"
    override val portalControlTitle = "بايراق كۆزنەكلىرىنى نازارەت قىلىش ۋە باشقۇرۇش"

    // Requirement 3: Admin Group Creation
    override val addNewGroup = "يېڭى بايراق قوشۇش"
    override val deleteGroup = "بايراقنى ئۆچۈرۈش"
    override val deleteGroupConfirm = "بۇ بايراقنى ۋە ئۇنىڭ بارلىق سانلىق مەلۇماتلىرىنى راستىنلا ئۆچۈرەمسىز؟"
    override val initialPassword = "دەسلەپكى مەخپىي نومۇر"
    override val groupCreatedSuccess = "يېڭى بايراق مۇۋەپپەقىيەتلىك قۇرۇلدى"

    // Requirement 4: Bulk Member Import & Image Upload
    override val smartBatchImport = "ئەزالارنى كۆپلەپ كىرگۈزۈش"
    override val pasteListText = "تېكىست چاپلاپ قوشۇش"
    override val uploadListImage = "سۈرەتتىن ئاپتوماتىك ئوقۇش (AI)"
    override val parseAndPreview = "ئانالىز قىلىش ۋە تەكشۈرۈش"
    override val batchImportConfirm = "بارلىق تىزىملىكتىكى ئەزالارنى قوشۇش"
    override val batchImportSuccess = "ئەزالار مۇۋەپپەقىيەتلىك قوشۇلدى"
    override val pasteFormatHint = "مەسىلەن: ئەلى نۇر, 13800010001, @ali_tele\nھەر قۇردا بىردىن ئادەم ئىسمى، نومۇرى بولسا بولىدۇ"
    override val analyzingImage = "سۈرەت ئوقۇلۇۋاتىدۇ..."
    override val clearInput = "تازىلاش"

    // Requirement 5: Equipment / Weapon Inventory
    override val equipmentInventory = "قورال-ياراغ تىزىملىكى"
    override val equipmentName = "قورال / ئەسۋاب نامى"
    override val totalCount = "ئومۇمىي سانى"
    override val readyCount = "تەييار سانى"
    override val notReadyCount = "تەييار ئەمەس سانى"
    override val addEquipment = "قورال-ياراغ قوشۇش"
    override val editEquipment = "قورال ئۇچۇرىنى تەھرىرلەش"
    override val deleteEquipment = "قورالنى ئۆچۈرۈش"
    override val deleteEquipmentConfirm = "بۇ قورال خاتىرىسىنى راستىنلا ئۆچۈرەمسىز؟"
    override val equipmentReadinessRate = "تەييارلىق نىسبىتى"
    override val equipmentNotes = "ئىزاھات / ساقلىنىش ئورنى"
    override val allGroupsEquipment = "بارلىق بايراقلار قورال-ياراغ تىزىملىكى"
    override val emptyEquipment = "بۇ بايراققا تېخى قورال-ياراغ كىرگۈزۈلمىگەن"
    override val readyBadge = "تەييار"
    override val notReadyBadge = "تەييار ئەمەس"

    // Requirement 1 (New): Daily Updates & Situation Reports (يېڭىلىقلار)
    override val dailyUpdatesTab = "يېڭىلىقلار"
    override val dailyUpdatesTitle = "كۈندىلىك يېڭىلىقلار ۋە ئۇقتۇرۇشلار"
    override val addUpdate = "يېڭىلىق / ئەھۋال يېزىش"
    override val editUpdate = "يېڭىلىقنى تەھرىرلەش"
    override val deleteUpdate = "ئۆچۈرۈش"
    override val deleteUpdateConfirm = "بۇ يېڭىلىق ياكى ئۇقتۇرۇشنى راستىنلا ئۆچۈرەمسىز؟"
    override val updateTitle = "ماۋزۇسى"
    override val updateContent = "كۈندىلىك ئەھۋال / تەپسىلاتى"
    override val updatePriority = "دەرىجىسى"
    override val updatePriorityNormal = "ئادەتتىكى"
    override val updatePriorityUrgent = "جىددىي"
    override val updatePriorityImportant = "مۇھىم"
    override val allGroupsUpdate = "مەركەز / ئومۇمىي ئۇقتۇرۇش"
    override val noUpdatesYet = "تېخى ھېچقانداق يېڭىلىق ياكى ئەھۋال يوللانمىدى"
    override val author = "يوللىغۇچى"
    override val postUpdateSuccess = "يېڭىلىق يوللاندى"

    // Requirement 2 & 3 (New): Executive Leadership Contacts & Other Contact Field
    override val executiveContactsTitle = "مەسئۇللار ئالاقە ئادىرسى"
    override val addExecutiveContact = "باش مەسئۇل قوشۇش"
    override val editExecutiveContact = "باش مەسئۇلنى تەھرىرلەش"
    override val deleteExecutiveContact = "ئالاقىنى ئۆچۈرۈش"
    override val deleteExecutiveContactConfirm = "بۇ مەسئۇلنىڭ ئالاقە ئۇچۇرىنى راستىنلا ئۆچۈرەمسىز؟"
    override val executiveName = "مەسئۇلنىڭ ئىسمى"
    override val executiveTitle = "ۋەزىپىسى / سالاھىيىتى"
    override val radioCommsContact = "مۇخابىرات ئادرېسى / چاستوتىسى"
    override val otherContact = "باشقا"
    override val otherContactHint = "باشقا ئالاقە كودى ياكى ئىزاھات"
    override val copyRadioSuccess = "مۇخابىرات ئادرېسى كۆچۈرۈۋېلىندى"
    override val copyOtherSuccess = "ئالاقە ئادرېسى كۆچۈرۈۋېلىندى"

    // Emergency Notice (جىددىي ئۇقتۇرۇش)
    override val emergencyNoticeTitle = "جىددىي ئۇقتۇرۇش"
    override val broadcastEmergencyNotice = "جىددىي ئۇقتۇرۇش تارقىتىش"
    override val emergencyNoticeReceived = "جىددىي تاپشۇرۇپ ئېلىش"
    override val emergencyNoticeDismiss = "ئۇقتۇرۇشنى كۆردۈم / تاپشۇرۇپ ئالدىم"
    override val emergencyAlertBanner = "باش باشقۇرغۇچىدىن جىددىي ئۇقتۇرۇش!"

    // Dynamic Sub-group & Group Details
    override val addSubGroup = "سانجاق قوشۇش / كۆپەيتىش"
    override val duplicateSubGroup = "سانجاق كۆپەيتىش"
    override val subGroupUnit = "-سانجاق"
    override val groupDetailsTitle = "بايراق تەپسىلاتى"
    override val groupDetailsSubtitle = "بايراقنىڭ بارلىق ئەھۋالى ۋە ئەزالار تىزىملىكى"

    // Duty Group (نۆۋبەتچى سانجاقلار)
    override val dutyGroupTitle = "نۆۋەتچى سانجاقلار"
    override val currentDutyGroup = "نۆۋەتتىكى نۆۋەتچى بايراق"
    override val changeDutyGroup = "نۆۋەتچىنى ئالماشتۇرۇش"
    override val selectDutyGroup = "نۆۋەتچى سانجاقلارنى تاللاڭ"
    override val dutyGroupNotes = "نۆۋەتچىلىك ئىزاھاتى / ئورۇنلاشتۇرۇشى"
    override val dutyGroupReadyPersonnel = "نۆۋەتچى تەييار ئەزالار"
    override val dutyGroupStatusLabel = "نۆۋەتچىلىك ھالىتى"

    // Member Full Details (ئەزانىڭ تولۇق مەلۇماتى)
    override val memberDetailsTitle = "ئەزانىڭ تولۇق ئۇچۇرلىرى"
    override val memberAttendanceSummary = "يوقلىما ئەھۋالى خۇلاسىسى"
    override val presentDays = "كەلگەن كۈنى"
    override val absentDays = "كەلمىگەن كۈنى"
    override val excusedDays = "رۇخسەت كۈنى"
    override val recentAttendanceHistory = "يېقىنقى كۈنلەردىكى يوقلىما خاتىرىسى"

    // Password Management & Removal (مەخپىي نومۇرنى چىقىرىۋېتىش / كىرگۈزۈش)
    override val removePassword = "مەخپىي نومۇرنى چىقىرىۋېتىش"
    override val setPassword = "مەخپىي نومۇر بەلگىلەش"
    override val noPasswordSet = "مەخپىي نومۇرسىز (ئوچۇق كىرىش)"
    override val passwordRemovedSuccess = "مەخپىي نومۇر مۇۋەپپەقىيەتلىك چىقىرىۋېتىلدى"
    override val loginWithoutPassword = "مەخپىي نومۇرسىز بىۋاسىتە كىرىش"

    // AI Data Export & Custom Reports (AI ئەقلىي مەلۇمات چىقىرىش)
    override val aiDataExportTitle = "AI ئەقلىي مەلۇمات چىقىرىش ۋە دوكلات"
    override val aiPromptLabel = "مەلۇمات چىقىرىش تەلىپى (AI Prompt)"
    override val aiPromptHint = "مەسىلەن: بارلىق بايراقلارنىڭ يوقلىما، نۆۋەتچىلىك، قورال-ياراغ ۋە كەلمىگەنلەر تىزىملىكىنى خۇلاسىلەپ، رەتلىك تېلېگرام فورماتىدا چىقىرىپ بەر..."
    override val geminiApiKeyLabel = "Gemini API ئاچقۇچى"
    override val generateAiExport = "AI ئارقىلىق دوكلات ھاسىل قىلىش"
    override val aiExportGenerating = "AI مەلۇماتلارنى رەتلەپ دوكلات تۈزۈۋاتىدۇ..."
    override val aiExportResultTitle = "ھاسىل قىلىنغان AI دوكلاتى"
    override val presetPrompt1 = "كەلمىگەنلەر ۋە ئالاقە تىزىملىكى"
    override val presetPrompt2 = "نۆۋەتچىلىك ۋە قورال-ياراغ دوكلاتى"
    override val presetPrompt3 = "تولۇق كۈندىلىك يوقلىما دوكلاتى"

    // Save Feedback (Requirement 4)
    override val savedSuccess = "مۇۋەپپەقىيەتلىك ساقلاندى"
    override val saveFailed = "ساقلانمىدى، قايتا سىناڭ"

    // Notification Tracking & Timestamps (Requirement 1 & 2)
    override val notificationTrackingTitle = "بايراقلارنىڭ ئۇقتۇرۇشنى تاپشۇرۇپ ئېلىش ئەھۋالى"
    override val statusNotReceived = "تاپشۇرۇپ ئالمىدى"
    override val statusReceived = "تىلېفونغا كىردى"
    override val statusAcknowledged = "تاپشۇرۇپ ئالدىم دەپ بېسىلدى"
    override val sentTime = "تارقىتىلغان ۋاقتى"
    override val receivedTime = "تاپشۇرۇپ ئالغان ۋاقتى"
    override val acknowledgedTime = "جەزملىگەن ۋاقتى"
    override val acknowledgeNoticeBtn = "تاپشۇرۇپ ئالدىم"
    override val acknowledgedNoticeBanner = "بۇ ئۇقتۇرۇشنى تاپشۇرۇپ ئالغانلىقىڭىز جەزملەندى"

    // Member Search Across Groups (Requirement 3)
    override val searchAllMembers = "بارلىق بايراقلاردىن ئەزا ئىزدەش"
    override val searchMemberAcrossGroups = "ئەزا ئىسمى ياكى تېلېفون بويىچە ئىزدەش..."
    override val foundMembersCount = "تېپىلغان ئەزالار"

    // Equipment Per Group (Requirement 7)
    override val selectGroupEquipment = "بايراق بويىچە قورال-ياراغ كۆرۈش"
    override val allGroupsEquipmentLabel = "ھەممە بايراق"

    // AI Multimodal & Custom Task Execution (Requirement 6)
    override val aiMemberAnalysisTitle = "Gemini AI بىلەن ئەزا كىرگۈزۈش ۋە تەلەپ بويىچە ۋەزىپە ئىجرا قىلىش"
    override val aiCustomPromptHint = "بۇ يەرگە خالىغان تەلەپ ياكى كۆرسەتمىنى يېزىڭ (مەسىلەن: تېكىست ياكى رەسىمدىن ئەزالارنى ئايرىپ 1 ۋە 2-سانجاققا تەقسىملەپ بەر)..."
    override val aiExecuteTask = "تەلەپ بويىچە ئىجرا قىلىش"
    override val aiExecutionSuccess = "سۈنئىي ئىدراك ۋەزىپىنى مۇۋەپپەقىيەتلىك ئىجرا قىلدى"

    // Duty Group Attendance Details & Timestamps (Requirement 8)
    override val dutyGroupAttendanceDetails = "نۆۋەتچى سانجاقلارنىڭ بۈگۈنكى تاپشۇرغان يوقلىمىسى"
    override val attendanceSubmittedAt = "يوقلىما تاپشۇرۇلغان ۋاقىت"
    override val attendanceNotSubmittedYet = "بۈگۈنكى يوقلىما تېخى تاپشۇرۇلمىدى"
    override val dutySubGroupTitle = "نۆۋەتچى سانجاقلار"
    override val selectDutySubGroup = "نۆۋەتچى سانجاقنى بەلگىلەش"
    override val designatedDutySubGroup = "نۆۋەتچىلىككە بېكىتىلگەن سانجاقلار"
    override val dutySubGroupLeader = "نۆۋەتچى سانجاق مەسئۇلى"
    override val dutySubGroupMembers = "نۆۋەتچى سانجاق ئەزالىرى"
    override val switchDutyMainGroup = "چوڭ بايراقنى ئالماشتۇرۇش"
    override val savedSuccessfully = "ساقلاندى"

    override val noticeCancelledStatus = "ئۇقتۇرۇش ئەمەلدىن قالدى"
    override val cancelNoticeAction = "ئەمەلدىن قالدۇرۇش"
    override val uncancelNoticeAction = "ئەسلىگە كەلتۈرۈش"
    override val noticeCancelledBanner = "⚠️ بۇ ئۇقتۇرۇش باش باشقۇرغۇچى تەرىپىدىن ئەمەلدىن قالدۇرۇلدى"
    override val aiMemberImportTitle = "AI ئارقىلىق تەلەپ يېزىپ ئەزا كىرگۈزۈش"
    override val aiMemberImportPromptHint = "مەسىلەن: تۆۋەندىكى تىزىملىكتىكىلەرنى ئايرىپ، تېلېفون ۋە تېلېگراملىرى بىلەن رەتلەپ بېكىتىپ بەر..."
    override val runAiExtractionBtn = "✨ AI بىلەن ئانالىز قىلىپ كىرگۈزۈش"
    override val parsingAiWait = "AI سانلىق مەلۇماتلارنى ئانالىز قىلىۋاتىدۇ..."
}

object ArabicStrings : LocalizedStrings {
    override val appTitle = "نظام إدارة الحضور"
    override val appSubtitle = "إدارة ذكية ودقيقة للحضور والغياب"
    override val loginTitle = "تسجيل الدخول"
    override val loginSubtitle = "اختر دورك وقم بتسجيل الدخول بكلمة المرور"
    override val roleAdmin = "المدير العام (المشرف)"
    override val roleGroupLead = "مسؤول المجموعة"
    override val loginAsAdmin = "الدخول كمدير عام"
    override val loginAsLead = "الدخول كمسؤول مجموعة"
    override val username = "اسم المستخدم"
    override val password = "كلمة المرور"
    override val loginButton = "تسجيل الدخول"
    override val logoutButton = "تسجيل الخروج"
    override val quickDemoLogin = "دخول سريع تجريبي (نقرة واحدة)"
    override val selectRoleHint = "حدد دورك والمجموعة الخاصة بك"
    override val loginError = "اسم المستخدم أو كلمة المرور غير صحيحة!"

    override val todayAttendance = "حضور اليوم"
    override val memberRoster = "قائمة الأعضاء"
    override val attendanceHistory = "سجل الحضور"
    override val groupHistory = "سجل الحضور"
    override val markAllPresent = "تحديد الكل حاضر"
    override val markAllPresentSuccess = "تم تحديد جميع الأعضاء كحاضرين"
    override val date = "التاريخ"
    override val selectDate = "اختيار التاريخ"
    override val today = "اليوم"
    override val yesterday = "الأمس"
    override val totalMembers = "إجمالي الأعضاء"
    override val presentCount = "الحاضرون"
    override val absentCount = "الغائبون"
    override val lateCount = "المتأخرون"
    override val excusedCount = "المأذونون (رخصة)"
    override val attendanceRate = "نسبة الحضور"
    override val notes = "ملاحظات"
    override val addNote = "إضافة ملاحظة"
    override val editNote = "تعديل الملاحظة"
    override val save = "حفظ"
    override val cancel = "إلغاء"
    override val status = "الحالة"

    override val subGroup1 = "1-مجموعة"
    override val subGroup2 = "2-مجموعة"
    override val subGroup1Leader = "مسؤول 1-مجموعة"
    override val subGroup2Leader = "مسؤول 2-مجموعة"
    override val subGroup1LeaderLabel = "مسؤول 1-مجموعة"
    override val subGroup2LeaderLabel = "مسؤول 2-مجموعة"
    override val memberCountLabel = "عضو"
    override val subGroupSelection = "اختيار المجموعة"
    override val groupLeader = "مسؤول المجموعة"
    override val groupLeaderPlaceholder = "أدخل اسم المسؤول..."
    override val contactAddress = "عنوان / رقم التواصل"
    override val phoneContact = "رقم الهاتف"
    override val telegramContact = "تواصل تيليجرام"
    override val whatsappContact = "تواصل واتساب"
    override val openTelegram = "فتح في تيليجرام"
    override val openWhatsapp = "فتح في واتساب"
    override val callPhone = "اتصال هاتفياً"

    override val statusPresent = "حاضر"
    override val statusAbsent = "غائب"
    override val statusLate = "متأخر"
    override val statusExcused = "رخصة / إجازة"

    override val addMember = "إضافة عضو جديد"
    override val addMemberTitle = "إضافة عضو جديد"
    override val editMember = "تعديل بيانات العضو"
    override val deleteMember = "حذف العضو"
    override val memberName = "اسم العضو"
    override val selectSubGroup = "اختر المجموعة"
    override val memberStatus = "حالة العضو"
    override val statusActive = "نشط"
    override val statusInactive = "غير نشط"
    override val joinDate = "تاريخ الانضمام"
    override val noMembersInGroup = "لا يوجد أعضاء في هذه المجموعة حتى الآن"
    override val deleteMemberConfirmTitle = "هل تريد حذف هذا العضو؟"
    override val deleteMemberConfirmDesc = "سيتم حذف العضو وجميع سجلات الحضور المرتبطة به نهائياً."
    override val searchMemberPlaceholder = "بحث باسم العضو..."

    override val adminDashboardTitle = "لوحة تحكم المدير"
    override val adminSettingsTitle = "إعدادات المسؤولين والحسابات"
    override val overview = "نظرة عامة"
    override val groupsOverview = "حالة المجموعات"
    override val analytics = "التحليلات والإحصاءات"
    override val exportData = "تصدير البيانات"
    override val groupLeadsSettings = "إدارة المسؤولين"
    override val allGroupsAttendanceRate = "نسبة الحضور العامة للنظام"
    override val totalActiveMembers = "إجمالي الأعضاء النشطين"
    override val lowestGroupWarning = "المجموعة الأقل حضوراً"
    override val topAbsenteesTitle = "الأكثر غياباً (قائمة المتابعة)"
    override val topAbsenteesSubtitle = "الأعضاء الأكثر تكراراً للغياب"
    override val topLatesTitle = "الأكثر تأخراً"
    override val groupRanking = "ترتيب المجموعات حسب الحضور"
    override val resetPassword = "إعادة تعيين كلمة المرور"
    override val newPassword = "كلمة المرور الجديدة"
    override val resetPasswordSuccess = "تم تحديث كلمة المرور بنجاح"
    override val editCredentials = "تعديل بيانات الحساب"
    override val displayName = "الاسم المعروض"
    override val editCredentialsSuccess = "تم تحديث اسم المستخدم وكلمة المرور بنجاح"
    override val editGroupTitle = "معلومات المجموعة"
    override val groupName = "اسم المجموعة"
    override val groupCode = "رمز المجموعة"
    override val leaderLoginName = "اسم دخول المسؤول"

    override val dailyAnalytics = "التحليل اليومي"
    override val monthlyAnalytics = "الإحصاء الشهري"
    override val quarterlyAnalytics = "الإحصاء الفصلي"
    override val monthlyStats = "الإحصاء الشهري"
    override val quarterlyStats = "الإحصاء الفصلي"
    override val selectMonth = "اختيار الشهر"
    override val selectQuarter = "اختيار الفصل"
    override val monthUnit = "شهر"
    override val quarterUnit = "فصل"
    override val absenceTimes = "مرات الغياب"
    override val quarter1 = "الربع 1 (أشهر 1-3)"
    override val quarter2 = "الربع 2 (أشهر 4-6)"
    override val quarter3 = "الربع 3 (أشهر 7-9)"
    override val quarter4 = "الربع 4 (أشهر 10-12)"

    override val darkMode = "الوضع الليلي"
    override val lightMode = "الوضع النهاري"
    override val theme = "المظهر"
    override val themes = "خيارات المظهر"
    override val switchTheme = "تغيير المظهر"
    override val themeBlue = "أزرق"
    override val themeClassicBlue = "أزرق ملكي"
    override val themeEmerald = "أخضر زمردي"
    override val themePurple = "بنفسجي"
    override val themeAmber = "كهرماني / ذهبي"

    override val exportTitle = "تصدير بيانات الحضور"
    override val exportCSV = "تصدير كملف CSV / Excel"
    override val exportSummaryText = "نسخ تقرير الحضور المفصل"
    override val copyToClipboard = "نسخ للذاكرة"
    override val copiedToClipboard = "تم نسخ التقرير إلى الحافظة"
    override val shareReport = "مشاركة مع التطبيقات الأخرى"
    override val exportDateRange = "النطاق الزمني للتصدير"
    override val last7Days = "آخر 7 أيام"
    override val last30Days = "آخر 30 يوماً"
    override val allTime = "جميع السجلات"

    override val language = "اللغة"
    override val switchToUyghur = "ئۇيغۇرچە"
    override val switchToArabic = "العربية"
    override val confirm = "تأكيد"
    override val actions = "إجراءات"
    override val filter = "تصفية"
    override val all = "الكل"
    override val emptyAttendanceRecord = "لم يتم تسجيل حضور لهذا اليوم بعد"
    override val groupLeaderCardTitle = "إعدادات مسؤول المجموعة"
    override val pressAgainToExit = "اضغط مرة أخرى للخروج"
    override val editLockedAfter12Hours = "لا يمكن تعديل الحضور بعد 12 ساعة إلا للمشرف العام"
    override val editLockedPastDate = "لا يمكن تعديل سجلات الأيام السابقة إلا للمشرف العام"

    // Requirement 2: Active Portals & Admin Control
    override val activePortals = "حالة البوابات المفتوحة للمجموعات"
    override val portalStatus = "حالة البوابة"
    override val statusActivePort = "قيد الاستخدام والنشاط"
    override val statusInactivePort = "غير نشط حالياً"
    override val statusSuspendedPort = "موقوف مؤقتاً"
    override val suspendPortal = "إيقاف البوابة"
    override val resumePortal = "تفعيل البوابة"
    override val portalSuspendedWarning = "تم إيقاف هذه البوابة مؤقتاً من قبل المشرف العام"
    override val lastActive = "آخر نشاط"
    override val portalControlTitle = "مراقبة وإيقاف بوابات المجموعات"

    // Requirement 3: Admin Group Creation
    override val addNewGroup = "إضافة مجموعة جديدة"
    override val deleteGroup = "حذف المجموعة"
    override val deleteGroupConfirm = "هل أنت متأكد من حذف هذه المجموعة وجميع بياناتها؟"
    override val initialPassword = "كلمة المرور الأولية"
    override val groupCreatedSuccess = "تمت إضافة المجموعة الجديدة بنجاح"

    // Requirement 4: Bulk Member Import & Image Upload
    override val smartBatchImport = "استيراد وتفريغ الأعضاء"
    override val pasteListText = "لصق نص القائمة"
    override val uploadListImage = "استخراج من الصورة (ذكاء اصطناعي)"
    override val parseAndPreview = "تحليل ومعاينة"
    override val batchImportConfirm = "استيراد جميع الأعضاء للقائمة"
    override val batchImportSuccess = "تم استيراد الأعضاء بنجاح"
    override val pasteFormatHint = "مثال: الاسم, رقم الهاتف, معرف تيليجرام\nفي كل سطر اسم ورقم"
    override val analyzingImage = "جاري معالجة وقراءة الصورة..."
    override val clearInput = "مسح"

    // Requirement 5: Equipment / Weapon Inventory
    override val equipmentInventory = "قائمة العتاد والسلاح"
    override val equipmentName = "اسم السلاح / العتاد"
    override val totalCount = "العدد الإجمالي"
    override val readyCount = "الجاهز"
    override val notReadyCount = "غير الجاهز"
    override val addEquipment = "إضافة عتاد جديد"
    override val editEquipment = "تعديل بيانات العتاد"
    override val deleteEquipment = "حذف العتاد"
    override val deleteEquipmentConfirm = "هل أنت متأكد من حذف هذا السجل؟"
    override val equipmentReadinessRate = "نسبة الجاهزية"
    override val equipmentNotes = "ملاحظات / مكان الحفظ"
    override val allGroupsEquipment = "إحصائيات العتاد لجميع المجموعات"
    override val emptyEquipment = "لم يتم تسجيل أي عتاد في هذه المجموعة بعد"
    override val readyBadge = "جاهز"
    override val notReadyBadge = "غير جاهز"

    // Requirement 1 (New): Daily Updates & Situation Reports (يېڭىلىقلار)
    override val dailyUpdatesTab = "المستجدات والأخبار"
    override val dailyUpdatesTitle = "المستجدات اليومية والتعميمات"
    override val addUpdate = "كتابة مستجدات / تقرير"
    override val editUpdate = "تعديل التقرير"
    override val deleteUpdate = "حذف"
    override val deleteUpdateConfirm = "هل أنت متأكد من حذف هذا التقرير أو التعميم؟"
    override val updateTitle = "العنوان"
    override val updateContent = "المحتوى / تفاصيل الوضع اليومي"
    override val updatePriority = "الأهمية"
    override val updatePriorityNormal = "عادي"
    override val updatePriorityUrgent = "عاجل"
    override val updatePriorityImportant = "مهم"
    override val allGroupsUpdate = "القيادة العامة / تعميم للجميع"
    override val noUpdatesYet = "لم يتم نشر أي مستجدات أو تقارير بعد"
    override val author = "الناشر"
    override val postUpdateSuccess = "تم نشر التقرير بنجاح"

    // Requirement 2 & 3 (New): Executive Leadership Contacts & Other Contact Field
    override val executiveContactsTitle = "عناوين اتصال المسؤولين"
    override val addExecutiveContact = "إضافة مسؤول قيادي"
    override val editExecutiveContact = "تعديل بيانات المسؤول"
    override val deleteExecutiveContact = "حذف جهة الاتصال"
    override val deleteExecutiveContactConfirm = "هل أنت متأكد من حذف جهة الاتصال هذه؟"
    override val executiveName = "اسم المسؤول / الجهة"
    override val executiveTitle = "المنصب / الصفة"
    override val radioCommsContact = "عنوان اللاسلكي / التردد (مخابرات)"
    override val otherContact = "أخرى"
    override val otherContactHint = "رمز اتصال إضافي أو ملاحظة"
    override val copyRadioSuccess = "تم نسخ عنوان اللاسلكي"
    override val copyOtherSuccess = "تم نسخ العنوان الإضافي"

    // Emergency Notice (جىددىي ئۇقتۇرۇش)
    override val emergencyNoticeTitle = "إشعار عاجل"
    override val broadcastEmergencyNotice = "بث إشعار عاجل"
    override val emergencyNoticeReceived = "استلام الإشعار العاجل"
    override val emergencyNoticeDismiss = "تم الاطلاع والاستلام"
    override val emergencyAlertBanner = "إشعار عاجل من الإدارة العامة!"

    // Dynamic Sub-group & Group Details
    override val addSubGroup = "إضافة / مضاعفة مجموعة فرعية"
    override val duplicateSubGroup = "تكرار المجموعة"
    override val subGroupUnit = "-مجموعة"
    override val groupDetailsTitle = "تفاصيل المجموعة"
    override val groupDetailsSubtitle = "حالة المجموعة وقائمة الأعضاء بالتفصيل"

    // Duty Group (نۆۋبەتچى گۇرۇپپا)
    override val dutyGroupTitle = "المجموعة المناوبة"
    override val currentDutyGroup = "المجموعة المناوبة حالياً"
    override val changeDutyGroup = "تغيير المجموعة المناوبة"
    override val selectDutyGroup = "اختر المجموعة المناوبة"
    override val dutyGroupNotes = "ملاحظات وتعليمات المناوبة"
    override val dutyGroupReadyPersonnel = "الأفراد الجاهزون للمناوبة"
    override val dutyGroupStatusLabel = "حالة المناوبة"

    // Member Full Details (ئەزانىڭ تولۇق مەلۇماتى)
    override val memberDetailsTitle = "بيانات العضو الكاملة"
    override val memberAttendanceSummary = "ملخص سجل الحضور"
    override val presentDays = "أيام الحضور"
    override val absentDays = "أيام الغياب"
    override val excusedDays = "أيام الإجازة"
    override val recentAttendanceHistory = "سجل الحضور في الأيام الأخيرة"

    // Password Management & Removal (مەخپىي نومۇرنى چىقىرىۋېتىش / كىرگۈزۈش)
    override val removePassword = "إزالة كلمة المرور"
    override val setPassword = "تعيين كلمة المرور"
    override val noPasswordSet = "بدون كلمة مرور (دخول مباشر)"
    override val passwordRemovedSuccess = "تمت إزالة كلمة المرور بنجاح"
    override val loginWithoutPassword = "دخول مباشر بدون كلمة مرور"

    // AI Data Export & Custom Reports (AI ئەقلىي مەلۇمات چىقىرىش)
    override val aiDataExportTitle = "تصدير البيانات والتقارير الذكية (AI)"
    override val aiPromptLabel = "تعليمات التصدير وتنسيق التقرير (AI Prompt)"
    override val aiPromptHint = "مثال: لخص حضور وغياب جميع المجموعات مع أرقام التواصل وحالة العتاد والمجموعة المناوبة..."
    override val geminiApiKeyLabel = "مفتاح Gemini API"
    override val generateAiExport = "توليد التقرير بواسطة AI"
    override val aiExportGenerating = "جاري إعداد وتنسيق التقرير بواسطة الذكاء الاصطناعي..."
    override val aiExportResultTitle = "التقرير المنشأ بواسطة AI"
    override val presetPrompt1 = "قائمة الغائبين وأرقام التواصل"
    override val presetPrompt2 = "تقرير المناوبة وحالة العتاد"
    override val presetPrompt3 = "تقرير الحضور والجاهزية اليومي الشامل"

    // Save Feedback (Requirement 4)
    override val savedSuccess = "تم الحفظ بنجاح"
    override val saveFailed = "فشل الحفظ، يرجى المحاولة مرة أخرى"

    // Notification Tracking & Timestamps (Requirement 1 & 2)
    override val notificationTrackingTitle = "حالة استلام المجموعات للإشعار والتعميم"
    override val statusNotReceived = "لم يستلم"
    override val statusReceived = "وصل للهاتف"
    override val statusAcknowledged = "تم تأكيد الاستلام"
    override val sentTime = "وقت الإرسال"
    override val receivedTime = "وقت الاستلام"
    override val acknowledgedTime = "وقت التأكيد"
    override val acknowledgeNoticeBtn = "تم الاستلام"
    override val acknowledgedNoticeBanner = "تم تأكيد استلام هذا الإشعار بنجاح"

    // Member Search Across Groups (Requirement 3)
    override val searchAllMembers = "بحث عن الأعضاء في جميع المجموعات"
    override val searchMemberAcrossGroups = "بحث بالاسم أو الهاتف في كل المجموعات..."
    override val foundMembersCount = "الأعضاء المطابقين"

    // Equipment Per Group (Requirement 7)
    override val selectGroupEquipment = "عرض العتاد حسب المجموعة"
    override val allGroupsEquipmentLabel = "جميع المجموعات"

    // AI Multimodal & Custom Task Execution (Requirement 6)
    override val aiMemberAnalysisTitle = "تحليل وإدخال الأعضاء وتنفيذ المهام عبر Gemini AI"
    override val aiCustomPromptHint = "اكتب أي تعليمات أو طلب هنا (مثال: استخرج أسماء الأعضاء من الصورة ووزعهم على المجموعات)..."
    override val aiExecuteTask = "تنفيذ المهمة عبر AI"
    override val aiExecutionSuccess = "تم تنفيذ المهمة بنجاح عبر الذكاء الاصطناعي"

    // Duty Group Attendance Details & Timestamps (Requirement 8)
    override val dutyGroupAttendanceDetails = "سجل حضور المجموعة المناوبة اليوم"
    override val attendanceSubmittedAt = "وقت تسليم الحضور"
    override val attendanceNotSubmittedYet = "لم يتم تسليم حضور اليوم بعد"
    override val dutySubGroupTitle = "المجموعة الفرعية المناوبة"
    override val selectDutySubGroup = "تحديد المجموعة الفرعية المناوبة"
    override val designatedDutySubGroup = "المجموعة الفرعية المحددة للمناوبة"
    override val dutySubGroupLeader = "مسؤول المجموعة الفرعية المناوبة"
    override val dutySubGroupMembers = "أعضاء المجموعة الفرعية المناوبة"
    override val switchDutyMainGroup = "تبديل المجموعة الرئيسية"
    override val savedSuccessfully = "تم الحفظ"

    override val noticeCancelledStatus = "تم إلغاء الإشعار"
    override val cancelNoticeAction = "إلغاء الإشعار"
    override val uncancelNoticeAction = "استعادة الإشعار"
    override val noticeCancelledBanner = "⚠️ تم إلغاء هذا الإشعار من قبل الإدارة العامة"
    override val aiMemberImportTitle = "استيراد الأعضاء عبر كتابة طلب للذكاء الاصطناعي"
    override val aiMemberImportPromptHint = "مثال: استخرج أسماء الأعضاء وأرقام الهواتف والتليجرام وقسمهم لمجموعات..."
    override val runAiExtractionBtn = "✨ تحليل واستخراج بالذكاء الاصطناعي"
    override val parsingAiWait = "جاري تحليل البيانات عبر الذكاء الاصطناعي..."
}

object EnglishStrings : LocalizedStrings {
    override val appTitle = "Attendance System"
    override val appSubtitle = "Smart & Accurate Attendance Management"
    override val loginTitle = "Sign In"
    override val loginSubtitle = "Select your role and enter password"
    override val roleAdmin = "Super Admin"
    override val roleGroupLead = "Group Leader"
    override val loginAsAdmin = "Login as Super Admin"
    override val loginAsLead = "Login as Group Leader"
    override val username = "Username"
    override val password = "Password"
    override val loginButton = "Sign In"
    override val logoutButton = "Sign Out"
    override val quickDemoLogin = "Quick Login (One-click Demo)"
    override val selectRoleHint = "Select your role and designated group"
    override val loginError = "Incorrect username or password"

    // Group Lead Screen
    override val todayAttendance = "Today's Attendance"
    override val memberRoster = "Member Roster"
    override val attendanceHistory = "Attendance History"
    override val groupHistory = "Group History"
    override val markAllPresent = "Mark All Present"
    override val markAllPresentSuccess = "All active members marked present"
    override val date = "Date"
    override val selectDate = "Select Date"
    override val today = "Today"
    override val yesterday = "Yesterday"
    override val totalMembers = "Total Members"
    override val presentCount = "Present"
    override val absentCount = "Absent"
    override val lateCount = "Late"
    override val excusedCount = "Excused"
    override val attendanceRate = "Attendance Rate"
    override val notes = "Notes"
    override val addNote = "Add Note"
    override val editNote = "Edit Note"
    override val save = "Save"
    override val cancel = "Cancel"
    override val status = "Status"

    // Sub-groups & Leaders
    override val subGroup1 = "Subgroup 1"
    override val subGroup2 = "Subgroup 2"
    override val subGroup1Leader = "Subgroup 1 Leader"
    override val subGroup2Leader = "Subgroup 2 Leader"
    override val subGroup1LeaderLabel = "Subgroup 1 Lead"
    override val subGroup2LeaderLabel = "Subgroup 2 Lead"
    override val memberCountLabel = "Members"
    override val subGroupSelection = "Subgroup"
    override val groupLeader = "Group Leader"
    override val groupLeaderPlaceholder = "Enter group leader name"
    override val contactAddress = "Contact / Address"
    override val phoneContact = "Phone"
    override val telegramContact = "Telegram"
    override val whatsappContact = "WhatsApp"
    override val openTelegram = "Open Telegram"
    override val openWhatsapp = "Open WhatsApp"
    override val callPhone = "Call"

    // Statuses
    override val statusPresent = "Present"
    override val statusAbsent = "Absent"
    override val statusLate = "Late"
    override val statusExcused = "Excused"

    // Members
    override val addMember = "Add Member"
    override val addMemberTitle = "Add New Member"
    override val editMember = "Edit Member"
    override val deleteMember = "Delete Member"
    override val memberName = "Member Name"
    override val selectSubGroup = "Select Subgroup"
    override val memberStatus = "Member Status"
    override val statusActive = "Active"
    override val statusInactive = "Inactive"
    override val joinDate = "Join Date"
    override val noMembersInGroup = "No members registered in this group"
    override val deleteMemberConfirmTitle = "Delete Member"
    override val deleteMemberConfirmDesc = "Are you sure you want to permanently delete this member?"
    override val searchMemberPlaceholder = "Search member name or contact..."

    // Admin Dashboard
    override val adminDashboardTitle = "Administration Dashboard"
    override val adminSettingsTitle = "Admin Settings"
    override val overview = "Overview"
    override val groupsOverview = "Groups Overview"
    override val analytics = "Analytics"
    override val exportData = "Export Reports"
    override val groupLeadsSettings = "Group Leaders Management"
    override val allGroupsAttendanceRate = "Overall Attendance Rate"
    override val totalActiveMembers = "Total Active Members"
    override val lowestGroupWarning = "Group with lowest attendance"
    override val topAbsenteesTitle = "Frequent Absentees"
    override val topAbsenteesSubtitle = "Members with highest absence count"
    override val topLatesTitle = "Frequent Lates"
    override val groupRanking = "Group Attendance Ranking"
    override val resetPassword = "Reset Password"
    override val newPassword = "New Password"
    override val resetPasswordSuccess = "Password reset successfully"
    override val editCredentials = "Edit Account Credentials"
    override val displayName = "Display Name"
    override val editCredentialsSuccess = "Credentials updated successfully"
    override val editGroupTitle = "Edit Group Information"
    override val groupName = "Group Name"
    override val groupCode = "Group Code"
    override val leaderLoginName = "Leader Username"

    // Analytics
    override val dailyAnalytics = "Daily Analytics"
    override val monthlyAnalytics = "Monthly Analytics"
    override val quarterlyAnalytics = "Quarterly Analytics"
    override val monthlyStats = "Monthly Statistics"
    override val quarterlyStats = "Quarterly Statistics"
    override val selectMonth = "Select Month"
    override val selectQuarter = "Select Quarter"
    override val monthUnit = "Month"
    override val quarterUnit = "Quarter"
    override val absenceTimes = "Absences"
    override val quarter1 = "Q1 (Jan - Mar)"
    override val quarter2 = "Q2 (Apr - Jun)"
    override val quarter3 = "Q3 (Jul - Sep)"
    override val quarter4 = "Q4 (Oct - Dec)"

    // Themes
    override val darkMode = "Dark Mode"
    override val lightMode = "Light Mode"
    override val theme = "Theme"
    override val themes = "Color Themes"
    override val switchTheme = "Switch Theme"
    override val themeBlue = "Ocean Blue"
    override val themeClassicBlue = "Classic Blue"
    override val themeEmerald = "Emerald Green"
    override val themePurple = "Royal Purple"
    override val themeAmber = "Amber Gold"

    // Export
    override val exportTitle = "Export Attendance Data"
    override val exportCSV = "Export CSV Table"
    override val exportSummaryText = "Export Text Summary"
    override val copyToClipboard = "Copy to Clipboard"
    override val copiedToClipboard = "Copied to clipboard"
    override val shareReport = "Share Report"
    override val exportDateRange = "Export Date Range"
    override val last7Days = "Last 7 Days"
    override val last30Days = "Last 30 Days"
    override val allTime = "All History"

    // Common
    override val language = "Language"
    override val switchToUyghur = "ئۇيغۇرچە"
    override val switchToArabic = "العربية"
    override val confirm = "Confirm"
    override val actions = "Actions"
    override val filter = "Filter"
    override val all = "All"
    override val emptyAttendanceRecord = "No attendance records found for this date"
    override val groupLeaderCardTitle = "Group Leader Information"
    override val pressAgainToExit = "Press back once more to exit"
    override val editLockedAfter12Hours = "Locked: Attendance locked after day crossover"
    override val editLockedPastDate = "Past dates can only be modified by Super Admin"

    // Requirement 2: Active Portals & Admin Control
    override val activePortals = "Active Portals Status"
    override val portalStatus = "Portal Status"
    override val statusActivePort = "Active"
    override val statusInactivePort = "Inactive"
    override val statusSuspendedPort = "Suspended"
    override val suspendPortal = "Suspend Portal"
    override val resumePortal = "Resume Portal"
    override val portalSuspendedWarning = "This group portal has been temporarily suspended by Admin"
    override val lastActive = "Last Active"
    override val portalControlTitle = "Group Portals Monitoring & Control"

    // Requirement 3: Admin Group Creation
    override val addNewGroup = "Add New Group"
    override val deleteGroup = "Delete Group"
    override val deleteGroupConfirm = "Are you sure you want to delete this group and all its records?"
    override val initialPassword = "Initial Password"
    override val groupCreatedSuccess = "New group created successfully"

    // Requirement 4: Bulk Member Import
    override val smartBatchImport = "Smart Member Import"
    override val pasteListText = "Paste Roster Text"
    override val uploadListImage = "Upload Roster Image / Document"
    override val parseAndPreview = "Extract & Preview"
    override val batchImportConfirm = "Confirm Import"
    override val batchImportSuccess = "Members imported successfully"
    override val pasteFormatHint = "Paste members list here (one per line, format: Name, Subgroup, Contact)..."
    override val analyzingImage = "Analyzing document / image..."
    override val clearInput = "Clear"

    // Requirement 5: Equipment Inventory
    override val equipmentInventory = "Equipment Inventory"
    override val equipmentName = "Equipment Name"
    override val totalCount = "Total Count"
    override val readyCount = "Ready"
    override val notReadyCount = "Maintenance"
    override val addEquipment = "Add Equipment"
    override val editEquipment = "Edit Equipment"
    override val deleteEquipment = "Delete Equipment"
    override val deleteEquipmentConfirm = "Are you sure you want to delete this equipment record?"
    override val equipmentReadinessRate = "Readiness Rate"
    override val equipmentNotes = "Storage Location / Notes"
    override val allGroupsEquipment = "All Groups Equipment Overview"
    override val emptyEquipment = "No equipment registered for this group yet"
    override val readyBadge = "Ready"
    override val notReadyBadge = "Under Care"

    // Requirement 1: Daily Updates & Announcements
    override val dailyUpdatesTab = "Announcements"
    override val dailyUpdatesTitle = "Notices & Announcements"
    override val addUpdate = "Post Notice"
    override val editUpdate = "Edit Notice"
    override val deleteUpdate = "Delete Notice"
    override val deleteUpdateConfirm = "Are you sure you want to delete this notice?"
    override val updateTitle = "Notice Subject"
    override val updateContent = "Message Details"
    override val updatePriority = "Priority"
    override val updatePriorityNormal = "Normal"
    override val updatePriorityUrgent = "Urgent"
    override val updatePriorityImportant = "Important"
    override val allGroupsUpdate = "All Groups Broadcast"
    override val noUpdatesYet = "No notices posted yet"
    override val author = "Author"
    override val postUpdateSuccess = "Notice broadcasted successfully"

    // Executive Leadership Contacts
    override val executiveContactsTitle = "Command Leadership Contacts"
    override val addExecutiveContact = "Add Contact"
    override val editExecutiveContact = "Edit Contact"
    override val deleteExecutiveContact = "Delete Contact"
    override val deleteExecutiveContactConfirm = "Are you sure you want to delete this contact?"
    override val executiveName = "Commander / Officer Name"
    override val executiveTitle = "Rank / Role"
    override val radioCommsContact = "Radio Frequency Channel"
    override val otherContact = "Alternative Phone / Contact"
    override val otherContactHint = "Alternative number, backup, etc."
    override val copyRadioSuccess = "Radio channel copied"
    override val copyOtherSuccess = "Contact copied"

    // Emergency Notice
    override val emergencyNoticeTitle = "Emergency Alert"
    override val broadcastEmergencyNotice = "Broadcast Emergency Alert"
    override val emergencyNoticeReceived = "Emergency alert received"
    override val emergencyNoticeDismiss = "Acknowledge"
    override val emergencyAlertBanner = "⚠️ HIGH PRIORITY EMERGENCY ALERT"

    // Dynamic Sub-group
    override val addSubGroup = "Add Subgroup"
    override val duplicateSubGroup = "Duplicate Subgroup"
    override val subGroupUnit = "Unit"
    override val groupDetailsTitle = "Group Details"
    override val groupDetailsSubtitle = "Group Information and Subgroups"

    // Duty Group
    override val dutyGroupTitle = "Duty Group"
    override val currentDutyGroup = "Current Duty Group"
    override val changeDutyGroup = "Assign Duty Group"
    override val selectDutyGroup = "Select Duty Group"
    override val dutyGroupNotes = "Duty Handover Notes"
    override val dutyGroupReadyPersonnel = "Standby Personnel Count"
    override val dutyGroupStatusLabel = "Duty Status"

    // Member Full Details
    override val memberDetailsTitle = "Member Profile"
    override val memberAttendanceSummary = "Attendance Summary"
    override val presentDays = "Present Days"
    override val absentDays = "Absent Days"
    override val excusedDays = "Excused Days"
    override val recentAttendanceHistory = "Recent Attendance History"

    // Password Management
    override val removePassword = "Remove Password"
    override val setPassword = "Set Password"
    override val noPasswordSet = "No password required"
    override val passwordRemovedSuccess = "Password protection removed"
    override val loginWithoutPassword = "Enter without Password"

    // AI Data Export
    override val aiDataExportTitle = "AI Report Generator"
    override val aiPromptLabel = "Custom Prompt Instructions"
    override val aiPromptHint = "Specify what report to generate (e.g. attendance summary, absentees, duty roster)..."
    override val geminiApiKeyLabel = "Gemini API Key"
    override val generateAiExport = "Generate AI Report"
    override val aiExportGenerating = "AI is preparing report..."
    override val aiExportResultTitle = "Generated Report"
    override val presetPrompt1 = "Absentee and Contact List"
    override val presetPrompt2 = "Duty and Equipment Status"
    override val presetPrompt3 = "Full Daily Attendance Overview"

    // Save Feedback
    override val savedSuccess = "Saved successfully"
    override val saveFailed = "Save failed, please retry"

    // Notification Tracking
    override val notificationTrackingTitle = "Notice Delivery Receipts"
    override val statusNotReceived = "Pending Delivery"
    override val statusReceived = "Delivered to Device"
    override val statusAcknowledged = "Acknowledged"
    override val sentTime = "Broadcast Time"
    override val receivedTime = "Delivery Time"
    override val acknowledgedTime = "Acknowledged Time"
    override val acknowledgeNoticeBtn = "Acknowledge Receipt"
    override val acknowledgedNoticeBanner = "You have acknowledged this notice"

    // Member Search
    override val searchAllMembers = "Search Members Across All Groups"
    override val searchMemberAcrossGroups = "Search name or phone across all groups..."
    override val foundMembersCount = "Matching Members"

    // Equipment Per Group
    override val selectGroupEquipment = "Filter Equipment by Group"
    override val allGroupsEquipmentLabel = "All Groups"

    // AI Multimodal
    override val aiMemberAnalysisTitle = "Gemini AI Member Import & Analysis"
    override val aiCustomPromptHint = "Write instructions for AI (e.g. parse members and assign to Subgroups 1 & 2)..."
    override val aiExecuteTask = "Execute AI Task"
    override val aiExecutionSuccess = "AI task executed successfully"

    // Duty Group Attendance Details
    override val dutyGroupAttendanceDetails = "Today's Duty Group Attendance Records"
    override val attendanceSubmittedAt = "Submission Time"
    override val attendanceNotSubmittedYet = "Not submitted yet today"
    override val dutySubGroupTitle = "Designated Duty Subgroup"
    override val selectDutySubGroup = "Assign Duty Subgroup"
    override val designatedDutySubGroup = "Assigned Duty Subgroup"
    override val dutySubGroupLeader = "Duty Subgroup Leader"
    override val dutySubGroupMembers = "Duty Subgroup Members"
    override val switchDutyMainGroup = "Switch Main Group"
    override val savedSuccessfully = "Saved successfully"

    override val noticeCancelledStatus = "Notice Cancelled"
    override val cancelNoticeAction = "Cancel Notice"
    override val uncancelNoticeAction = "Restore Notice"
    override val noticeCancelledBanner = "⚠️ This notice has been cancelled by Administration"
    override val aiMemberImportTitle = "Smart AI Roster Import"
    override val aiMemberImportPromptHint = "Example: Extract names, phone numbers, and subgroups..."
    override val runAiExtractionBtn = "✨ Extract & Import with AI"
    override val parsingAiWait = "AI is parsing member roster..."
}
