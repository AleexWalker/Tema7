package exemples

import java.awt.EventQueue
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JTextArea
import javax.swing.JButton
import javax.swing.JTextField
import java.awt.BorderLayout
import javax.swing.JPanel
import java.awt.FlowLayout
import java.awt.Color
import javax.swing.JScrollPane
import java.io.FileInputStream

import com.google.firebase.FirebaseOptions
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*



class Missatge(var nom: String, var contingut: String)

class CrearXat : JFrame() {

    val etUltimMissatge= JLabel("Últim missatge: ")
    val ultimMissatge= JLabel()

    val etiqueta = JLabel("Missatges:")
    val area = JTextArea()

    val etIntroduccioMissatge = JLabel("Introdueix missatge:")
    val enviar = JButton("Enviar")
    val missatge = JTextField(15)

    // en iniciar posem un contenidor per als elements anteriors
    init {
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        setBounds(100, 100, 450, 300)
        setLayout(BorderLayout())

        // contenidor per als elements
        //Hi haurà títol. Panell de dalt: últim missatge. Panell de baix: per a introduir missatge. Panell central: tot el xat

        val panell1 = JPanel(FlowLayout())
        panell1.add(etUltimMissatge)
        panell1.add(ultimMissatge)
        getContentPane().add(panell1, BorderLayout.NORTH)

        val panell2 = JPanel(BorderLayout())
        panell2.add(etiqueta, BorderLayout.NORTH)
        area.setForeground(Color.blue)
        area.setEnabled(false)
        val scroll = JScrollPane(area)
        panell2.add(scroll, BorderLayout.CENTER)
        getContentPane().add(panell2, BorderLayout.CENTER)

        val panell3 = JPanel(FlowLayout())
        panell3.add(etIntroduccioMissatge)
        panell3.add(missatge)
        panell3.add(enviar)
        getContentPane().add(panell3, BorderLayout.SOUTH)

        setVisible(true)
        enviar.addActionListener{enviar()}

        val serviceAccount = FileInputStream("xat-ad-firebase-adminsdk-my2d0-8c69944b34.json")

        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .setDatabaseUrl("https://xat-ad.firebaseio.com/").build()

        FirebaseApp.initializeApp(options)

        // Exemple de listener de lectura única addListenerForSingleValue()
        // Per a posar el títol. Sobre nomXat
        val nomXat = FirebaseDatabase.getInstance().getReference("nomXat")

        nomXat.addListenerForSingleValueEvent(object : ValueEventListener {
            override
            fun onDataChange(dataSnapshot: DataSnapshot) {
                setTitle(dataSnapshot.getValue().toString())
            }

            override
            fun onCancelled(error: DatabaseError) {
            }
        })

        // Exemple de listener de lectura contínua addValueEventListener()
        // Per a posar l'últim missatge registrat. Sobre a1
        val ultim = FirebaseDatabase.getInstance().getReference("a1")

        ultim.addValueEventListener(object : ValueEventListener {
            override
            fun onDataChange(dataSnapshot: DataSnapshot) {
                ultimMissatge.setText(dataSnapshot.getValue().toString())
                //area.append(dataSnapshot.getValue().toString() + "\n")         // aquesta línia després la llevarem
                area.disabledTextColor = Color.black
            }

            override
            fun onCancelled(error: DatabaseError ) {
            }
        })

        // Exemple de listener d'una llista addChildEventListener()
        // Per a posar tota la llista de missatges. Sobre xat
        val xat = FirebaseDatabase.getInstance().getReference("xat")

        xat.addChildEventListener(object : ChildEventListener {
            override
            fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                area.append(dataSnapshot.child("nom").getValue().toString() + ": "
                        + dataSnapshot.child("contingut").getValue().toString() + "\n"
                )
            }

            override
            fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
            }

            override
            fun onChildRemoved(dataSnapshot: DataSnapshot) {
            }

            override
            fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {
            }

            override
            fun onCancelled(databaseError: DatabaseError) {
            }
        }
        )
    }

    // Exemple de guardar dades sense haver d'esperar per ser una aplicació gràfica
    // Per a guardar dades. Sobre a1, i despŕes sobre la llista xat
    fun enviar() {
        val refA1 = FirebaseDatabase.getInstance().getReference("a1")
        refA1.setValue(missatge.getText(), null)

        val xat = FirebaseDatabase.getInstance().getReference("xat")
        val m = Missatge("Alex", missatge.getText())
        xat.push().setValue(m, null)
    }
}

fun main(args: Array<String>) {
    EventQueue.invokeLater {
        CrearXat().isVisible = true
    }
}

