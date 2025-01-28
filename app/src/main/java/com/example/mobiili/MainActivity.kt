import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.res.painterResource
import com.example.mobiili.R
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
import com.example.mobiili.SampleData
import androidx.compose.foundation.clickable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeTutorialTheme {
                AppNavigation()
            }
        }
    }
}
// Main View
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()

    ){
    TopAppBar(
            title = { Text(text="", style = MaterialTheme.typography.bodyLarge)},
            actions = {
                IconButton(
                    onClick = {
                        // Navigate to the second view when clicked
                        navController.navigate("secondView")
                    }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.settings_icon),
                        contentDescription = "Settings",
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
                )
            )
        // content
        Conversation(SampleData.conversationSample)
    }
}

// Second View
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecondView(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize()) {
        // TopAppBar with the navigation icon aligned to the left
        TopAppBar(
            title = { Text(text = "", style = MaterialTheme.typography.bodyLarge) },
            navigationIcon = {

                IconButton(
                    onClick = {
                        // back navigation
                        navController.popBackStack()
                    }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.left_arrow),
                        contentDescription = "Back",
                        modifier = Modifier.size(32.dp)
                    )
                }
            },

            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "User:",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Image(
                painter = painterResource(R.drawable.john_smith2),
                contentDescription = "Contact profile picture",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .padding(bottom = 16.dp)
            )
        }
    }
}

// navigation logic
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "mainView") {
        composable("mainView") { MainView(navController) }
        composable("secondView") { SecondView(navController) }
    }
}

data class Message(val author: String, val body: String)

@Composable
fun MessageCard(msg: Message) {
    Row(
        modifier = Modifier
            .padding(all = 8.dp)

    ){
    Image(
            painter = painterResource(R.drawable.john_smith2),
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

// Preview
@Preview
@Composable
fun PreviewApp() {
    ComposeTutorialTheme {
        AppNavigation()
    }
}

@Composable
fun ComposeTutorialTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            primary = Color(0xFF6200EE),
            background = Color(0xFF212121),
            surface = Color(0xFFBB86FC)
        ),
        typography = MaterialTheme.typography,
        content = content
    )
}