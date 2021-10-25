package com.example.myapplication.presentation



import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.Factory
import com.example.myapplication.R
import kotlinx.android.synthetic.main.activity_history.*


class HistoryActivity : AppCompatActivity() {

    private val factory : Factory = Factory()
    @Suppress("SENSELESS_COMPARISON")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //set the back "button"
        actionBar?.setDisplayHomeAsUpEnabled(true)

        setContentView(R.layout.activity_history)
        empty.visibility = View.GONE

        var list : MutableList<String> = mutableListOf()
        val pers = factory.getPersistence(this.applicationContext)
        list = pers.findTrips()




        if(list.isEmpty()){
            empty.visibility = View.VISIBLE
        }
        else {
            //display the content of the file if is not empty
            val adapter = ArrayAdapter(this, R.layout.row, list)
            val listView: ListView = listView
            listView.adapter = adapter
            listView.onItemClickListener = AdapterView.OnItemClickListener {
                    parent, view, position, id ->

                // remove clicked item from list view
                //val listItems = arrayOf("Delete", "Don't delete")
                val mBuilder = AlertDialog.Builder(this)
                mBuilder.setTitle("Delete Trip?")

                var inputSelection =1

                //mBuilder.setSingleChoiceItems(listItems, -1) { _, i ->
                //    inputSelection = i
                //}

                mBuilder.setPositiveButton(
                    "Yes"
                ) { _, _ ->
                    //if (inputSelection==0){
                        Toast.makeText(this.applicationContext, "Deleted", Toast.LENGTH_SHORT).show()
                        pers.removeLine(position)
                        list.removeAt(position)
                        if(list.isEmpty()) {empty.visibility = View.VISIBLE}
                        adapter.notifyDataSetChanged()
                   // }else { Toast.makeText(this.applicationContext, "Don't deleted", Toast.LENGTH_SHORT).show()}

                }
                mBuilder.setNegativeButton(
                    "No"
                ) { dialog, _ -> dialog.cancel() }

                val mDialog = mBuilder.create()
                mDialog.show()

            }
        }



    }

    override fun onSupportNavigateUp(): Boolean {
        //set the back "button" to resume the main activity and not to re create it
        finish()
        return true
    }

}
