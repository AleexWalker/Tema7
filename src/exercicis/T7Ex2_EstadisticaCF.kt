package exercicis

import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JComboBox
import javax.swing.JTextArea
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.JPanel
import java.awt.Color
import javax.swing.JScrollPane
import java.io.FileInputStream
import com.google.firebase.FirebaseOptions
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.DocumentChange
import com.google.cloud.firestore.DocumentReference
import com.google.cloud.firestore.ListenerRegistration
import com.google.firebase.FirebaseApp
import com.google.firebase.cloud.FirestoreClient
import java.awt.EventQueue
import java.text.SimpleDateFormat

class EstadisticaCF : JFrame() {

    val etCombo = JLabel("Llista de províncies:")
    val comboProv = JComboBox<String>()

    val etiqueta = JLabel("Estadístiques:")
    val area = JTextArea()

    var referenciaDocumento: DocumentReference? = null

    var listenerUltimMissatge: ListenerRegistration? = null
    var listenerProvincias: ListenerRegistration? = null

    // en iniciar posem un contenidor per als elements anteriors
    init {
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        setBounds(100, 100, 450, 400)
        setLayout(BorderLayout())
        // contenidor per als elements

        val panell1 = JPanel(FlowLayout())
        panell1.add(etCombo)
        panell1.add(comboProv)
        getContentPane().add(panell1, BorderLayout.NORTH)

        val panell2 = JPanel(BorderLayout())
        panell2.add(etiqueta, BorderLayout.NORTH)
        area.setForeground(Color.blue)
        area.setEditable(false)
        val scroll = JScrollPane(area)
        panell2.add(scroll, BorderLayout.CENTER)
        getContentPane().add(panell2, BorderLayout.CENTER)

        setVisible(true)

        val serviceAccount = FileInputStream("xat-ad-firebase-adminsdk-my2d0-8c69944b34.json")

        val options = FirebaseOptions.Builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build()

        FirebaseApp.initializeApp(options)

        val database = FirestoreClient.getFirestore()

        // Instruccions per a omplir el JComboBox amb les províncies
        val documents = database?.collection("Estadistica")?.orderBy("Provincia")?.get()?.get()?.getDocuments()
        val listaProvincias = mutableSetOf<String>()
        for (document in documents!!) {
            document.getString("Provincia")?.let { listaProvincias.add(it) }
            //comboProv.addItem(document.getString("Provincia"))
        }
        listaProvincias.forEach {
            comboProv.addItem(it.toString())
        }

        //println(database.collection("Estadisitica").orderBy("Provincia").get().get().documents.toString())

        // Instruccions per agafar la informació de tots els anys de la província triada

        comboProv.addActionListener() {
            area.text = ""
            database?.collection("Estadistica").whereEqualTo("Provincia", comboProv.selectedItem.toString()).orderBy("any").addSnapshotListener { querySnapshot, firestoreException ->
                for (documento in querySnapshot!!.documentChanges) {
                    when (documento.type) {
                        DocumentChange.Type.ADDED -> {
                            area.append(documento.document.getString("any") + ": " + documento.document.getString("Dones") + " - " + documento.document.getString("Homes") + "\n")
                        }
                        DocumentChange.Type.MODIFIED ->
                            println("El documento ha sido modificado: " + documento.getDocument().getData());
                        DocumentChange.Type.REMOVED ->
                            println("Un mensaje ha sido borrado: " + documento.getDocument().getData());
                    }
                }
            }
            /*for (j in documents) {
                j.getString("Provincia")
                if (comboProv.selectedItem.toString().equals(j.getString("Provincia")))
                    area.text = j.getString("any" + ": " + j.getString("Dones") + " - " + j.getString("Homes"))

                /*for (i in 0..listaProvincias.size) {
                if (comboProv.selectedItem.toString() == database?.document(i.toString()).id)
                    area.text = ""
            }*/
            }
            val provincias = referenciaDocumento?.get()
            println(comboProv.selectedIndex)*/
        }
        comboProv.setSelectedIndex(0)
    }
}

fun main(args: Array<String>) {
    EventQueue.invokeLater {
        EstadisticaCF().isVisible = true
    }
}
