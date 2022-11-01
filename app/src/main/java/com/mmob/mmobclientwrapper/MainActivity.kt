package com.mmob.mmobclientwrapper

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mmob.mmobclient.MmobClient
import com.mmob.mmobclient.MmobView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mmobView: MmobView = findViewById(R.id.mmob_view)

        val client = MmobClient(mmobView, applicationContext)

        val distribution = MmobClient.MmobDistribution(
            distribution = MmobClient.MmobDistribution.Configuration(
                distribution_id = "tpd_Ss2Z0lvWtG5qJ9BQLfs0A",
                environment = "stag"
            )
        )

        val customerInfo = MmobClient.MmobCustomerInfo(
            customerInfo = MmobClient.MmobCustomerInfo.Configuration(
                email = "corey.hudson@example.com",
                first_name = "Corey",
                surname = "Hudson",
                gender = "male",
                title = "Mr",
                building_number = "8",
                address_1 = "Brandon Grove",
                town_city = "Newcastle Upon Tyne",
                postcode = "NE2 1PA",
                dob = "1946-03-26T20:49:19.388Z"
            )
        )

        client.loadDistribution(distribution = distribution, customerInfo = customerInfo)
    }
}