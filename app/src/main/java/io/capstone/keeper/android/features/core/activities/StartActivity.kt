package io.capstone.keeper.android.features.core.activities

import android.content.Intent
import android.os.Bundle
import io.capstone.keeper.android.features.authentication.AuthenticationActivity
import io.capstone.keeper.android.features.shared.components.BaseActivity

class StartActivity: BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this, AuthenticationActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}