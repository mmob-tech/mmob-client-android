package com.mmob.mmobclient

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import java.net.URLEncoder
import kotlin.reflect.full.memberProperties

typealias MmobView = WebView

class MmobClient(
    private val mmobView: MmobView,
    private val context: Context,
    private val instanceDomain: InstanceDomain = InstanceDomain.MMOB
) {
    fun loadIntegration(integration: MmobIntegrationConfiguration, customerInfo: MmobCustomerInfo) {
        val data = "&${encodeIntegrationConfiguration(integration)}${
            encodeCustomerInfo(customerInfo)
        }"

        startWebView(mmobView, getUrl(integration.environment, instanceDomain), data)
    }

    fun loadDistribution(distribution: MmobDistribution, customerInfo: MmobCustomerInfo) {
        val data = "configuration${encodeDistributionConfiguration(distribution)}${
            encodeCustomerInfo(customerInfo)
        }"

        startWebView(
            mmobView,
            getUrl(distribution.distribution.environment, instanceDomain, "tpp/distribution/boot"),
            data
        )
    }

    private fun getUrl(
        environment: String, instanceDomain: InstanceDomain, suffix: String = "boot"
    ): String {
        val parsedInstanceDomain = MmobClientHelper().getInstanceDomain(instanceDomain)

        val localUrl = "http://10.0.2.2:3100/$suffix"
        val devUrl = "https://client-ingress.dev.$parsedInstanceDomain/$suffix"
        val stagUrl = "https://client-ingress.stag.$parsedInstanceDomain/$suffix"
        val prodUrl = "https://client-ingress.prod.$parsedInstanceDomain/$suffix"

        return when (environment) {
            "local" -> localUrl
            "dev" -> devUrl
            "stag" -> stagUrl
            else -> {
                prodUrl
            }
        }
    }

    private fun encodeIntegrationConfiguration(integration: MmobIntegrationConfiguration): String {
        val queryStringArray = ArrayList<String>()

        for (prop in MmobIntegrationConfiguration::class.memberProperties) {
            if (prop.get(integration) != null) {
                val query =
                    "${prop.name}=${URLEncoder.encode(prop.get(integration).toString(), "UTF-8")}"
                queryStringArray.add(query)
            }
        }

        return queryStringArray.joinToString("&")
    }

    private fun encodeDistributionConfiguration(distribution: MmobDistribution): String {
        val queryStringArray = ArrayList<String>()

        for (prop in MmobDistribution.Configuration::class.memberProperties) {
            if (prop.get(distribution.distribution) != null) {
                val query = "[${prop.name}]=${
                    URLEncoder.encode(
                        prop.get(distribution.distribution).toString(), "UTF-8"
                    )
                }"
                queryStringArray.add(query)
            }
        }

        queryStringArray.add("[identifier_type]=android")
        queryStringArray.add("[identifier_value]=${context.packageName}")

        return queryStringArray.joinToString("&configuration")
    }

    private fun encodeCustomerInfo(customerInfo: MmobCustomerInfo): String {
        val queryStringArray = ArrayList<String>()

        for (prop in MmobCustomerInfo.Configuration::class.memberProperties) {
            if (prop.get(customerInfo.customerInfo) != null) {
                val query = "[${prop.name}]=${
                    URLEncoder.encode(
                        prop.get(customerInfo.customerInfo).toString(), "UTF-8"
                    )
                }"
                queryStringArray.add(query)
            }
        }

        if (queryStringArray.isEmpty()) {
            return ""
        }

        return "&${queryStringArray.joinToString("&")}"
    }

    private fun startWebView(mmobView: WebView, url: String, data: String) {
        // Enable Javascript
        val webSettings = mmobView.settings
        webSettings.javaScriptEnabled = true

        // Force links and redirects to open in the WebView instead of in a browser
        mmobView.webViewClient = MmobViewClient(mmobView.context, instanceDomain)

        // Post data to url
        mmobView.postUrl(url, data.toByteArray())
    }

    data class MmobIntegrationConfiguration(
        val cp_id: String, val cp_deployment_id: String, val environment: String = "production"
    )

    data class MmobDistribution(
        val distribution: Configuration
    ) {
        data class Configuration(
            val distribution_id: String, val environment: String = "production"
        )
    }

    data class MmobCustomerInfo(
        val customerInfo: Configuration
    ) {
        data class Configuration(
            val email: String? = null,
            val first_name: String? = null,
            val surname: String? = null,
            val title: String? = null,
            val preferred_name: String? = null,
            val dob: String? = null,
            val postcode: String? = null,
            val building_number: String? = null,
            val address_1: String? = null,
            val address_2: String? = null,
            val address_3: String? = null,
            val town_city: String? = null,
            val country: String? = null,
            val country_of_residence: String? = null,
            val nationality: String? = null,
            val gender: String? = null,
            val relationship_status: String? = null,
            val number_of_children: Number? = null,
            val ages_of_children: Number? = null,
            val partner_first_name: String? = null,
            val partner_surname: String? = null,
            val partner_dob: String? = null,
            val partner_sex: String? = null,
            val relationship_to_partner: String? = null,
            val smoker: String? = null,
            val number_of_cigarettes_per_week: Number? = null,
            val drinker: String? = null,
            val number_of_units_per_week: Number? = null,
            val passport_number: String? = null,
            val national_insurance_number: String? = null,
            val phone_number: String? = null,
            val mobile_number: String? = null,
        )
    }
}

private class MmobViewClient(private val context: Context, private val instanceDomain: InstanceDomain) :
    WebViewClient() {
    @Deprecated("shouldOverrideUrlLoading is deprecated, providing support for older versions of Android")
    override fun shouldOverrideUrlLoading(view: MmobView?, url: String): Boolean {
        return handleUri(Uri.parse(url))
    }

    @TargetApi(Build.VERSION_CODES.N)
    override fun shouldOverrideUrlLoading(view: MmobView?, request: WebResourceRequest): Boolean {
        return handleUri(request.url)
    }

    private fun handleUri(uri: Uri): Boolean {
        // Do not override whitelisted domains; let MmobView load the page
        val parsedInstanceDomain = MmobClientHelper().getInstanceDomain(instanceDomain)
        val domain = MmobClientHelper().getRootDomain(uri)

        if (parsedInstanceDomain == domain) {
            return false
        }

        // Otherwise, launch URL in MmobBrowser
        try {
            val intent = Intent(context, MmobBrowser::class.java).apply {
                putExtra("uri", uri.toString())
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            null
        }

        return true
    }
}

