package pl.charlie.repertuar.updates

data class AppVersion private constructor(val major: Int, val minor: Int, val patch: Int) : Comparable<AppVersion> {
    override fun compareTo(other: AppVersion): Int = compareValuesBy(this, other,
        AppVersion::major, AppVersion::minor, AppVersion::patch)
    override fun toString(): String = "$major.$minor.$patch"

    companion object {
        fun parse(value: String): AppVersion {
            require(value.matches(Regex("(?:0|[1-9]\\d*)\\.(?:0|[1-9]\\d*)\\.(?:0|[1-9]\\d*)")))
            val numbers = value.split('.').map(String::toInt)
            return AppVersion(numbers[0], numbers[1], numbers[2])
        }
    }
}

data class AppRelease(val version: AppVersion) {
    val tag: String get() = "v$version"
    val filename: String get() = "Charlie-MVP-$version.apk"
    val pageUrl: String get() = "$RepositoryUrl/releases/tag/$tag"
    val downloadUrl: String get() = "$RepositoryUrl/releases/download/$tag/$filename"

    companion object {
        const val Repository = "piotrowskiadam/kino-charlie-android"
        const val RepositoryUrl = "https://github.com/$Repository"
    }
}

data class UpdateState(
    val available: AppRelease? = null,
    val checkedAt: Long = 0L,
    val checking: Boolean = false,
    val error: String? = null,
)
