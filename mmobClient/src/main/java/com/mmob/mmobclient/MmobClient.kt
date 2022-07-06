package com.mmob.mmobclient

import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.serialization.Serializable
import java.net.URLEncoder
import kotlin.reflect.full.memberProperties

typealias MmobView = WebView

class MmobClient(private var mmobView: MmobView) {
    fun loadIntegration(integration: MmobIntegrationConfiguration, customerInfo: MmobCustomerInfo) {
        val data = "&${encodeIntegrationConfiguration(integration)}&customer_info${encodeCustomerInfo(customerInfo)}"

        startWebView(mmobView, "https://marketplace.mmobstars.com/boot", data)
    }

    fun loadDistribution(distribution: MmobDistribution, customerInfo: MmobCustomerInfo) {
        val data = "configuration${encodeDistributionConfiguration(distribution)}&customer_info${encodeCustomerInfo(customerInfo)}"

        startWebView(mmobView, "https://marketplace.mmobstars.com/tpp/distribution/boot", data)
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

    @Serializable
    data class MmobIntegrationConfiguration(val cp_id: String, val cp_deployment_id: String, val environment: String = "production") {
    }

    @Serializable
    data class MmobDistribution(val distribution: Configuration) {
        @Serializable
        data class Configuration(val distribution_id: String, val environment: String = "production", val identifier_value: String, val identifier_type: String) {
        }
    }

    @Serializable
    data class MmobCustomerInfo(val customerInfo: Configuration) {
        @Serializable
        data class Configuration(val email: String, val first_name: String, val surname: String, val gender: String, val title: String, val building_number: String, val address_1: String, val town_city: String, val postcode: String, val dob: String) {
        }
    }
}

