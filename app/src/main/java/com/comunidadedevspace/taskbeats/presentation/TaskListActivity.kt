package com.comunidadedevspace.taskbeats.presentation

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.comunidadedevspace.taskbeats.R
import com.comunidadedevspace.taskbeats.data.AppDataBase
import com.comunidadedevspace.taskbeats.data.Task
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {


    //Banco de Dados
    private val dataBase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDataBase::class.java, "taskbeats-database"
        ).build()
    }

    private val dao by lazy {
        dataBase.taskDao()
    }


    private val adapter: TaskListAdapter by lazy {
        TaskListAdapter(::onListItemClicked)
    }

    private lateinit var ctnContent : LinearLayout

    private val resultActivity = registerForActivityResult(

        ActivityResultContracts.StartActivityForResult()

    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK){
            val data = result.data
            val taskAction = data?.getSerializableExtra(TASK_ACTION_RESULT) as TaskAction
            val task : Task = taskAction.task

            when (taskAction.actionType) {
                ActionType.DELETE.name -> {
                    deleteById(task.id)
                }
                ActionType.CREATE.name -> {
                    insertIntoDataBase(task)
                }
                ActionType.UPDATE.name -> {
                    updateIntoDataBase(task)
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)
        setSupportActionBar(findViewById(R.id.toolbar))

        listFromDataBase()

        ctnContent = findViewById(R.id.ctn_content)


        //RecyclerView
        val rv_tasklist: RecyclerView = findViewById(R.id.rv_task_list)
        rv_tasklist.adapter = adapter

        val fab = findViewById<FloatingActionButton>(R.id.floatingActionButton)
        fab.setOnClickListener{
            openTaskListDetail(null)
        }

    }

    private fun insertIntoDataBase(task: Task){
        CoroutineScope(IO).launch{
            dao.insert(task)
            listFromDataBase()
        }
    }
    private fun updateIntoDataBase(task: Task) {
        CoroutineScope(IO).launch{
            dao.update(task)
            listFromDataBase()
        }
    }
    private fun listFromDataBase(){
        CoroutineScope(IO).launch{
            val myDataBaseList:List<Task> = dao.getAll()
            adapter.submitList(myDataBaseList)

        }
    }

    private fun deleteById(id:Int){
        CoroutineScope(IO).launch{
            dao.delete(id)
            listFromDataBase()
        }
    }

    private fun deleteAll(){
        CoroutineScope(IO).launch{
            dao.deleteAll()
            listFromDataBase()
        }
    }

    private fun showMessage(view:View, message:String){
        Snackbar.make(view,message, Snackbar.LENGTH_LONG)
            .setAction("Action", null)
            .show()
    }

    private fun onListItemClicked(task: Task){
        openTaskListDetail(task)
    }

    private fun openTaskListDetail(task: Task? = null){
        val intent = TaskDetailActivity.start(this, task)
        resultActivity.launch(intent)
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_tasklist, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.delete_all_task -> {
                deleteAll()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}

enum class ActionType{
     DELETE,
     UPDATE,
     CREATE
}

data class TaskAction (
    val task: Task,
    val actionType: String
    ) : java.io.Serializable


const val TASK_ACTION_RESULT = "TASK_ACTION_RESULT"
