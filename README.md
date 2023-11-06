# MMOB Android Client 📱

The MMOB Android Client works across multiple Android versions from **5.0 Lollipop** to **12.0 Snow Cone**

## Instructions to implement

Create a reference in your XML layout file for a WebView with id: `mmob_view`

### activity_mmob_client.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MmobClientActivity">

    <WebView
        android:id="@+id/mmob_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

### MmobClientActivity.swift

Create a Kotlin file containing the MmobClientActivity with a reference to the XML file `activity_mmob_client.xml`

```kotlin
//
//  MmobClientActivity.kt
//

package com.mmob.mmobclientwrapper

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mmob.mmobclient.MmobClient
import com.mmob.mmobclient.MmobView

class MmobViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mmob_client)

        val mmobView: MmobView = findViewById(R.id.mmob_view)
        val client = MmobClient(mmobView, applicationContext)

        val integration = MmobClient.MmobIntegrationConfiguration(
            cp_id = "YOUR_CP_ID_HERE",
            cp_deployment_id = "YOUR_CP_DEPLOYMENT_ID_HERE",
            locale = "en_GB",
            signature = "SIGNATURE_HERE"
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
```
