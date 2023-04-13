package com.comunidadedevspace.taskbeats

import android.app.Activity
import android.app.Notification.Action
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.google.android.material.snackbar.Snackbar

class TaskDetailActivity : AppCompatActivity() {

    var task: Task? = null
    lateinit var btnDone:Button

    companion object{
        private const val TASK_EXTRA = "task.extra.detail"

        fun start(context: Context, task:Task?):Intent{
            val intent = Intent(context, TaskDetailActivity::class.java)
                .apply {
                    putExtra(TaskDetailActivity.TASK_EXTRA, task)
                }
            return intent
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)
        setSupportActionBar(findViewById(R.id.toolbar))

        //pegar task

        task  = intent.getSerializableExtra(TASK_EXTRA) as Task?

        val edtTitle = findViewById<EditText>(R.id.edt_task_title_detail)
        val edtDesc = findViewById<EditText>(R.id.edt_task_description)
        btnDone = findViewById<Button>(R.id.btn_done)

        if(task != null){
            edtTitle.setText(task!!.title)
            edtDesc.setText(task!!.description)
        }

        btnDone.setOnClickListener{
            val title = edtTitle.text.toString()
            val desc = edtDesc.text.toString()

            if(title.isNotEmpty() && desc.isNotEmpty()){
                if(task == null){
                    addOrUpdateTask(title,desc, ActionType.CREATE,0 )
                }else{
                    addOrUpdateTask(title,desc,ActionType.UPDATE, task!!.id)
                }

            }else{
                showMessage(it, "Preencha os campos")
            }

        }


    }


    private fun addOrUpdateTask (title:String,
                                 description:String,
                                 actionType:ActionType,
                                 id:Int){
        val task = Task(id,title,description)
        returnAction(task, actionType)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_taskdetail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.delete_task -> {
                task?.let {
                    returnAction(task!!, ActionType.DELETE)
                }
                showMessage(btnDone,"Item não encontrado")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun returnAction(task:Task, actionType: ActionType){
        val intent = Intent()
            .apply {
                val taskAction = TaskAction(task, actionType.name)
                putExtra(TASK_ACTION_RESULT,taskAction)
            }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun showMessage(view: View, message:String){
        Snackbar.make(view,message, Snackbar.LENGTH_LONG)
            .setAction("Action", null)
            .show()
    }
}