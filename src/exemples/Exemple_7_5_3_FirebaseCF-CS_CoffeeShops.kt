package exemples

import javax.swing.JFrame
import java.awt.EventQueue
import javax.swing.JComboBox
import javax.swing.JLabel
import java.awt.BorderLayout
import javax.swing.JPanel
import java.awt.FlowLayout
import java.io.FileInputStream
import com.google.firebase.FirebaseOptions
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.cloud.storage.Bucket
import com.google.firebase.cloud.StorageClient
import java.nio.file.Paths
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io.IOException
import java.nio.ByteBuffer
import java.io.ByteArrayInputStream
import javax.swing.ImageIcon
import java.io.File
import javax.swing.JButton
import com.google.firebase.cloud.FirestoreClient
import com.google.cloud.firestore.DocumentChange
import com.google.cloud.firestore.Firestore

class CoffeeShops : JFrame() {
    val nomCafe = JComboBox<String>()
    val foto = JButton()

    var bucket: Bucket? = null
    var database: Firestore? = null

    init {
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        setBounds(100, 100, 900, 600)
        setLayout(BorderLayout())

        val panell1 = JPanel(FlowLayout())
        panell1.add(nomCafe)
        getContentPane().add(panell1, BorderLayout.NORTH)

        getContentPane().add(foto, BorderLayout.CENTER)

        val serviceAccount = FileInputStream("xat-ad-firebase-adminsdk-my2d0-8c69944b34.json")

        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build()

        FirebaseApp.initializeApp(options)

        database = FirestoreClient.getFirestore()
        bucket = StorageClient.getInstance().bucket("xat-ad.appspot.com")

        // Exemple de listener de lectura contínua addSnapshotListener() sobre una col·lecció
        // Per a posar tota la llista de missatges. Sobre /Xats/XatProva/missatges

        database?.collection("CoffeeShops")?.orderBy("nom")?.addSnapshotListener { snapshots, e ->
            for (dc in snapshots!!.getDocumentChanges()) {
                nomCafe.addItem(dc.getDocument().getString("nom"))
            }
        }

        nomCafe.addActionListener { agafar() }

    }

    fun agafar() {
        //Primer agafem el nom de la imatge mirant el document que té el nom com el triat
        //Després agafem la imatge amb eixe nom
        database?.collection("CoffeeShops")?.whereEqualTo("nom", nomCafe.getSelectedItem())!!
            .addSnapshotListener { snapshots, e ->

                for (dc in snapshots!!.getDocumentChanges()) {
                    val blob = bucket?.get("CoffeeShops/" + dc.getDocument().getString("imatge"))

                    //Segona manera de llegir: muntant un reader per a carregar a un ByteBuffer
                    val im = ByteBuffer.allocate(1024 * 1024)
                    blob?.reader()?.read(im)
                    val image = ImageIO.read(ByteArrayInputStream(im.array()))
                    foto.setIcon(ImageIcon(image))
                }
            }
    }

}

fun main(args: Array<String>) {
    EventQueue.invokeLater {
        CoffeeShops().isVisible = true
    }
}

