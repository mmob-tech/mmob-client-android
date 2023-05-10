package com.mmob.mmobclient

import android.net.Uri
import com.google.common.net.InternetDomainName
import java.net.URI

class MmobClientHelper {
    fun getDomain(uri: Uri): String? {
        return try {
            val uriHostString = uri.host.toString()
            InternetDomainName.from(uriHostString).topPrivateDomain().toString()
        } catch (e: Exception) {
            null
        }
    }

    fun getHost(urlString: String?): String? {
        return try {
            if (urlString == null) {
                return null
            }

            val uri = URI(urlString)
            uri.host
        } catch (e: Exception) {
            null
        }
    }
}