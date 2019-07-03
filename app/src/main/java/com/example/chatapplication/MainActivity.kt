package com.example.chatapplication

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.backgroundDrawable
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        register_material_button.setOnClickListener {

            var user = email_text_view.text.toString() + "@wasteconnectionsApp.com"
            var password = password_text_view.text.toString()

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(user, password)
                .addOnCompleteListener {
                    if (!it.isSuccessful) return@addOnCompleteListener
                    Log.e("Main", "Successful created user name with uid: ${it.result?.user?.uid}")

                    uploadImageToFirebaseStorage()
                }
                .addOnFailureListener {
                    Log.e("Main", "Failed due to ${it.message}")
                }

        }

        login_material_button.setOnClickListener {

            var user = email_text_view.text.toString() + "@wasteconnectionsApp.com"
            var password = password_text_view.text.toString()

            FirebaseAuth.getInstance().signInWithEmailAndPassword(user, password)
            //TODO : IMPLEMENT THIS
        }

        register_image_view.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

    }

    private fun uploadImageToFirebaseStorage() {

        if (selectedPhotoUri == null) return

        var filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.e("PHOTO", "photo is saved : ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {
                    Log.e("PHOTO", "path is $it")

                    saveUsertToFirebaseDatabase(it.toString())
                }
            }
    }

    private fun saveUsertToFirebaseDatabase(profileImageUrl:String) {
        var uid = FirebaseAuth.getInstance().uid ?:""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user = User(uid, email_text_view.text.toString(), profileImageUrl,   password_text_view.text.toString())
        ref.setValue(user)
            .addOnSuccessListener {
                Log.e("STUFF", "finally saved user to database")
            }.addOnFailureListener {
                Log.e("STUFF", "user not saved to database")
            }
    }

    var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Log.e("REGISTER", "photo was selected")

            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            val bitmapDrawable = BitmapDrawable(bitmap)
            register_image_view.backgroundDrawable = bitmapDrawable
        }
    }
}
