package com.example.mobiili

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import coil.compose.rememberAsyncImagePainter
import com.example.mobiili.data.MessageData
import com.example.mobiili.database.AppDatabase
import com.example.mobiili.database.SampleData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeTutorialTheme {
                val messages = remember { mutableStateOf(listOf<Message>()) }
                val context = LocalContext.current

                // Load data from database when app starts
                LaunchedEffect(Unit) {
                    CoroutineScope(Dispatchers.IO).launch {
                        initializeSampleData(context)
                        messages.value = loadMessagesFromDatabase(context)
                    }
                }

                AppNavigation(messages)
            }
        }
    }
}

//Load messages
private suspend fun loadMessagesFromDatabase(context: Context): List<Message> {
    val database = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "app_database"
    ).build()

    val messageDataDao = database.messageDataDao()
    val messages = messageDataDao.getAllMessages().map {
        Message(it.author, it.body, it.imageUrl)
    }
    println("Loaded messages: $messages")
    return messages
}

//Sample data to database
suspend fun initializeSampleData(context: Context) {
    val database = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "app_database"
    ).build()

    val messageDataDao = database.messageDataDao()

    if (messageDataDao.getAllMessages().isEmpty()) {
        // Insert sample data into the database
        SampleData.conversationSample.forEach { message ->
            val messageData = MessageData(
                author = message.author,
                body = message.body,
                imageUrl = message.imageUrl
            )
            messageDataDao.insertMessageData(messageData)
            println("Inserted message: $messageData")
        }
    } else {
        println("Database already contains data")
    }
}

// Main View
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView(navController: NavController, messages: List<Message>) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text(text = "Conversations", style = MaterialTheme.typography.bodyLarge) },
            actions = {
                IconButton(onClick = { navController.navigate("userProfileView") }) {
                    Image(
                        painter = painterResource(id = R.drawable.settings_icon),
                        contentDescription = "Settings",
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )

        // Show conversations
        Conversation(messages = messages)
    }
}

//Second view
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecondView(navController: NavController, messages: MutableState<List<Message>>) {
    val context = LocalContext.current
    var author by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(key1 = messages.value) {
        val firstMessage = messages.value.firstOrNull()
        if (firstMessage != null) {
            author = firstMessage.author
            imageUri = Uri.parse(firstMessage.imageUrl)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(text = "Edit Profile", style = MaterialTheme.typography.bodyLarge)
                }
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Image(
                        painter = painterResource(id = R.drawable.left_arrow),
                        contentDescription = "Back",
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )

        // Display Profile Picture
        Text(
            text = "User:",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)
        )
        ImagePicker(imageUri) { uri ->
            imageUri = uri
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Username input field
        Text(
            text = "Username",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)
        )
        TextField(
            value = author,
            onValueChange = { author = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Save changes button
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = {
                // Update the messages state with the new author and image URL
                messages.value = messages.value.map {
                    it.copy(author = author, imageUrl = imageUri.toString())
                }
                saveAuthorData(context, author, imageUri.toString())
                navController.popBackStack() // Go back to the previous screen after saving
            }) {
                Text("Save Changes")
            }
        }
    }
}

//Save new information
fun saveAuthorData(context: Context, newAuthor: String, newImageUri: String) {
    val database = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "app_database"
    ).build()

    val messageDataDao = database.messageDataDao()

    CoroutineScope(Dispatchers.IO).launch {
        // Update all messages in the database with new author and image URL
        messageDataDao.updateAllMessages(newAuthor, newImageUri)
    }
}

//Profile picture change
@Composable
fun ImagePicker(currentImageUri: Uri?, onImagePicked: (Uri) -> Unit) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            val file = File(context.filesDir, "picked_image.jpg")
            val outputStream = file.outputStream()
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            onImagePicked(Uri.fromFile(file))
        }
    }

    Box(
        modifier = Modifier
            .padding(16.dp)
            .clickable {
                launcher.launch("image/*")
            }
    ) {
        Image(
            painter = rememberAsyncImagePainter(currentImageUri ?: "android.resource://com.example.mobiili/drawable/john_smith2"),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
        )
    }
}

//Navigation for app
@Composable
fun AppNavigation(messages: MutableState<List<Message>>) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "mainView") {
        composable("mainView") {
            MainView(navController = navController, messages = messages.value)
        }
        composable("userProfileView") {
            SecondView(navController = navController, messages = messages)
        }
    }
}

//Message structure
data class Message(val author: String, val body: String, val imageUrl: String)


@Composable
fun MessageCard(msg: Message) {
    Row(
        modifier = Modifier
            .padding(all = 8.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(msg.imageUrl),
            contentDescription = "Contact profile picture",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
        )

        Spacer(modifier = Modifier.width(8.dp))

        var isExpanded by remember { mutableStateOf(false) }

        val surfaceColor by animateColorAsState(
            if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        )

        Column(modifier = Modifier.clickable { isExpanded = !isExpanded }) {
            Text(
                text = msg.author,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                shape = MaterialTheme.shapes.medium,
                shadowElevation = 1.dp,
                color = surfaceColor,
                modifier = Modifier.animateContentSize().padding(1.dp)
            ) {
                Text(
                    text = msg.body,
                    modifier = Modifier.padding(all = 4.dp),
                    maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun Conversation(messages: List<Message>) {
    LazyColumn {
        items(messages) { message ->
            MessageCard(message)
        }
    }
}

@Preview
@Composable
fun PreviewApp() {
    ComposeTutorialTheme {
        val sampleMessages = remember { mutableStateOf(SampleData.conversationSample) }
        AppNavigation(messages = sampleMessages)
    }
}

@Composable
fun ComposeTutorialTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            primary = Color(0xFF6200EE),
            surface = Color(0xFFBB86FC)
        ),
        typography = MaterialTheme.typography,
        content = content
    )
}