package exemples

import java.io.FileInputStream
import com.google.firebase.FirebaseOptions
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import java.util.concurrent.CountDownLatch
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.DatabaseError

fun main() {
    val serviceAccount = FileInputStream("acces-a-dades-23c92-firebase-adminsdk-x08mj-2f1488c9b7.json")

    val options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
        .setDatabaseUrl("https://acces-a-dades-23c92-default-rtdb.firebaseio.com/").build()

    FirebaseApp.initializeApp(options)

    val empresa = FirebaseDatabase.getInstance().getReference("empresa")

    val done = CountDownLatch (1)

    empresa.child("empleat").child("0").child("edat").setValue("33",
        object : DatabaseReference.CompletionListener {
            override fun onComplete(p0: DatabaseError?, p1: DatabaseReference) {
                done.countDown()
            }
        })
    done.await()
}

