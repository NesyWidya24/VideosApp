package com.nessy.videoapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_videos.*

class VideosActivity : AppCompatActivity() {

//    arrayList for video List
    private lateinit var  videoArrayList: ArrayList<ModelVideo>
    private lateinit var adapterVideo: AdapterVideo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_videos)

        title = "Videos"

//        function call to load video from firebase
        loadVideosFromFirebase()

        addVideoFab.setOnClickListener {
            startActivity(Intent(this, AddVideoActivity::class.java))
        }
    }

    private fun loadVideosFromFirebase() {
//        init arrayList before adding data into it
        videoArrayList = ArrayList()

        //reference of firebase db
        val ref = FirebaseDatabase.getInstance().getReference("Videos")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear listbefore adding data into it
                videoArrayList.clear()
                for (ds in snapshot.children){
                    val modelVideo = ds.getValue(ModelVideo::class.java)
                    //add to array list
                    videoArrayList.add(modelVideo!!)
                }
                adapterVideo = AdapterVideo(this@VideosActivity, videoArrayList)
                //set adapter to rv
                videoRv.adapter = adapterVideo
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}