package com.example.nurungji

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nurungji.data.InventoryItem
import com.example.nurungji.ui.InventoryViewModel
import com.example.nurungji.ui.theme.NurungjiTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import java.nio.file.WatchEvent

class MainActivity : ComponentActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    private var isLoggedIn by mutableStateOf(false)

    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

        try {
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: Exception) {
            Log.e("LOGIN", "Google sign in failed", e)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        isLoggedIn = auth.currentUser != null

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setContent {
            NurungjiTheme {
                if (isLoggedIn) {
                    InventoryScreen(
                        userName = auth.currentUser?.displayName ?: "사용자",
                        onLogout = { signOut() }
                    )
                } else {
                    LoginScreen(
                        onGoogleLoginClick = { signIn() }
                    )
                }
            }
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Log.d("LOGIN", "로그인 성공: ${user?.displayName}, ${user?.email}, uid=${user?.uid}")
                    isLoggedIn = true
                } else {
                    Log.e("LOGIN", "로그인 실패", task.exception)
                }
            }
    }

    private fun signOut() {
        auth.signOut()
        googleSignInClient.revokeAccess().addOnCompleteListener {
            isLoggedIn = false
        }
    }
}

@Composable
fun LoginScreen(
    onGoogleLoginClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "구글 로그인",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onGoogleLoginClick) {
            Text("Google로 로그인")
        }
    }
}

@Composable
fun InventoryScreen(
    userName: String,
    onLogout: () -> Unit,
    viewModel: InventoryViewModel = viewModel()
) {
    val inventoryItems by viewModel.inventoryItems.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var itemName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadInventory()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "안녕하세요, $userName 님",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = onLogout) {
            Text("로그아웃")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "재고 추가",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = itemName,
            onValueChange = { itemName = it },
            label = { Text("상품명") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("카테고리") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = quantityText,
            onValueChange = { quantityText = it },
            label = { Text("수량") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                val quantity = quantityText.toLongOrNull()

                if (itemName.isNotBlank() && category.isNotBlank() && quantity != null) {
                    viewModel.addInventory(
                        itemName = itemName,
                        category = category,
                        quantity = quantity
                    )

                    itemName = ""
                    category = ""
                    quantityText = ""
                }
            }
        ) {
            Text("재고 추가")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "재고 목록",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(12.dp))

        errorMessage?.let {
            Text(text = "오류: $it")
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (inventoryItems.isEmpty()) {
            Text(text = "재고가 없습니다.")
        } else {
            LazyColumn {
                items(inventoryItems) { item ->
                    InventoryItemCard(
                        item=item,
                        onDelete= {
                            viewModel.deleteInventory(item.documentId)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun InventoryItemCard(
    item: InventoryItem,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(text = "상품명: ${item.itemName}")
        Text(text = "카테고리: ${item.category}")
        Text(text = "수량: ${item.quantity}")
        Text(text = "사용자: ${item.userId}")

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick=onDelete){
            Text("삭제")
        }
    }
}