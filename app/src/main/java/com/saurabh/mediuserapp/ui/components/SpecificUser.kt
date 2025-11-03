package com.saurabh.mediuserapp.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.saurabh.mediuserapp.network.response.User


@Composable
fun SpecificUser(user : User) {
    val userDetails = listOf(
        "ID" to user.id.toString(),
        "User ID" to user.user_id.toString(),
        "Password" to user.password.toString(),
        "Date of Account Creation" to user.date_of_account_creation.toString(),
        "isApproved" to user.isApproved.toString(),
        "Block" to user.block.toString(),
        "Name" to user.name.toString(),
        "Phone Number" to user.phone_number.toString(),
        "Email" to user.email.toString(),
        "Pin Code" to user.pin_code.toString(),
        "Address" to user.address.toString(),

        )
    LazyColumn (modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)){
        items(userDetails) {(label, value) ->
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)) {
                Text(text = label,style = MaterialTheme.typography.labelMedium)
                Text(text = value,style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 4.dp))

            }
            HorizontalDivider()
        }
    }

}