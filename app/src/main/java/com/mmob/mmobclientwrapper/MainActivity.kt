package com.mmob.mmobclientwrapper

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mmob.mmobclient.InstanceDomain
import com.mmob.mmobclient.MmobClient
import com.mmob.mmobclient.MmobView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mmobView: MmobView = findViewById(R.id.mmob_view)
        val client = MmobClient(mmobView, applicationContext, instanceDomain = InstanceDomain.EFNETWORK)

        val integration = MmobClient.MmobIntegrationConfiguration(
            cp_id = "cp_TMaiVSyyzBx2rZqF-PqdY",
            cp_deployment_id = "cpd_xH1KQOhFh_hIIVKTCBJF5",
            environment = "stag"
        )

        val customerInfo = MmobClient.MmobCustomerInfo(
            customerInfo = MmobClient.MmobCustomerInfo.Configuration(
                email = "john.smith@example.com",
                first_name = "John",
                surname = "Smith",
                title = "Mr",
                building_number = "Flat 81",
                address_1 = "Marconi Square",
                town_city = "Chelmsford",
                postcode = "CM1 1XX"
            )
        )

        client.loadIntegration(integration = integration, customerInfo = customerInfo)
    }
}