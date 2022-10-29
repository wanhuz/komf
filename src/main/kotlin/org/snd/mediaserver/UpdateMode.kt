package org.snd.mediaserver

enum class UpdateMode {
    API,

    @Deprecated("use COMIC_INFO instead")
    FILE_EMBED,
    COMIC_INFO,

    @Deprecated("deprecating support")
    SERIES_JSON
}
