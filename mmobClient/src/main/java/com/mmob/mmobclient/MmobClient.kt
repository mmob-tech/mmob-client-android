package com.mmob.mmobclient

import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient
import java.net.URLEncoder
import kotlin.reflect.full.memberProperties

typealias MmobView = WebView

class MmobClient(private var mmobView: MmobView, private val context: Context) {
    fun loadIntegration(integration: MmobIntegrationConfiguration, customerInfo: MmobCustomerInfo) {
        val data = "&${encodeIntegrationConfiguration(integration)}&customer_info${encodeCustomerInfo(customerInfo)}"

        startWebView(mmobView, getUrl(integration.environment), data)
    }

    fun loadDistribution(distribution: MmobDistribution, customerInfo: MmobCustomerInfo) {
        val data = "configuration${encodeDistributionConfiguration(distribution)}&customer_info${encodeCustomerInfo(customerInfo)}"

        startWebView(mmobView, getUrl(distribution.distribution.environment, "tpp/distribution/boot"), data)
    }

    private fun getUrl(environment: String, suffix: String = "boot"): String {
        val localUrl = "http://localhost:3100/$suffix"
        val devUrl = "https://client-ingress.dev.mmob.com/$suffix"
        val stagUrl = "https://client-ingress.stag.mmob.com/$suffix"
        val prodUrl = "https://client-ingress.prod.mmob.com/$suffix"

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
                val query = "${prop.name}=${URLEncoder.encode(prop.get(integration).toString(), "UTF-8")}"
                queryStringArray.add(query)
            }
        }

        return queryStringArray.joinToString("&")
    }

    private fun encodeDistributionConfiguration(distribution: MmobDistribution): String {
        val queryStringArray = ArrayList<String>()

        for (prop in MmobDistribution.Configuration::class.memberProperties) {
            if (prop.get(distribution.distribution) != null) {
                val query = "[${prop.name}]=${URLEncoder.encode(prop.get(distribution.distribution).toString(), "UTF-8")}"
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
                val query = "[${prop.name}]=${URLEncoder.encode(prop.get(customerInfo.customerInfo).toString(), "UTF-8")}"
                queryStringArray.add(query)
            }
        }

        return queryStringArray.joinToString("&customer_info")
    }

    private fun startWebView(mmobView: WebView, url: String, data: String) {
        // Enable Javascript
        val webSettings = mmobView.settings
        webSettings.javaScriptEnabled = true

        // Force links and redirects to open in the WebView instead of in a browser
        mmobView.webViewClient = WebViewClient()

        // Post data to url
        mmobView.postUrl(url, data.toByteArray())
    }

    data class MmobIntegrationConfiguration(
        val cp_id: String,
        val cp_deployment_id: String,
        val environment: String = "production"
    ) {
    }

    data class MmobDistribution(
        val distribution: Configuration) {
        data class Configuration(
            val distribution_id: String,
            val environment: String = "production"
        ) {
        }
    }

    data class MmobCustomerInfo(
        val customerInfo: Configuration) {
        data class Configuration(
            val email: String? = null,
            val first_name: String? = null,
            val surname: String? = null,
            val gender: String? = null,
            val title: String? = null,
            val building_number: String? = null,
            val address_1: String? = null,
            val town_city: String? = null,
            val postcode: String? = null,
            val dob: String? = null
        ) {
        }
    }
}

