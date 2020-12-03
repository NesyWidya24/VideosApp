package com.nessy.videoapp

import android.app.DownloadManager
import android.app.ProgressDialog
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import kotlin.collections.ArrayList

class AdapterVideo(
    private var context: Context,
    private var videoArrayList: ArrayList<ModelVideo>?
) : RecyclerView.Adapter<AdapterVideo.HolderVideo>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderVideo {
        //inflate layout row_video.xml
        val view = LayoutInflater.from(context).inflate(R.layout.row_video, parent, false)
        return HolderVideo(view)
    }

    override fun onBindViewHolder(holder: HolderVideo, position: Int) {
        //get data, set data, handle clicks etc

        //get data
        val modelVideo = videoArrayList!![position]

        //get specific data
        val id: String? = modelVideo.id
        val title: String? = modelVideo.title
        val desc: String? = modelVideo.desc
        val timestamp: String? = modelVideo.timestamp
        val videoUri: String? = modelVideo.videoUri

        //format date e.g 28/09/2020 05:10PM
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp!!.toLong()
        val formattedDateTime =
            android.text.format.DateFormat.format("dd/MM/yyyy K:mm a", calendar).toString()

        //set data
        holder.titleTv.text = title
        holder.timeTv.text = formattedDateTime
        setVideoUrl(modelVideo, holder)

//        delete click listener
        holder.delFab.setOnClickListener {
            deleteVideo(modelVideo)
        }
//        download click listener
        holder.downloadFab.setOnClickListener {
            dowloadVideo(modelVideo)
        }
    }

    private fun deleteVideo(modelVideo: ModelVideo) {
//        progress dialog, show while video being del
        val progressDialog: ProgressDialog = ProgressDialog(context)
        progressDialog.setTitle("Please Wait")
        progressDialog.setMessage("Delete video..")
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.show()

        val videoId = modelVideo.id!! //to del video from firebase db
        val videoUrl = modelVideo.videoUri!! //to del video from direbase storage

//        del from firebase storage
        val storageReference = FirebaseStorage.getInstance().getReference(videoUrl)
        storageReference.delete()
            .addOnSuccessListener {
                //del from storage

                //del from firebase db
                val databaseReference = FirebaseDatabase.getInstance().getReference("Videos")
                databaseReference.child(videoId)
                    .removeValue()
                    .addOnSuccessListener {
                        //del from firebase db too
                        progressDialog.dismiss()
                        Toast.makeText(context, "Delete successfully..", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener { e ->
                        //failed to delete from firebase db
                        progressDialog.dismiss()
                        Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                //failed to delete from storage
                progressDialog.dismiss()
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun dowloadVideo(modelVideo: ModelVideo) {
        val videoUrl =
            modelVideo.videoUri!! //url of video to be downloaded from firebase using DownloadManager

        //get video reference using video url
        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(videoUrl)
        storageReference.metadata
            .addOnSuccessListener { storageMetadata ->
                //get file/video basic info e.g. title, type
                val fileName = storageMetadata.name //file name in firebase storage
                val fileType =
                    storageMetadata.contentType //file type in firebase storage e.g. video/mp4
                val fileDirectory =
                    Environment.DIRECTORY_DOWNLOADS //video will be saved in this folder

                //init DownloadManager
                val downloadManager =
                    context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

                //get uri of file to be downloaded
                val uri = Uri.parse(videoUrl)

                //create download request, new request for each download, multiple videos can be downloaded
                val request = DownloadManager.Request(uri)

                //download notif visibility
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                request.setDestinationInExternalPublicDir("$fileDirectory", "$fileName.mp4")

                //add request to queue
                downloadManager.enqueue(request)
            }.addOnFailureListener { e ->
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun setVideoUrl(modelVideo: ModelVideo, holder: HolderVideo) {
        holder.progressBar.visibility = View.VISIBLE

        //get video url
        val videoUrl: String? = modelVideo.videoUri

        //MediaController for play/pause/time etc
        val mediaController = MediaController(context)
        mediaController.setAnchorView(holder.videoView)
        val videoUri = Uri.parse(videoUrl)

        holder.videoView.setMediaController(mediaController)
        holder.videoView.setVideoURI(videoUri)
        holder.videoView.requestFocus()

        holder.videoView.setOnPreparedListener { mediaPlayer ->
            mediaPlayer.start()
        }
        holder.videoView.setOnInfoListener(MediaPlayer.OnInfoListener { mp, what, extra ->
            when (what) {
                MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> {
                    //rendering started
                    holder.progressBar.visibility = View.VISIBLE
                    return@OnInfoListener true
                }
                MediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                    //buffering started
                    holder.progressBar.visibility = View.VISIBLE
                    return@OnInfoListener true
                }
                MediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                    //buffering started
                    holder.progressBar.visibility = View.GONE
                    return@OnInfoListener true
                }
            }
            false
        })
        holder.videoView.setOnCompletionListener { mediaPlayer ->
            mediaPlayer.start()
        }
    }

    override fun getItemCount(): Int {
        return videoArrayList!!.size
    }

    //    view holder class holds and inits UI Views or row_video.xml
    class HolderVideo(itemView: View) : RecyclerView.ViewHolder(itemView) {

        //ini UI Views
        var videoView: VideoView = itemView.findViewById(R.id.videoView)
        var titleTv: TextView = itemView.findViewById(R.id.titleTv)
        var timeTv: TextView = itemView.findViewById(R.id.timeTv)
        var progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        var downloadFab: FloatingActionButton = itemView.findViewById(R.id.downloadFab)
        var delFab: FloatingActionButton = itemView.findViewById(R.id.delFab)
    }
}