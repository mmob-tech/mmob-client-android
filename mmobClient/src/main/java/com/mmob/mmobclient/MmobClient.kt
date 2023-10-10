package com.mmob.mmobclient

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

typealias MmobView = WebView

class MmobClient(
    private val mmobView: MmobView,
    private val context: Context,
    private val instanceDomain: InstanceDomain = InstanceDomain.MMOB
) {
    fun loadDistribution(distribution: MmobDistribution, customerInfo: MmobCustomerInfo) {
        val data = "configuration${encodeDistributionConfiguration(distribution)}&${
            encodeCustomerInfo(customerInfo)
        }"

        startWebView(
            mmobView,
            getUrl(distribution.distribution.environment, instanceDomain, "tpp/distribution/boot"),
            data
        )
    }

    fun loadIntegration(integration: MmobIntegrationConfiguration, customerInfo: MmobCustomerInfo) {
        val data = "&${encodeIntegrationConfiguration(integration)}&${
            encodeCustomerInfo(customerInfo)
        }"

        startWebView(mmobView, getUrl(integration.environment, instanceDomain), data)
    }

    private fun getUrl(
        environment: String, instanceDomain: InstanceDomain, suffix: String = "boot"
    ): String {
        val instanceDomainString = MmobClientHelper().getInstanceDomain(instanceDomain)

        return when (environment) {
            "local" -> "http://10.0.2.2:3100/$suffix"
            "dev" -> "https://client-ingress.dev.$instanceDomainString/$suffix"
            "stag" -> "https://client-ingress.stag.$instanceDomainString/$suffix"
            else -> {
                "https://client-ingress.prod.$instanceDomainString/$suffix"
            }
        }
    }

    private fun encodeIntegrationConfiguration(integration: MmobIntegrationConfiguration): String {
        val builder = Uri.Builder()

        builder.appendQueryParameter("cp_id", integration.cp_id)
        builder.appendQueryParameter("cp_deployment_id", integration.cp_deployment_id)
        builder.appendQueryParameter("environment", integration.environment)
        builder.appendQueryParameter("locale", integration.locale)
        builder.appendQueryParameter("identifier_type", "android")
        builder.appendQueryParameter("identifier_value", context.packageName)

        val finalUri = builder.build()
        return finalUri.encodedQuery.toString()
    }

    private fun encodeDistributionConfiguration(distribution: MmobDistribution): String {
        val configuration = distribution.distribution
        val builder = Uri.Builder()

        builder.appendQueryParameter("[distribution_id]", configuration.distribution_id)
        builder.appendQueryParameter("configuration[environment]", configuration.environment)
        builder.appendQueryParameter("configuration[locale]", configuration.environment)
        builder.appendQueryParameter("configuration[identifier_type]", "android")
        builder.appendQueryParameter("configuration[identifier_value]", context.packageName)

        val finalUri = builder.build()
        return finalUri.encodedQuery.toString()
    }

    private fun encodeCustomerInfo(customerInfo: MmobCustomerInfo): String {
        val configuration = customerInfo.customerInfo
        val builder = Uri.Builder()

        for (property in configuration.javaClass.declaredFields) {
            property.isAccessible = true
            val propertyName = property.name
            val propertyValue = property.get(configuration)

            if (propertyValue != null) {
                builder.appendQueryParameter("[${propertyName}]", propertyValue.toString())
            }
        }

        val finalUri = builder.build()
        return finalUri.encodedQuery.toString()
    }

    private fun startWebView(mmobView: WebView, url: String, data: String) {
        // Enable Javascript
        val webSettings = mmobView.settings
        webSettings.domStorageEnabled = true
        webSettings.javaScriptEnabled = true

        // Force links and redirects to open in the WebView instead of in a browser
        mmobView.webViewClient = MmobViewClient(mmobView.context, instanceDomain)

        // Post data to url
        mmobView.postUrl(url, data.toByteArray())
    }

    data class MmobIntegrationConfiguration(
        val cp_id: String, val cp_deployment_id: String, val environment: String = "production", val locale: String = "en_GB"
    )

    data class MmobDistribution(
        val distribution: Configuration
    ) {
        data class Configuration(
            val distribution_id: String, val environment: String = "production", val locale: String = "en_GB"
        )
    }

    data class MmobCustomerInfo(
        val customerInfo: Configuration
    ) {
        data class Configuration(
            val email: String? = null,
            val title: String? = null,
            val first_name: String? = null,
            val surname: String? = null,
            val dob: String? = null,
            val phone_number: String? = null,
            val mobile_number: String? = null,
            val preferred_name: String? = null,
            val passport_number: String? = null,
            val national_insurance_number: String? = null,
            val building_number: String? = null,
            val address_1: String? = null,
            val address_2: String? = null,
            val address_3: String? = null,
            val town_city: String? = null,
            val county: String? = null,
            val postcode: String? = null,
            val country_of_residence: String? = null,
            val nationality: String? = null,
            val gender: String? = null,
            val relationship_status: String? = null,
            val number_of_children: Number? = null,
            val partner_first_name: String? = null,
            val partner_surname: String? = null,
            val partner_dob: String? = null,
            val partner_sex: String? = null,
            val relationship_to_partner: String? = null,
            val smoker: String? = null,
            val number_of_cigarettes_per_week: Number? = null,
            val drinker: String? = null,
            val number_of_units_per_week: Number? = null,
            val meta: Map<String, Any>? = null
        )
    }
}

private class MmobViewClient(private val context: Context, private val instanceDomain: InstanceDomain) :
    WebViewClient() {
    val helper = MmobClientHelper()

    @Deprecated("shouldOverrideUrlLoading is deprecated, providing support for older versions of Android")
    override fun shouldOverrideUrlLoading(view: MmobView?, url: String): Boolean {
        return handleUri(Uri.parse(url))
    }

    @TargetApi(Build.VERSION_CODES.N)
    override fun shouldOverrideUrlLoading(view: MmobView?, request: WebResourceRequest): Boolean {
        return handleUri(request.url)
    }

    // Gives us a chance to take control when a URL is about to be loaded in the MmobViewClient
    // Boolean response determines whether we took control or not
    // True to cancel current load
    private fun handleUri(uri: Uri): Boolean {
        val domain = helper.getRootDomain(uri)
        val instanceDomainString = helper.getInstanceDomain(instanceDomain)
        val isAffiliateRedirect = helper.isAffiliateRedirect(uri)

        // uri is invalid, cancel current load
        val isValidUri = helper.isUriValid(uri.toString())
        if (!isValidUri) {
            return true
        }

        // uri does not begin with http / https, open in native browser, cancel current load
        val isValidUrlScheme = helper.isValidUrlScheme(uri)
        if (!isValidUrlScheme) {
            helper.openUriInBrowser(context, uri)
            return true
        }

        // uri domain is blacklisted, open in native browser, cancel current load
        val isBlacklistedDomain = helper.isBlacklistedDomain(uri)
        if (isBlacklistedDomain) {
            helper.openUriInBrowser(context, uri)
            return true
        }
        val isPdfUrl =helper.isPdfUrl(uri)
        if (isPdfUrl){
            helper.openUriInBrowser(context,uri)
            return true
        }

        // Instance domain matches, is not an affiliate redirect, continue within current view
        if (instanceDomainString == domain && !isAffiliateRedirect) {
            return false
        }

        val isLocal = helper.containsLocalLink(uri)
        if (isLocal) {
            return false
        }

        // Otherwise, launch URL in MmobBrowser
        try {
            val intent = Intent(context, MmobBrowser::class.java).apply {
                putExtra("uri", uri.toString())
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            return true
        }

        return true
    }
}

