package exemples

import java.awt.EventQueue
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JTextArea
import javax.swing.JButton
import javax.swing.JTextField
import javax.swing.JPanel
import javax.swing.JComboBox
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.GridLayout
import java.awt.Color
import javax.swing.JScrollPane
import java.io.FileInputStream

import com.google.api.core.ApiFuture
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.DocumentChange
import com.google.cloud.firestore.DocumentReference
import com.google.cloud.firestore.DocumentSnapshot
import com.google.cloud.firestore.EventListener
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.FirestoreException
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import com.google.cloud.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.Date

class XatCloudMillorat : JFrame() {

    val etComboXats = JLabel("Llista de tots els xats disponibles:")
    val comboXats = JComboBox<String>()

    val etUsuari = JLabel("Nom Usuari:")
    val usuari = JTextField(25)

    val etUltimMissatge = JLabel("Últim missatge: ")
    val ultimMissatge = JLabel()

    val etiqueta = JLabel("Missatges:")
    val area = JTextArea()

    val etIntroduccioMissatge = JLabel("Introdueix missatge:")
    val enviar = JButton("Enviar")
    val missatge = JTextField(15)

    var database: Firestore? = null
    var docRef: DocumentReference? = null

    var listenerUltimMissatge: ListenerRegistration? = null
    var listenerMissatges: ListenerRegistration? = null

    // en iniciar posem un contenidor per als elements anteriors
    init {
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        setBounds(100, 100, 550, 400)
        setLayout(BorderLayout())
        // contenidor per als elements
        //Hi haurà títol. Panell de dalt: últim missatge. Panell de baix: per a introduir missatge. Panell central: tot el xat

        val panell10 = JPanel(FlowLayout())
        panell10.add(etComboXats)
        panell10.add(comboXats)
        val panell11 = JPanel(FlowLayout())
        panell11.add(etUsuari)
        panell11.add(usuari)
        val panell12 = JPanel(FlowLayout())
        panell12.add(etUltimMissatge)
        panell12.add(ultimMissatge)
        val panell1 = JPanel(GridLayout(3, 1))
        panell1.add(panell10)
        panell1.add(panell11)
        panell1.add(panell12)
        getContentPane().add(panell1, BorderLayout.NORTH)

        val panell2 = JPanel(BorderLayout())
        panell2.add(etiqueta, BorderLayout.NORTH)
        area.setForeground(Color.blue)
        area.setEditable(false)
        val scroll = JScrollPane(area)
        panell2.add(scroll, BorderLayout.CENTER)
        getContentPane().add(panell2, BorderLayout.CENTER)

        val panell3 = JPanel(FlowLayout())
        panell3.add(etIntroduccioMissatge)
        panell3.add(missatge)
        panell3.add(enviar)
        getContentPane().add(panell3, BorderLayout.SOUTH)

        setVisible(true)
        comboXats.addActionListener() { inicialitzarXat() }
        enviar.addActionListener { enviar() }

        val serviceAccount = FileInputStream("acces-a-dades-23c92-firebase-adminsdk-x08mj-2f1488c9b7.json")

        val options = FirebaseOptions.Builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build()

        FirebaseApp.initializeApp(options)

        database = FirestoreClient.getFirestore()


        // Exemple de llegir tots els documents d'una col·lecció
        // Per a triar el xat
        val documents = database?.collection("Xats")?.get()?.get()?.getDocuments()
        for (document in documents!!) {
            comboXats.addItem(document.getId())
        }
        comboXats.setSelectedIndex(0)

    }

    fun inicialitzarXat() {
        docRef = database?.collection("Xats")?.document(comboXats.getSelectedItem().toString())
        area.setText("")

        // Exemple de lectura única: senzillament sobre un ApiFuture i sobre ell get()
        // Per a posar el títol. Sobre /Xats/XatProva/nomXat
        val future = docRef?.get()
        val nomXat = future?.get()?.getString("nomXat")
        this.setTitle(nomXat)

        // Exemple de listener de lectura contínua addSnapshotListener()
        // Per a posar l'últim missatge registrat. Sobre /Xats/XatProva/ultimUsuari i /Xats/XatProva/ultimMissatge
        // Si estava en marxa, el parem abans de tornar-lo a llançar
        if (listenerUltimMissatge != null)
            listenerUltimMissatge!!.remove()

        listenerUltimMissatge = docRef?.addSnapshotListener { snapshot, e ->
            ultimMissatge.setText(snapshot?.getString("ultimMissatge"))
        }

        // Exemple de listener de lectura contínua addSnapshotListener() sobre una col·lecció
        // Per a posar tota la llista de missatges. Sobre /Xats/XatProva/missatges
        // Si estava en marxa, el parem abans de tornar-lo a llançar
        if (listenerMissatges != null)
            listenerMissatges!!.remove()

        listenerMissatges = docRef?.collection("mensajes")?.orderBy("data")?.addSnapshotListener { snapshots, e ->
            for (dc in snapshots!!.getDocumentChanges()) {
                val dData = dc.getDocument().getDate("data")
                val d = SimpleDateFormat("dd-MM-yyyy HH:mm").format(dData)
                area.append(
                    dc.getDocument().getString("nom") + " (" + d + "): " + dc.getDocument().getString("contingut") + "\n"
                )
            }
        }
    }


    // Exemple de guardar dades en Cloud Firestore
    // Per a guardar dades. Sobre /Xats/XatProva i després sobre /Xats/Xat1
    fun enviar() {
        val database = FirestoreClient.getFirestore()
        val docXat = database.collection("Xats").document(comboXats.getSelectedItem().toString())

        val dades = HashMap<String, Any>()
        dades.put("ultimUsuari", usuari.getText())
        dades.put("ultimMissatge", missatge.getText())

        docXat.update(dades)

        val dades2 = HashMap<String, Any>()
        dades2.put("nom", usuari.getText())
        dades2.put("contingut", missatge.getText())

        val m = MissatgeCloud(usuari.getText(), Date(), missatge.getText())
        docXat.collection("mensajes").add(m)
    }

}

fun main(args: Array<String>) {
    EventQueue.invokeLater {
        XatCloudMillorat().isVisible = true
    }
}

