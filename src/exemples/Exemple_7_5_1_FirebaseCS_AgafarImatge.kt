package exemples

import javax.swing.JFrame
import java.awt.EventQueue
import javax.swing.JTextField
import javax.swing.JButton
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

class AgafarImatge_1 : JFrame() {
    val nomIm = JTextField(25)
    val boto = JButton("Agafar")

    val foto = JLabel()
    var bucket: Bucket? = null

    init {
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        setBounds(100, 100, 900, 600)
        setLayout(BorderLayout())

        val panell1 = JPanel(FlowLayout())
        panell1.add(nomIm)
        panell1.add(boto)
        getContentPane().add(panell1, BorderLayout.NORTH)

        getContentPane().add(foto, BorderLayout.CENTER)

        boto.addActionListener { agafar() }

        val serviceAccount = FileInputStream("acces-a-dades-23c92-firebase-adminsdk-x08mj-2f1488c9b7.json")

        val options = FirebaseOptions.Builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .setStorageBucket("acces-a-dades-23c92.appspot.com")
            .build()

        FirebaseApp.initializeApp(options)
    }

    fun agafar() {

        bucket = StorageClient.getInstance().bucket()
        // Instruccions per agafar la imatge
        val blob = bucket?.get(nomIm.getText())

        val destFilePath = Paths.get("auxiliar.jpg")
        blob?.downloadTo(destFilePath)
        val image = ImageIO.read(destFilePath.toFile())
        foto.setIcon(ImageIcon(image))


    }

}

fun main(args: Array<String>) {
    EventQueue.invokeLater {
        AgafarImatge_1().isVisible = true
    }
}

