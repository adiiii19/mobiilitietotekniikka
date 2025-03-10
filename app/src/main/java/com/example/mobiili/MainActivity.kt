package com.example.mobiili

import android.Manifest
import androidx.compose.ui.platform.LocalContext
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
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



class MainActivity : ComponentActivity(), SensorEventListener {
    private var sensorManager: SensorManager? = null
    private var lightSensor: Sensor? = null
    private var currentLux: Float = 0f
    private var isDarkTheme = mutableStateOf(false)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize sensor manager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)

        // Listener for light sensor events
        sensorManager?.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)

        setContent {
            fun startMapActivity(context: Context) {
                val intent = Intent(context, MapActivity::class.java)
                context.startActivity(intent)
            }
            ComposeTutorialTheme(isDarkTheme.value) {
                val messages = remember { mutableStateOf(listOf<Message>()) }
                val context = LocalContext.current

                // Load data from database when app starts
                LaunchedEffect(Unit) {
                    CoroutineScope(Dispatchers.IO).launch {
                        initializeSampleData(context)
                        messages.value = loadMessagesFromDatabase(context)
                    }
                }

                AppNavigation(messages, { startMapActivity(context) })
            }
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "channel_id",
                "channel_name",
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Create notification channel (only needed for Android 8.0 and higher)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "lux_channel_id", "Lux Notifications", NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null && event.sensor.type == Sensor.TYPE_LIGHT) {
            // Get the current lux value
            currentLux = event.values[0]

            // Check if lux is above 20000
            if (currentLux > 20000 && !isDarkTheme.value) {
                isDarkTheme.value = true // Switch to dark theme
                sendLuxNotification("High light detected: $currentLux lux")
            } else if (currentLux <= 20000 && isDarkTheme.value) {
                isDarkTheme.value = false // Switch to light theme
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed
    }
    // Send notification when high light is detected
    private fun sendLuxNotification(message: String) {
        val notificationManager = NotificationManagerCompat.from(this)

        // Check if the necessary permission is granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return // Permission not granted, return early
            }
        }

        // Create an Intent to open the app when the notification is clicked
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // Build the notification
        val notification = NotificationCompat.Builder(this, "lux_channel_id")
            .setSmallIcon(R.drawable.notification_image) // Replace with your app icon
            .setContentTitle("High Light Alert")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager?.unregisterListener(this) // Unregister sensor listener
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView(navController: NavController, messages: MutableState<List<Message>>, startMapActivity: () -> Unit) {
    val context = LocalContext.current
    var newMessageText by remember { mutableStateOf("") }
    var isSendingMessage by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
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
            Conversation(messages = messages.value)

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = newMessageText,
                    onValueChange = { newMessageText = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    placeholder = { Text("Type a message...") }
                )

                Button(
                    onClick = {
                        if (newMessageText.isNotBlank()) {
                            isSendingMessage = true
                        }
                    }
                ) {
                    Text("Send")
                }
            }

            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Button(
                    onClick = {
                        startMapActivity()
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .size(56.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.placeholder),
                        contentDescription = "Placeholder",
                        modifier = Modifier.size(56.dp)
                    )
                }
            }
        }
    }

    LaunchedEffect(isSendingMessage) {
        if (isSendingMessage) {
            val firstMessage = messages.value.firstOrNull()
            val author = firstMessage?.author ?: "User"
            val imageUri = firstMessage?.imageUrl ?: "defaultImageUri"

            val newMessage = Message(author = author, body = newMessageText, imageUrl = imageUri)
            Log.d("MainView", "New message: $newMessage")
            saveMessageToDatabase(context, newMessage)

            // Update the UI
            messages.value = messages.value + newMessage
            Log.d("MainView", "Updated messages: ${messages.value}")

            newMessageText = "" // Clear the text field
            isSendingMessage = false
        }
    }
}

suspend fun saveMessageToDatabase(context: Context, message: Message) {
    val database = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "app_database"
    ).build()

    val messageDataDao = database.messageDataDao()
    val messageData = MessageData(
        author = message.author,
        body = message.body,
        imageUrl = message.imageUrl
    )
    messageDataDao.insertMessageData(messageData)
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

    // Notification permission launcher
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            sendNotification(context)
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
            text = "Username:",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)
        )
        TextField(
            value = author,
            onValueChange = { author = it },
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.White,
                cursorColor = Color.Blue,
                focusedIndicatorColor = Color.Red,
                unfocusedIndicatorColor = Color.Gray
            ),
            modifier = Modifier.fillMaxWidth().padding(16.dp)

        )

        Spacer(modifier = Modifier.height(16.dp))

        // Save changes button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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

        Spacer(modifier = Modifier.height(16.dp))

        // Enable notifications button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        sendNotification(context)
                    }
                } else {
                    sendNotification(context)
                }
            }) {
                Text("Enable Notifications")
            }
        }
    }
}

//
fun sendNotification(context: Context) {
    val notificationManager = NotificationManagerCompat.from(context)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return // Exit if permission is not granted
        }
    }

    // Create an Intent to open the app when notification is clicked
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }

    val pendingIntent = PendingIntent.getActivity(
        context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(context, "channel_id")
        .setSmallIcon(R.drawable.notification_image)
        .setContentTitle("Notifications Enabled")
        .setContentText("If lighting is high, colors change!")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build()

    notificationManager.notify(1, notification)
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

@Composable
fun AppNavigation(messages: MutableState<List<Message>>, startMapActivity: () -> Unit) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "mainView") {
        composable("mainView") {
            MainView(navController = navController, messages = messages, startMapActivity = startMapActivity)
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
    val isDarkTheme = remember { mutableStateOf(false) } // Default to light theme for preview

    ComposeTutorialTheme(isDarkTheme.value) {
        val sampleMessages = remember { mutableStateOf(SampleData.conversationSample) }
        AppNavigation(sampleMessages) { }
    }
}

@Composable
fun ComposeTutorialTheme(isDarkTheme: Boolean, content: @Composable () -> Unit) {
    val colors = if (isDarkTheme) {
        darkColorScheme(
            primary = Color(0xFF006400),
            surface = Color(0xFF00FF00),
            onPrimary = Color.Black,
            onSurface = Color.Black,
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF6200EE),
            surface = Color(0xFFBB86FC),
            onPrimary = Color.Black,
            onSurface = Color.Black,
            background = Color.White
        )
    }

    MaterialTheme(
        colorScheme = colors,
        typography = MaterialTheme.typography,
        content = content
    )
}