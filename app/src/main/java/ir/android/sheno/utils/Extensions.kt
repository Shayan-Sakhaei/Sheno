package ir.android.sheno.utils


fun String.getFileNameFromUrlString(): String {
    val segments = this.split("/")
    return segments[segments.size - 1]
}

fun String.getFileFormatFromUrlString(): String {
    val segments = this.split(".")
    return segments[segments.lastIndex]
}
