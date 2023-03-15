package com.androiddev.social.shared

import android.net.Uri
import android.text.TextUtils
import retrofit2.Response
import java.util.regex.Matcher
import java.util.regex.Pattern


private val LINK_HEADER_PATTERN: Pattern =
    Pattern.compile("(?:(?:,\\s*)?<([^>]+)>|;\\s*(\\w+)=['\"](\\w+)['\"])")

fun headerLinks(httpResponse: Response<*>): Pair<Uri?, Uri?> {
    val link: String = httpResponse.headers()["Link"]!!
    var nextPageUri: Uri? = null
    var prevPageUri: Uri? = null
    if (!TextUtils.isEmpty(link)) {
        val matcher: Matcher = LINK_HEADER_PATTERN.matcher(link)
        var url: String? = null
        while (matcher.find()) {
            if (url == null) {
                val _url = matcher.group(1) ?: continue
                url = _url
            } else {
                val paramName = matcher.group(2)
                val paramValue = matcher.group(3)
                if (paramName == null || paramValue == null) return Pair(null,null)
                if ("rel" == paramName) {
                    when (paramValue) {
                        "next" -> nextPageUri = Uri.parse(url)
                        "prev" -> prevPageUri = Uri.parse(url)
                    }
                    url = null
                }
            }
        }
    }
    return prevPageUri to nextPageUri
}

class HeaderPaginationList<T> : ArrayList<T> {
    var nextPageUri: Uri? = null
    var prevPageUri: Uri? = null

    constructor(initialCapacity: Int) : super(initialCapacity)
    constructor() : super()
    constructor(c: Collection<T>) : super(c)
}