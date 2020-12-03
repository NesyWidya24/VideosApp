package com.nessy.videoapp

class ModelVideo {
    var id: String? = null
    var title: String? = null
    var desc: String? = null
    var timestamp: String? = null
    var videoUri: String? = null

    constructor(){

    }

    constructor(id: String?, title: String?, desc: String?, timestamp: String?, videoUri: String?) {
        this.id = id
        this.title = title
        this.desc = desc
        this.timestamp = timestamp
        this.videoUri = videoUri
    }


}