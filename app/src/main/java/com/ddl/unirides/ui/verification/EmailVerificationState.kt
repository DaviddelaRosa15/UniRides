package com.ddl.unirides.ui.verification

data class EmailVerificationState(
    val isLoading: Boolean = false,
    val isCheckingVerification: Boolean = false,
    val isSendingEmail: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val isVerified: Boolean = false
)

