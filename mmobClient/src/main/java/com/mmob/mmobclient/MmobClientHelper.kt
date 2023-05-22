package com.mmob.mmobclient

import android.net.Uri
import com.google.common.net.InternetDomainName
import java.net.URI

enum class InstanceDomain {
    MMOB, EFNETWORK
}

class MmobClientHelper {
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

    fun getInstanceDomain(instanceDomain: InstanceDomain): String {
        return when (instanceDomain) {
            InstanceDomain.MMOB -> "mmob.com"
            InstanceDomain.EFNETWORK -> "ef-network.com"
        }
    }

    fun getRootDomain(uri: Uri): String? {
        return try {
            val uriHostString = uri.host.toString()
            InternetDomainName.from(uriHostString).topPrivateDomain().toString()
        } catch (e: Exception) {
            null
        }
    }
}