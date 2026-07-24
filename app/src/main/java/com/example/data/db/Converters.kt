package com.example.data.db

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromChatType(value: ChatType): String = value.name

    @TypeConverter
    fun toChatType(value: String): ChatType = enumValueOf(value)

    @TypeConverter
    fun fromMediaType(value: MediaType): String = value.name

    @TypeConverter
    fun toMediaType(value: String): MediaType = enumValueOf(value)

    @TypeConverter
    fun fromMessageStatus(value: MessageStatus): String = value.name

    @TypeConverter
    fun toMessageStatus(value: String): MessageStatus = enumValueOf(value)

    @TypeConverter
    fun fromCallType(value: CallType): String = value.name

    @TypeConverter
    fun toCallType(value: String): CallType = enumValueOf(value)

    @TypeConverter
    fun fromCallDirection(value: CallDirection): String = value.name

    @TypeConverter
    fun toCallDirection(value: String): CallDirection = enumValueOf(value)
}
