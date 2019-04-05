package com.anwesh.uiprojects.linkedsquaresinlineview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.squaresinlineview.SquaresInLineView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SquaresInLineView.create(this)
    }
}
