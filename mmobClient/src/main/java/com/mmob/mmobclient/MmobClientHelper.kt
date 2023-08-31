package com.mmob.mmobclient

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.common.net.InternetDomainName
import java.net.URI

enum class InstanceDomain {
    MMOB, EFNETWORK
}

class MmobClientHelper {
    object Constants {
        const val AFFILIATE_REDIRECT_PATH = "affiliate-redirect"
        const val MMOB_ROOT_DOMAIN = "mmob.com"
        const val EFNETWORK_ROOT_DOMAIN = "ef-network.com"
        val BLACKLISTED_DOMAINS = arrayOf("play.google.com", "appgallery.huawei.com")
    }

    fun isAffiliateRedirect(uri: Uri): Boolean {
        val uriString = uri.toString()
        return Constants.AFFILIATE_REDIRECT_PATH in uriString
    }

    fun isBlacklistedDomain(uri: Uri): Boolean {
        return Constants.BLACKLISTED_DOMAINS.contains(this.getHost(uri))
    }

    fun isValidUrlScheme(uri: Uri): Boolean {
        val uriString = uri.toString()
        return uriString.startsWith("http://") || uriString.startsWith("https://")
    }

    fun isUriValid(uriString: String): Boolean {
        return try {
            val uri = URI(uriString)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getHost(uri: Uri): String? {
        return try {
            return uri.host
        } catch (e: Exception) {
            null
        }
    }

    fun getInstanceDomain(instanceDomain: InstanceDomain): String {
        return when (instanceDomain) {
            InstanceDomain.MMOB -> Constants.MMOB_ROOT_DOMAIN
            InstanceDomain.EFNETWORK -> Constants.EFNETWORK_ROOT_DOMAIN
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

    fun openUriInBrowser(context: Context, uri: Uri): Boolean {
        if (isUriValid(uri.toString())) {
            val browserIntent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(browserIntent)
            return true
        }
        return false
    }
}