package com.siris.mobile.messages.common.models.contacts

import kotlinx.serialization.Serializable

@Serializable
data class Email(var value: String, var type: Int, var label: String)
