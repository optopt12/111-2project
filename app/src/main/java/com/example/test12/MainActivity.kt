package com.example.test12

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.test12.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var msgAdapter: MsgAdapter
    private lateinit var answer:String
    //建立消息数据列表
    private var msgList: MutableList<Msg> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initMsg()   //初始化
        initRv()
        setListener()
    }

    private fun setListener() {
        binding.run {
            sendButton.setOnClickListener {
                val content: String = editText.text.toString()   //获取输入框的文本
                if (content.isNotEmpty()) {
                    msgList.add(Msg(content, Msg.RIGHT))    //将输入的消息及其类型添加进消息数据列表中
                    aichat()
                    editText.hideKeyboard() //收起鍵盤
                    editText.setText("")    //清空输入框文本
                }
            }
        }
    }
    private fun initRv() {
        binding.recyclerView.apply {
            msgAdapter = MsgAdapter(msgList)   //建立适配器实例
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)  //布局为线性垂直
            adapter = msgAdapter
        }
    }
    private fun initMsg() {
        msgList.add(Msg("發送訊息以獲得回覆", Msg.LEFT))
    }
    private fun  aichat(){
        val API_KEY = "Bearer $MY_API_KEY"
        val openAI = OpenAI(API_KEY)
        var prompt =  editText.text.toString()
        val message = binding.editText.text.toString()
        CoroutineScope(Dispatchers.IO).launch {
            prompt += "\n\nHuman: $message \nAI:"
            try {
                val response = openAI.createCompletion(
                    model = "text-davinci-003",
                    prompt = prompt,
                    temperature = 0.3,
                    max_tokens = 200,
                    top_p = 1,
                    frequency_penalty = 0.0,
                    presence_penalty = 0.0,
                    stop = listOf(" Human:", " AI:")
                )
                if (response.isSuccessful) {
                    answer = response.body()?.choices?.first()?.text.toString()
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(this@MainActivity, answer, Toast.LENGTH_SHORT).show()
                        msgList.add(Msg(answer, Msg.LEFT))
                        msgAdapter.notifyDataSetChanged()   //为RecyclerView添加末尾子项
                    }
                } else {
                    Log.d("RESPONSE", "Error: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                Log.d("RESPONSE", "Error: $e")
            }
        }
    }
    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
}

