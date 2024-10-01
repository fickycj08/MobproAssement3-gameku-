package org.d3if3163.gameku.ui.screen

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import org.d3if3163.gameku.model.Gameku
import org.d3if3163.gameku.model.User
import org.d3if3163.gameku.network.Api
import org.d3if3163.gameku.network.ApiStatus
import org.d3if3163.gameku.network.UserDataStore
import org.d3if3163.gameku.ui.component.MediumText
import org.d3if3163.gameku.ui.component.RegularText
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.d3if3163.gameku.BuildConfig
import org.d3if3163.gameku.R
import org.d3if3163.gameku.ui.component.LargeText
import org.d3if3163.gameku.ui.component.MediumLargeText
import org.d3if3163.gameku.ui.component.SmallText
import org.d3if3163.gameku.ui.theme.CustomGray
import org.d3if3163.gameku.ui.theme.CustomText
import org.d3if3163.gameku.ui.theme.CustomTextBackground
import org.d3if3163.gameku.ui.theme.CustomTitleText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val viewModel: MainViewModel = viewModel()
    val dataStore = UserDataStore(context)
    val errorMessage by viewModel.errorMessage
    val user by dataStore.userFlow.collectAsState(User())
    var showDialog by remember { mutableStateOf(false) }
    var showImgDialog by remember { mutableStateOf(false) }

    var bitmap: Bitmap? by remember { mutableStateOf(null) }
    val launcher = rememberLauncherForActivityResult(CropImageContract()) {
        bitmap = getCroppedImage(context.contentResolver, it)
        if (bitmap != null) showImgDialog = true
    }

    val isUploading by viewModel.isUploading
    val isSuccess by viewModel.querySuccess

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            Toast.makeText(context, "Success!", Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }
    LaunchedEffect(isUploading) {
        if (isUploading) {
            Toast.makeText(context, "Uploading...", Toast.LENGTH_LONG).show()
            viewModel.clearMessage()
        }
    }

    val showList by dataStore.layoutFlow.collectAsState(true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = {
                            if (user.email.isEmpty()) {
                                CoroutineScope(Dispatchers.IO).launch { signIn(context, dataStore) }
                            } else {
                                showDialog = true
                            }
                        }) {
                            if (user.email.isEmpty()) {
                                Icon(
                                    modifier = Modifier.size(30.dp),
                                    painter = painterResource(R.drawable.account_circle),
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            } else {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(user.photoUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    placeholder = painterResource(id = R.drawable.loading_img),
                                    error = painterResource(id = R.drawable.broken_image),
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(shape = CircleShape)
                                )
                            }
                        }
                        MediumLargeText(
                            text = stringResource(id = R.string.app_name),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        IconButton(onClick = {
                            CoroutineScope(Dispatchers.IO).launch {
                                dataStore.saveLayout(!showList)
                            }
                        }) {
                            Icon(
                                painter = painterResource(
                                    if (!showList) R.drawable.baseline_grid_view_24
                                    else R.drawable.baseline_view_list_24
                                ),
                                contentDescription =
                                if (showList) "List"
                                else "Grid",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = topAppBarColors(containerColor = CustomGray)
            )
        },
        floatingActionButton = {
            FloatingActionButton(containerColor = CustomGray, onClick = {
                if (user.email.isNotEmpty() && user.email != "") {
                    val options = CropImageContractOptions(
                        null, CropImageOptions(
                            imageSourceIncludeGallery = true,
                            imageSourceIncludeCamera = true,
                            fixAspectRatio = true
                        )
                    )
                    launcher.launch(options)
                } else {
                    Toast.makeText(context, "Please login first.", Toast.LENGTH_SHORT)
                        .show()
                }
            }) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Tambah Data",
                    tint = Color.White
                )
            }

        },
        containerColor = CustomGray
    ) {
        ScreenContent(modifier = Modifier.padding(it), viewModel, user, showList)


        if (showDialog) {
            ProfilDialog(user = user, onDismissRequest = { showDialog = false }) {
                CoroutineScope(Dispatchers.IO).launch { signOut(context, dataStore) }
                showDialog = false
            }
        }

        if (showImgDialog) {
            ImageDialog(
                bitmap = bitmap,
                onDismissRequest = { showImgDialog = false }) { title, genre, price ->
                viewModel.saveData(user.email, title, genre, price, bitmap!!)
                showImgDialog = false
            }
        }

        LaunchedEffect(errorMessage) {
            if (errorMessage != null) {
                Log.d("MainScreen", "$errorMessage")
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                viewModel.clearMessage()
            }
        }
    }
}

@Composable
private fun ScreenContent(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    user: User,
    showList: Boolean
) {
    val data by viewModel.data
    val status by viewModel.status.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var gamekuData by remember { mutableStateOf<Gameku?>(null) }
    val retrieveErrorMessage by viewModel.errorMessageNoToast

    LaunchedEffect(data) {
        viewModel.retrieveData(user.email)
    }

    LaunchedEffect(user) {
        viewModel.retrieveData(user.email)
    }

    Column(modifier = modifier.fillMaxSize()) {
        if (user.name != "") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 22.dp, top = 16.dp)
            ) {
                RegularText(
                    text = "Hello, ${user.name}",
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
                MediumLargeText(
                    text = "This is Your Ultimate",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(
                        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
                    )
                    .background(Color.White),
            ) {
                when (status) {
                    ApiStatus.LOADING -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = CustomGray)
                        }
                    }

                    ApiStatus.SUCCESS -> {
                        if (showList) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 84.dp, top = 16.dp)
                            ) {
                                item {
                                    MediumText(
                                        text = "Collections",
                                        color = CustomTitleText,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(start = 24.dp)
                                    )
                                }
                                items(data) {
                                    ListItem(data = it) {
                                        gamekuData = it
                                        showDeleteDialog = true
                                    }
                                }
                            }
                        } else {
                            LazyVerticalStaggeredGrid(
                                modifier = Modifier
                                    .fillMaxSize(),
                                columns = StaggeredGridCells.Fixed(2),
                                verticalItemSpacing = 8.dp,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(8.dp, 8.dp, 8.dp, 84.dp)
                            ) {
                                item(span = StaggeredGridItemSpan.FullLine) {
                                    LargeText(
                                        text = "Collections",
                                        color = CustomTitleText,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )
                                }
                                items(data) {
                                    GridItem(it) {
                                        gamekuData = it
                                        showDeleteDialog = true
                                    }
                                }
                            }
                            if (showDeleteDialog) {
                                HapusDialog(
                                    gamekuData!!,
                                    onDismissRequest = { showDeleteDialog = false }) {
                                    viewModel.deleteData(user.email, gamekuData!!.id)
                                    showDeleteDialog = false
                                }
                            }
                        }
                        if (showDeleteDialog) {
                            HapusDialog(
                                gamekuData!!,
                                onDismissRequest = { showDeleteDialog = false }) {
                                viewModel.deleteData(user.email, gamekuData!!.id)
                                showDeleteDialog = false
                            }
                        }

                    }

                    ApiStatus.FAILED -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (user.email.isEmpty()) {
                                MediumText(
                                    text = "Log in to upload.",
                                    color = CustomText,
                                    fontWeight = FontWeight.SemiBold
                                )
                            } else {
                                if (retrieveErrorMessage != null) {
                                    when (retrieveErrorMessage) {
                                        "Anda belum memasukkan data." -> {
                                            Image(
                                                painter = painterResource(id = R.drawable.empty_state_gameku),
                                                contentDescription = "Empty Data Image",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp)
                                                    .clip(shape = RoundedCornerShape(15.dp))
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            MediumText(
                                                text = "You haven't uploaded any yet.",
                                                modifier = Modifier.fillMaxWidth(),
                                                textAlign = TextAlign.Center,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }

                                        else -> {
                                            MediumText(
                                                text = retrieveErrorMessage!!,
                                                color = CustomText
                                            )
                                            Button(
                                                onClick = { viewModel.retrieveData(user.email) },
                                                modifier = Modifier.padding(top = 16.dp),
                                                contentPadding = PaddingValues(
                                                    horizontal = 32.dp,
                                                    vertical = 16.dp
                                                ),
                                                colors = buttonColors(containerColor = CustomGray)
                                            ) {
                                                RegularText(text = "Try again", color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListItem(data: Gameku, onClick: () -> Unit) {
    Card(
        onClick = {},
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
        ),
        border = BorderStroke(1.dp, CustomGray),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(modifier = Modifier.width(16.dp))
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(Api.getImageUrl(data.image_id))
                    .crossfade(true)
                    .build(),
                contentDescription = "Gambar ${data.title}",
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.loading_img),
                error = painterResource(id = R.drawable.broken_image),
                modifier = Modifier
                    .size(130.dp, 145.dp)
                    .background(CustomTextBackground, RoundedCornerShape(15.dp))
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RegularText(
                        modifier = Modifier.width(125.dp),
                        text = data.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.SemiBold,
                        color = CustomTitleText
                    )

                    IconButton(onClick = { onClick() }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete Icon",
                            tint = CustomGray,
                            modifier = Modifier
                                .background(CustomTextBackground, RoundedCornerShape(30.dp))
                                .padding(vertical = 4.dp, horizontal = 12.dp)
                        )
                    }
                }
                SmallText(
                    text = data.genre,
                    modifier = Modifier
                        .background(CustomTextBackground, RoundedCornerShape(30.dp))
                        .padding(vertical = 4.dp, horizontal = 12.dp)
                )
                MediumText(
                    modifier = Modifier.width(125.dp),
                    text = formatRupiah(data.price),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GridItem(data: Gameku, onClick: () -> Unit) {
    Card(
        onClick = {},
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
        ),
        border = BorderStroke(1.dp, CustomGray)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(Api.getImageUrl(data.image_id))
                .crossfade(true)
                .build(),
            contentDescription = "Gambar ${data.image_id}",
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.loading_img),
            error = painterResource(id = R.drawable.broken_image),
            modifier = Modifier
                .fillMaxWidth()
                .background(CustomTextBackground, RoundedCornerShape(15.dp))
                .padding(16.dp)
                .clip(RoundedCornerShape(10.dp))
        )
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RegularText(
                    text = data.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold,
                    color = CustomTitleText
                )
                IconButton(onClick = { onClick() }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Icon",
                        tint = CustomGray
                    )
                }
            }
            RegularText(text = data.genre, fontWeight = FontWeight.Light)
            SmallText(text = formatRupiah(data.price), fontWeight = FontWeight.SemiBold)
        }
    }
}

@SuppressLint("DefaultLocale")
fun formatRupiah(amount: Int): String {
    val formattedAmount = String.format("%,d", amount).replace(',', '.')
    return "Rp. $formattedAmount"
}

private fun shareData(context: Context, message: String) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)
    }
    if (shareIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(shareIntent)
    }
}

private suspend fun signIn(
    context: Context,
    dataStore: UserDataStore
) {
    val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(BuildConfig.API_KEY)
        .build()

    val request: GetCredentialRequest = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    try {
        val credentialManager = CredentialManager.create(context)
        val result = credentialManager.getCredential(context, request)
        handleSignIn(result, dataStore)
    } catch (e: GetCredentialException) {
        Log.e("SIGN-IN", "Error: ${e.errorMessage}")
    }
}

private suspend fun handleSignIn(
    result: GetCredentialResponse,
    dataStore: UserDataStore
) {
    val credential = result.credential
    if (credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
    ) {
        try {
            val googleId = GoogleIdTokenCredential.createFrom(credential.data)
            val nama = googleId.displayName ?: ""
            val email = googleId.id
            val photoUrl = googleId.profilePictureUri.toString()
            dataStore.saveData(User(nama, email, photoUrl))
        } catch (e: GoogleIdTokenParsingException) {
            Log.e("SIGN-IN", "Error: ${e.message}")
        }
    } else {
        Log.e("SIGN-IN", "Error: unrecognized custom credential type")
    }
}

private suspend fun signOut(context: Context, dataStore: UserDataStore) {
    try {
        val credentialManager = CredentialManager.create(context)
        credentialManager.clearCredentialState(
            ClearCredentialStateRequest()
        )
        dataStore.saveData(User())
    } catch (e: ClearCredentialException) {
        Log.e("SIGN-IN", "Error: ${e.errorMessage}")
    }
}

private fun getCroppedImage(
    resolver: ContentResolver,
    result: CropImageView.CropResult
): Bitmap? {
    if (!result.isSuccessful) {
        Log.e("IMAGE", "Error: ${result.error}")
        return null
    }
    val uri = result.uriContent ?: return null

    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
        MediaStore.Images.Media.getBitmap(resolver, uri)
    } else {
        val source = ImageDecoder.createSource(resolver, uri)
        ImageDecoder.decodeBitmap(source)
    }
}


@Preview
@Composable
private fun Prev() {
    MainScreen()
}